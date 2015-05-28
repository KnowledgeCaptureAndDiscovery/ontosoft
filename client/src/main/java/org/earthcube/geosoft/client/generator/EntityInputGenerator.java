package org.earthcube.geosoft.client.generator;

import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import org.earthcube.geosoft.client.components.form.formgroup.input.IEntityInput;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

public class EntityInputGenerator extends Generator {

  /**
   * Only classes implementing this interface will be supported by EntityInputFactoryImpl.
   * Some filter is required otherwise 
   */
  public static final Class<?> MARKING_INTERFACE = IEntityInput.class;

  /**
   * Generate Java code for factory class capable of returning instance for a class name.
   * <p>
   * It is not true introspection. In fact the whole factory class is a mapper; mapping class name
   * to constructor call. Like {@code if (className=="my.code.Foo1") return new my.code.Foo1();}
   * Only small section of classes can be instantiable this way. Supported classes are filtered
   * based marker interface, annotation or regex. This implementation is uses marker interface
   * {@link EntryPoint}.
   * 
   * @param logger
   * @param context
   *            provides list of all visible classes, see {@link GeneratorContext#getTypeOracle()}
   *            , and stream for printing the Java code, see {@link SourceWriter}.
   * @param typeName
   *            example: "my.code.client.reflection.ClassFromStringFactory" ???
   * @return implementation class name, example:
   *         my.code.client.reflection.ClassFromStringFactoryImpl
   */
  @Override
  public String generate(TreeLogger logger, GeneratorContext context, String typeName)
    throws UnableToCompleteException
  {
    // Filter all classes visible to GWT client side and return only supported ones
    List<JClassType> allInstantiableClasses = getAllInstantiableClassesByThisfactory(context);

    final String genPackageName = EntityInputFactory.class.getPackage().getName();
    logger.log(Type.INFO, "genPackageName: " + genPackageName);

    final String genClassName = EntityInputFactory.class.getSimpleName() + "Impl";

    // prepare Composer. Composer prepares shell of the Java code, so we need to set package and class name
    ClassSourceFileComposerFactory composer = new ClassSourceFileComposerFactory(genPackageName, genClassName);
    
    // register (add) required imports to Composer 
    // but the generated class uses fully qualified names everywhere, no imports required
    
    PrintWriter printWriter = context.tryCreate(logger, genPackageName, genClassName);
    if (printWriter != null) {
      composer.addImplementedInterface(EntityInputFactory.class.getSimpleName());

      // Get source stream from Composer. It already contains shell of the new class
      // like package, imports and full class signature
      SourceWriter sourceWriter = composer.createSourceWriter(context, printWriter);

      // generate class content, content of the Java file, contructor and factory method
      generateFactoryClass(genClassName, composer, sourceWriter, logger, allInstantiableClasses);
    }
    return composer.getCreatedClassName();
  }

  /**
   * Filter all classes visible to GWT client side and return only those deemed instantiable
   * through the factory class we are generating (that is EntityInputFactoryImpl).
   * <p>
   * Allowing all classes to be instantiable would potentially generate too huge class method
   * perhaps even too huge to compile. There must be some sub-selection. Classes can be filtered
   * based on marker interface, or class annotation see {@link JClassType#getAnnotation(Class)},
   * or some regex matching based on package or class name.
   * 
   * @param context
   * @return Filter all classes visible to GWT client side and return only those deemed
   *         instantiable
   */
  private List<JClassType> getAllInstantiableClassesByThisfactory(GeneratorContext context) {
    TypeOracle oracle = context.getTypeOracle();
    JClassType markerInterface = oracle.findType(MARKING_INTERFACE.getName());
    List<JClassType> allInstantiableClasses = new LinkedList<JClassType>();

    for (JClassType classType : oracle.getTypes()) {
      if (!classType.equals(markerInterface) && classType.isAssignableTo(markerInterface)) {
        allInstantiableClasses.add(classType);
      }
    }

    return allInstantiableClasses;
  }

  /**
   * Generate factory class Java code. Example output (in sourceWriter):
   * 
   * <pre>
   * package ...
   * import ...
   * public class EntityInputFactoryImpl implements EntityInputFactory {
   *     // constructor
   *     public EntityInputFactoryImpl() {}
   *     // factory method
   *     ....
   * }
   * </pre>
   * 
   * @param genClassName
   * @param composer
   * @param sourceWriter
   * @param logger
   * @param allInstantiableClasses
   */
  private void generateFactoryClass(
    String genClassName,
    ClassSourceFileComposerFactory composer,
    SourceWriter sourceWriter,
    TreeLogger logger,
    List<JClassType> allInstantiableClasses)
  {
    // generate factory constructor; simple non-parametric constructor
    sourceWriter.println("public " + genClassName + "( ) {}");
    sourceWriter.println();

    // generate factory methods
    generateFactoryMethods(allInstantiableClasses, sourceWriter, logger);

    // flush all writes to stream; mark generating process as finished and ready for compiler
    sourceWriter.commit(logger);
  }

  /**
   * Generate Java code for factory method. Example output (in sourceWriter):
   * 
   * <pre>
   * public Object instantiate(String className) {
   *     if (className == null) {
   *        return null
   *     }
   *     else if (className.equals("my.code.client.Foo1")) {
   *        return new my.code.client.Foo1();
   *     }
   *     else if (className.equals("my.code.client.Foo2")) {
   *        return new my.code.client.Foo1();
   *     }
   *     ... all supported classes follows ...
   *     ... if nothing matches return null ...
   * }
   * </pre>
   * 
   * @param allInstantiableClasses
   * @param sourceWriter
   */
  private void generateFactoryMethods(List<JClassType> allInstantiableClasses, SourceWriter sourceWriter, TreeLogger logger) {
    // instantiate method
    sourceWriter.println("public Object instantiate(String className) {");
    sourceWriter.println("if (className == null) {");
    sourceWriter.println("return null;");
    sourceWriter.println("}");
    for (JClassType classType : allInstantiableClasses) {
      logger.log(Type.INFO, "creating class: " + classType);
      if (classType.isAbstract())
        continue;
      sourceWriter.println("else if (className.equals(\"" + classType.getQualifiedSourceName() + "\")) {");
      sourceWriter.println("return new " + classType.getQualifiedSourceName() + "( );");
      sourceWriter.println("}");
    }
    sourceWriter.println("return null;");
    sourceWriter.println("}");
    
    // hasClass method
    sourceWriter.println("public boolean hasClass(String className) {");
    sourceWriter.println("if (className == null) {");
    sourceWriter.println("return false;");
    sourceWriter.println("}");
    for (JClassType classType : allInstantiableClasses) {
      if (classType.isAbstract())
        continue;
      sourceWriter.println("else if (className.equals(\"" + classType.getQualifiedSourceName() + "\")) {");
      sourceWriter.println("return true;");
      sourceWriter.println("}");
    }
    sourceWriter.println("return false;");
    sourceWriter.println("}");
  }

}
