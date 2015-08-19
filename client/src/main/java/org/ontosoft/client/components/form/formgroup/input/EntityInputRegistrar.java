package org.ontosoft.client.components.form.formgroup.input;

import java.util.HashMap;

import org.ontosoft.client.generator.EntityInputFactory;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.shared.GWT;

public class EntityInputRegistrar {
  private static EntityInputFactory inputFactory = GWT.create(EntityInputFactory.class);
  private static HashMap<String, String> inputClasses = new HashMap<String, String>();
  
  public static boolean register(String typeid, String inputClass) {
    if(inputFactory.hasClass(inputClass)) {
      inputClasses.put(typeid, inputClass);
      return true;
    }
    return false;
  }
  
  public static IEntityInput getInput(Entity entity, MetadataProperty mprop, Vocabulary vocabulary) 
      throws Exception {
    String inputClass = inputClasses.get(mprop.getRange());
    if(inputClass != null) {
      Object item = inputFactory.instantiate(inputClass);
      if(item == null) {
        GWT.log("Cannot instantiate "+mprop.getRange());
        throw new Exception("Cannot instantiate "+mprop.getRange());
      }
      else if(item instanceof IEntityInput) {
        ((IEntityInput) item).createWidget(entity, mprop, vocabulary);
        return (IEntityInput) item;
      }
      else {
        GWT.log("Item not an extension of IEntityInput");
        throw new Exception("Item not an extension of IEntityInput");
      }
    }
    return null;
  }
}
