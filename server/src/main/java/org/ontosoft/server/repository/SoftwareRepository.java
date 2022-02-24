package org.ontosoft.server.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.ontosoft.server.repository.adapters.EntityRegistrar;
import org.ontosoft.server.repository.adapters.IEntityAdapter;
import org.ontosoft.server.repository.plugins.CodeAnalysisPlugin;
import org.ontosoft.server.repository.plugins.GithubPlugin;
import org.ontosoft.server.users.User;
import org.ontosoft.server.users.UserDatabase;
import org.ontosoft.server.util.Config;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.EnumerationEntity;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.provenance.Provenance;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.permission.Permission;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.SearchConfig;
import org.ontosoft.shared.classes.vocabulary.UIConfig;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginRegistrar;
import org.ontosoft.shared.search.EnumerationFacet;
import org.ontosoft.shared.utils.PermUtils;
import org.ontosoft.shared.classes.users.UserCredentials;

import com.fasterxml.jackson.databind.ObjectMapper;

import edu.isi.kcap.ontapi.KBAPI;
import edu.isi.kcap.ontapi.KBObject;
import edu.isi.kcap.ontapi.KBTriple;
import edu.isi.kcap.ontapi.OntFactory;
import edu.isi.kcap.ontapi.OntSpec;
import edu.isi.kcap.ontapi.SparqlQuerySolution;

public class SoftwareRepository {

  KBAPI ontkb, catkb, enumkb;
  OntFactory fac;
  ProvenanceRepository prov;
  PermissionRepository perm_repo;
  
  String tdbdir;
  String dbdir;
  String owlns, rdfns, rdfsns;
  String onturi,caturi,liburi,enumuri;
  String ontns,catns;
  
  String server;
  
  String topclass, uniongraph;

  Vocabulary vocabulary;
  Map<String, List<MetadataEnumeration>> enumerations;

  SecurityContext securityContext;
  
  ObjectMapper mapper = new ObjectMapper();
  
  private String allusers = "*";
  
  static SoftwareRepository singleton = null;

  public static SoftwareRepository get() {
    if(singleton == null)
      singleton = new SoftwareRepository();
    return singleton;
  }
  
  public SoftwareRepository() {
    setConfiguration();
    registerPlugins();    
    initializeKB();
    this.prov = new ProvenanceRepository();
    this.perm_repo = new PermissionRepository();
  }
  
  public String LIBURI() {
    if(liburi == null)
      liburi = server.replaceAll("\\/$", "") + "/software/";
    return liburi;
  }
  
  private String USERURI() {
    return server.replaceAll("\\/$", "") + "/users/"; 
  }
  
  private String USERNS() {
    return USERURI();
  }
  
  public String LIBNS() {
    return LIBURI();
  }
  
  public String ENUMURI() {
    return LIBURI() + "enumerations";
  }
  
  public String ENUMNS() {
    return ENUMURI() + "#";
  }
  
  private void setConfiguration() {
    PropertyListConfiguration props = Config.get().getProperties();
    this.server = props.getString("server");
    onturi = KBConstants.ONTURI();
    caturi = KBConstants.CATURI();
    liburi = this.LIBURI();
    enumuri = this.ENUMURI();
    
    ontns = KBConstants.ONTNS();
    catns = KBConstants.CATNS();
    
    tdbdir = props.getString("storage.tdb");
    File tdbdirf = new File(tdbdir);
    if(!tdbdirf.exists() && !tdbdirf.mkdirs()) {
      System.err.println("Cannot create tdb directory : "+tdbdirf.getAbsolutePath());
    }

    // TODO: Parse "imports" and "exports" details
    
    topclass = ontns + "Software";
    uniongraph = "urn:x-arq:UnionGraph";
    
    owlns = KBConstants.OWLNS();
    rdfns = KBConstants.RDFNS();
    rdfsns = KBConstants.RDFSNS();
  }

  /**
   * KB Initialization
   */
  
  public void reloadKBCaches() {
    if(this.ontkb != null)
      this.ontkb.delete();
    if(this.catkb != null)
      this.catkb.delete();
    
    this.initializeKB();
  }
  
  public void initializeKB() {
    this.fac = new OntFactory(OntFactory.JENA, tdbdir);
    try {
      this.ontkb = fac.getKB(onturi, OntSpec.PELLET, false, true);
      this.catkb = fac.getKB(caturi, OntSpec.PELLET, false, true);
      this.enumkb = fac.getKB(enumuri, OntSpec.PLAIN, true);
      
      this.registerEntityAdapters();
      this.initializeVocabularyFromKB();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  
  /**
   * Vocabulary Initialization
   */
  
  public void initializeVocabularyFromKB() {
    this.vocabulary = new Vocabulary();
    this.fetchCategoriesFromKB();
    this.fetchPropertiesFromKB();
    this.fetchTypesFromKB();
    this.fetchEnumerationsFromKB();
  }
  
  private void fetchCategoriesFromKB() {
    KBObject topcatcls = this.catkb.getConcept(catns + "MetadataCategory");
    this.vocabulary.addCategory(this.fetchCategoryFromKB(topcatcls));
  }
  
  private MetadataCategory fetchCategoryFromKB(KBObject cls) {
    String clsid = cls.getID();
    MetadataCategory cat = new MetadataCategory();
    // Get basic info
    cat.setId(clsid);
    cat.setName(cls.getName());
    cat.setLabel(this.catkb.getLabel(cls));
    cat.setSublabel(this.catkb.getComment(cls));
    // Get uiconfig
    KBObject uiprop = this.catkb.getAnnotationProperty(ontns + "uiConfig");
    KBObject uiconfval = this.catkb.getPropertyValue(cls, uiprop);
    if(uiconfval != null && uiconfval.getValue() != null) {
      String uiconfstr = uiconfval.getValue().toString();
      try {
        UIConfig uiconf = mapper.readValue(uiconfstr, UIConfig.class);
        cat.setUiConfig(uiconf);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    // Get subcats
    for(KBObject subcls : this.catkb.getSubClasses(cls, true)) {
      MetadataCategory subcat = this.fetchCategoryFromKB(subcls);
      subcat.setParent(clsid);
      cat.addChild(subcat.getId());
      this.vocabulary.addCategory(subcat);
    }
    return cat;
  }
  
  private void fetchTypesFromKB() {
    try {
      KBObject topcls = this.ontkb.getConcept(owlns + "Thing");
      this.fetchTypesFromKB(topcls);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  private MetadataType fetchTypesFromKB(KBObject cls) {
    String clsid = cls.getID();
    MetadataType type = new MetadataType();
    type.setId(clsid);
    type.setName(cls.getName());
    type.setLabel(this.ontkb.getLabel(cls));
    for(KBObject subcls : this.ontkb.getSubClasses(cls, true)) {
      MetadataType subtype = this.fetchTypesFromKB(subcls);
      if(!clsid.startsWith(owlns))
        subtype.setParent(clsid);
      if(!subtype.getId().startsWith(owlns))
        type.addChild(subtype.getId());
      this.vocabulary.addType(subtype);
    }

    return type;
  }
  
  private void fetchEnumerationsFromKB() {
    try {
      KBAPI allkb = fac.getKB(uniongraph, OntSpec.PLAIN);
      KBObject rdftype = ontkb.getProperty(KBConstants.RDFNS() + "type");
      MetadataType enumtype = this.vocabulary.getType(KBConstants.ONTNS() + "EnumerationEntity");
      //MetadataType swtype = this.vocabulary.getType(KBConstants.ONTNS() + "Software");
      
      enumerations = new HashMap<String, List<MetadataEnumeration>>();
      
      for(MetadataType type : this.vocabulary.getSubTypes(enumtype)) {
        List<MetadataEnumeration> typeenums = new ArrayList<MetadataEnumeration>();
        KBAPI kb = enumkb;
        //if(vocabulary.isA(type, swtype))
            kb = allkb;
        for(KBTriple t : kb.genericTripleQuery(null, rdftype, ontkb.getConcept(type.getId()))) {
          MetadataEnumeration menum = new MetadataEnumeration();
          KBObject inst = t.getSubject();
          menum.setId(inst.getID());
          menum.setName(inst.getName());
          menum.setType(type.getId());
          String label = this.ontkb.getLabel(inst);
          if(label == null)
            label = inst.getName();
          menum.setLabel(label);

          typeenums.add(menum);
        }
        enumerations.put(type.getId(), typeenums);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Map<String, List<MetadataEnumeration>> getEnumerations() {
    return enumerations;
  }
  
  public List<MetadataEnumeration> getEnumerationsForType(String typeid) {
    return enumerations.get(typeid);
  }
  
  /**
   * Plugin Registration
   */
  private void registerPlugins() {
    PluginRegistrar.registerPlugin(new GithubPlugin());
    PluginRegistrar.registerPlugin(new CodeAnalysisPlugin());
  }
  
  private String createPropertyLabel(String pname) {
    // Remove starting "has"
    pname = pname.replaceAll("^has", "");
    // Convert camel case to spaced human readable string
    pname = pname.replaceAll(String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])", "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"), " ");
    // Make first letter upper case
    return pname.substring(0,1).toUpperCase() + pname.substring(1);
  }
  
  private void fetchPropertiesFromKB() {
    KBObject catprop = this.ontkb.getAnnotationProperty(ontns + "category");
    KBObject quesprop = this.ontkb.getAnnotationProperty(KBConstants.DCNS() + "description");
    KBObject reqprop = this.ontkb.getAnnotationProperty(ontns + "isRequired");
    KBObject uiprop = this.ontkb.getAnnotationProperty(ontns + "uiConfig");
    KBObject searchprop = this.ontkb.getAnnotationProperty(ontns + "searchConfig");
    
    for(KBObject prop : this.ontkb.getAllObjectProperties()) {
      KBObject domcls = this.ontkb.getPropertyDomain(prop);
      KBObject rangecls = this.ontkb.getPropertyRange(prop);
      KBObject catobj = this.ontkb.getPropertyValue(prop, catprop);
      KBObject reqobj = this.ontkb.getPropertyValue(prop, reqprop);
      KBObject quesobj = this.ontkb.getPropertyValue(prop, quesprop);
      KBObject uiconfval = this.ontkb.getPropertyValue(prop, uiprop);
      KBObject searchconfval = this.ontkb.getPropertyValue(prop, searchprop);
      boolean required = reqobj != null && Boolean.parseBoolean(reqobj.getValue().toString());
      boolean multiple = !this.ontkb.isFunctionalProperty(prop);
      
      MetadataProperty mprop = new MetadataProperty();
      mprop.setId(prop.getID());
      mprop.setName(prop.getName());
      
      String label = this.createPropertyLabel(prop.getName());
      mprop.setLabel(label);
      
      if(quesobj != null && quesobj.getValue() != null)
        mprop.setQuestion(quesobj.getValue().toString());
      
      if(domcls != null)
        mprop.setDomain(domcls.getID());
      
      if(rangecls != null)
        mprop.setRange(rangecls.getID());
      
      if(catobj != null)
        mprop.setCategory(catobj.getID());
      
      mprop.setRequired(required);
      mprop.setMultiple(multiple);
      
      mprop.setPlugins(PluginRegistrar.getPluginsForProperty(prop.getID()));
      
      if(uiconfval != null && uiconfval.getValue() != null) {
        String uiconfstr = uiconfval.getValue().toString();
        try {
          UIConfig uiconf = mapper.readValue(uiconfstr, UIConfig.class);
          mprop.setUiConfig(uiconf);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      if(searchconfval != null && searchconfval.getValue() != null) {
        String searchconfstr = searchconfval.getValue().toString();
        try {
          SearchConfig searchconf = mapper.readValue(searchconfstr, SearchConfig.class);
          mprop.setSearchConfig(searchconf);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      this.vocabulary.addProperty(mprop);
    }
  }
  
  
  /**
   * Entity Registration
   */
  private void registerEntityAdapters() {
    // Register all entity adapters
    EntityRegistrar.clear();
    KBObject entityobj = this.ontkb.getConcept(KBConstants.PROVNS() + "Entity");
    this.registerEntityAdapters(entityobj, null);
  }
  
  @SuppressWarnings("unchecked")
  private void registerEntityAdapters(KBObject clsobj, 
      Class<IEntityAdapter> parentAdapter) {
    String className = IEntityAdapter.class.getPackage().getName() + "." + clsobj.getName() + "Adapter";
    Class<IEntityAdapter> adapterClass = parentAdapter;
    try {
      adapterClass = (Class<IEntityAdapter>) Class.forName(className);
    } catch (ClassNotFoundException e) {}
    
    if(adapterClass != null) {
      //System.out.println("Registering " + clsobj.getName() + " to " + adapterClass);
      EntityRegistrar.register(clsobj.getID(), adapterClass);
    }
    else {
      System.out.println("** Cannot find adapter for "+clsobj.getID());
    }
    for(KBObject childclsobj : this.ontkb.getSubClasses(clsobj, true)) {
      if(!childclsobj.getNamespace().equals(owlns))
        this.registerEntityAdapters(childclsobj, adapterClass);
    }
  }
  
  
  /**
   * Adding new software
   * 
   * @param sw Software
   * @return
   * @throws Exception
   */
  public String addSoftware(Software sw, User user) throws Exception {
    if(sw.getId() == null) 
      sw.setId(this.LIBNS() + "Software-" + GUID.get());
    
    if(sw.getType() == null)
      sw.setType(topclass);
    
    // First Look for existing software with same label
    for(MetadataEnumeration menum : enumerations.get(topclass)) {
      if(menum.getLabel().equalsIgnoreCase(sw.getLabel())) {
        sw.setId(menum.getId());
        return sw.getId();
      }
    }
    String swid = updateOrAddSoftware(sw, user, false);
    if(swid != null)  {
      Provenance prov = this.prov.getAddProvenance(sw, user);
      this.prov.addProvenance(prov);
      Permission perm = this.perm_repo.createSoftwarePermisson(sw.getId(), user);
      this.perm_repo.commitPermission(perm, sw.getId());
    }
    return swid;
  }
  
  public String createEntityId(String swid, Entity entity) {
    MetadataType type = vocabulary.getType(entity.getType());
    // Treat software entities specially 
    if(vocabulary.isA(type, vocabulary.getType(topclass)))
        return GUID.randomEntityId(swid, entity.getType());
    return GUID.randomEntityId(swid, entity.getType());
  }
  
  private String updateOrAddSoftware(Software sw, User user, boolean update) throws Exception {
    boolean isModerator = false;
    Boolean permFetureEnabled = getPermissionFeatureEnabled();
    
    if (update) {
      String accesslevel = PermUtils.getAccessLevelForUser(sw, user.getName(), sw.getId());
      if (user.getRoles().contains("admin") || 
          PermUtils.hasOwnerAccess(sw.getPermission(), user.getName())
          || accesslevel.equals("Write")) {
        isModerator = true;
      }
    }

    KBAPI swkb = fac.getKB(sw.getId(), OntSpec.PLAIN, true);
    String swtype = sw.getType();
    if(swtype == null)
      swtype = topclass;
    KBObject swcls = this.ontkb.getConcept(swtype);

    KBObject swobj = update ? swkb.getIndividual(sw.getId())
        : swkb.createObjectOfClass(sw.getId(), swcls);
    
    if(swobj == null)
      return null;
    
    if(sw.getLabel() != null)
      swkb.setLabel(swobj, sw.getLabel());
    
    for(String propid : sw.getValue().keySet()) {
      if (!permFetureEnabled ||
          !update            || 
          isModerator        || 
          PermUtils.getAccessLevelForUser(sw, user.getName(), propid).equals("Write")) {
        KBObject swprop = this.ontkb.getProperty(propid);
        if (swprop != null) {
          List<Entity> entities = sw.getValue().get(propid);
          MetadataProperty prop = vocabulary.getProperty(swprop.getID());
	        
          // Remove existing property values if any
          if (update) {
            for(KBTriple t : swkb.genericTripleQuery(swobj, swprop, null))
              swkb.removeTriple(t);
          }
	      
          for(Entity entity: entities) {
            MetadataType type = vocabulary.getType(entity.getType());
	          
            // Treat software entities specially 
            if(vocabulary.isA(type, vocabulary.getType(topclass))) {
              if(!this.hasSoftware(entity.getId())) {
                Software subsw = new Software();
                subsw.setId(entity.getId());
                subsw.setLabel((String)entity.getValue());
                subsw.setType(entity.getType());
                String swid = this.addSoftware(subsw, user);
                entity.setId(swid);
              }
	
              KBObject swval = swkb.getResource(entity.getId());
              swkb.addPropertyValue(swobj, swprop, swval);
              continue;
            }
	          
            // Get entity adapter for class
            IEntityAdapter adapter = EntityRegistrar.getAdapter(swkb, ontkb, enumkb, prop.getRange());
            if(adapter != null) {
              if(entity.getId() == null) {
                entity.setId(GUID.randomEntityId(sw.getId(), entity.getType()));
              }
              if(adapter.saveEntity(entity)) {
                KBObject entityobj = swkb.getIndividual(entity.getId());
                if(entityobj == null)
                  entityobj = ontkb.getIndividual(entity.getId());
                if(entityobj == null)
                  entityobj = enumkb.getIndividual(entity.getId());
                if(entityobj != null)
                  swkb.addPropertyValue(swobj, swprop, entityobj);
              }
            } else {
              System.out.println("No adapter registered for type: "+entity.getType());
            }
          }
        }
      }
    }
    
    if(swkb.save() && enumkb.save()) {
      if(!update) {
        MetadataEnumeration menum = new MetadataEnumeration();
        menum.setId(sw.getId());
        menum.setLabel(sw.getLabel());
        menum.setType(sw.getType());
        menum.setName(sw.getName());
        addEnumerationToVocabulary(menum);
        //vocabulary.setNeedsReload(true);
      }
      return sw.getId();
    }
    return null;    
  }
  
  
  /**
   * Updating software (for now just deleting old and adding new)
   * @param sw
   * @param swid
   * @return
   * @throws Exception
   */
  public boolean updateSoftware(Software newsw, String swid, User user) 
      throws Exception {  
    Software cursw = this.getSoftware(swid);
    
    Provenance prov = this.prov.getUpdateProvenance(cursw, newsw, user);
    String nswid = this.updateOrAddSoftware(newsw, user, true);
    if(nswid != null) {
       this.prov.addProvenance(prov);
       return true;
    }
    return false;
  }
  
  // TODO: Change this call. Make it more efficient than
  //       crawling through the whole union graph ?
  public ArrayList<SoftwareSummary> getAllSoftware() throws Exception {
    return this.getAllSoftwareWithFacets(null);
  }
  
  public ArrayList<SoftwareSummary> getAllSoftwareWithFacets(
      List<EnumerationFacet> facets) throws Exception {
    /*if(facets == null || facets.size() == 0)
      return getAllSoftware();*/
    
    String facetquery = "";
    if(facets != null) {
      for(EnumerationFacet facet : facets) {
        int i=0;
        int num = facet.getEnumerationIds().size();
        if(num > 0) {
          facetquery += "\t {\n";
          for(String propid : facet.getPropertyIds()) {
            for(String enumid : facet.getEnumerationIds()) {
              if(i > 0)
                facetquery += "\t\t UNION\n";
              facetquery += "\t\t { ?x <"+propid+"> <"+enumid+"> }\n";
              i++;
            }
          }
          facetquery += "\t } .\n";
        }
      }
    }
    String ons = KBConstants.ONTNS();
    String pns = KBConstants.PROVNS();
    String swquery = "\t ?x a <" + ons +"Software> .\n"
                   + "\t OPTIONAL {\n"
                   + "\t\t ?x <" + ons + "hasShortDescription> ?dobj .\n"
                   + "\t\t ?dobj <" + ons + "hasTextValue> ?desc \n"
                   + "\t } .\n"
                   + "\t OPTIONAL {\n"
                   + "\t\t ?x <" + ons + "hasCreator> ?creator .\n"
                   + "\t } .\n"
                   + "\t ?x <" + pns + "wasGeneratedBy> ?act .\n"
                   + "\t ?act <" + pns + "wasAssociatedWith> ?agent .\n"
                   + "\t ?act <" + pns + "endedAtTime> ?time .\n"
                   + "\t FILTER (STRSTARTS(STR(?act), CONCAT(STR(?x), '/" + ProvenanceRepository.PROV_GRAPH + "')))";
    String query = "SELECT ?x (SAMPLE(?desc) as ?description) "
        + " (GROUP_CONCAT(?creator) as ?creators)"
        + " (SAMPLE(?agent) as ?user)"
        + " (SAMPLE(?time) as ?posttime)"
        + " WHERE {\n" + swquery + facetquery + "}"
        + " GROUP BY ?x\n";
    
    ArrayList<SoftwareSummary> list = new ArrayList<SoftwareSummary>();
    KBAPI allkb = fac.getKB(uniongraph, OntSpec.PLAIN);
    for(ArrayList<SparqlQuerySolution> soln : allkb.sparqlQuery(query)) {
      KBObject sw = soln.get(0).getObject();
      KBObject desc = soln.get(1).getObject();
      KBObject creator = soln.get(2).getObject();
      KBObject agent = soln.get(3).getObject();
      KBObject time = soln.get(4).getObject();
      
      if(sw == null)
        continue;
      
      SoftwareSummary summary = new SoftwareSummary();
      summary.setId(sw.getID());
      summary.setName(sw.getName());
      summary.setLabel(allkb.getLabel(sw));
      summary.setType(topclass);
      summary.setPermission(this.perm_repo.getSoftwarePermission(sw.getID()));
      
      if(desc != null && desc.getValue() != null) {
        String description = desc.getValue().toString();
        description = Pattern.compile("\\n\\nInitial metadata was retrieved .*$", Pattern.DOTALL).
          matcher(description).replaceAll("");
        if(description.length() > 300)
          description = description.substring(0, 297) + "...";
        summary.setDescription(description);
      }
      if(creator != null && creator.getValue() != null) {
        List<String> authors = new ArrayList<String>();
        for(String creatorid : creator.getValue().toString().split("\\s")) {
          KBObject authobj = allkb.getResource(creatorid);
          authors.add(allkb.getLabel(authobj));
        }
        summary.setAuthors(authors);
      }
      if(agent != null)
        summary.setUser(agent.getName());
      if(time != null && time.getValue() != null) {
        Date timestamp = (Date)time.getValue();
        summary.setTime(timestamp.getTime());
      }
      list.add(summary);
    }
    return list;
  }
  
  public Vocabulary getVocabulary() {
    return this.vocabulary;
  }
  
  public Software getSoftware(String swid) throws Exception {
    KBAPI swkb = fac.getKB(swid, OntSpec.PLAIN);
    KBObject swobj = swkb.getIndividual(swid);
    if(swobj != null) {
      Software sw = new Software();
      sw.setId(swid);
      sw.setLabel(swkb.getLabel(swobj));
      sw.setName(swobj.getName());
      
      KBObject typeobj = swkb.getPropertyValue(swobj, ontkb.getProperty(rdfns+"type"));
      sw.setType(typeobj.getID());

      MetadataType swtype = this.vocabulary.getType(sw.getType());
      
      for(MetadataProperty prop : this.vocabulary.getPropertiesForType(swtype)) {
        
        KBObject propobj = swkb.getProperty(prop.getId());
        ArrayList<Entity> entities = new ArrayList<Entity>();
        for(KBObject valobj: swkb.getPropertyValues(swobj, propobj)) {
          
          MetadataType type = vocabulary.getType(prop.getRange());
          // Treat software entities specially 
          if(vocabulary.isA(type, vocabulary.getType(topclass))) {
            KBAPI tmpkb = fac.getKB(valobj.getID(), OntSpec.PLAIN);
            KBObject valswobj = tmpkb.getIndividual(valobj.getID());
            if(valswobj != null) {
              Entity entity = new EnumerationEntity();
              entity.setId(valobj.getID());
              entity.setLabel(tmpkb.getLabel(valswobj));
              entity.setType(prop.getRange());
              entities.add(entity);
            }
          }
          else {
            Entity entity = this.getSoftwareEntity(swkb, valobj, prop.getRange());
            if(entity != null)
              entities.add(entity);
          }
        }
        sw.addPropertyValues(prop.getId(), entities);
      }
      
      sw.setProvenance(this.prov.getSoftwareProvenance(swid));
      sw.setPermission(this.perm_repo.getSoftwarePermission(swid));
      return sw;
    }
    return null;
  }
  
  public Provenance getProvenance(String swid) throws Exception {
    return this.prov.getSoftwareProvenance(swid);
  }

  public Permission getSoftwarePermission(String swid) {
    try {
      return this.perm_repo.getSoftwarePermission(swid);
    } catch (Exception e) {
      return null;
    }
  }
  
  public String getSoftwarePermissionGraph(String swid) {
    try {
      return this.perm_repo.getSoftwarePermissionGraph(swid);
    } catch (Exception e) {
      return null;
    }
  }
  
  public AccessMode getSoftwareAccessLevelForUser(String swid, String username) {
    UserCredentials user = UserDatabase.get().getUser(username);
    AccessMode mode = new AccessMode();
    mode.setMode("Read");
    Software software = null;
    try {
      software = this.getSoftware(swid);
    } catch (Exception e) {
      mode.setMode("Read");
    }
    
    if (user != null && user.getRoles().contains("admin")) {
        mode.setMode("Write");
    } else if (software.getPermission().authExists(username, swid)) {
    	String level = PermUtils.getAccessLevelForUser(software, username, swid);
    	mode.setMode(level);
    } else if (software.getPermission().authExists("*", swid)) {
    	String level = PermUtils.getAccessLevelForUser(software, "*", swid);
    	mode.setMode(level);
    } else {
    	mode.setMode("Read");
    }
    
    return mode;
  }

  public AccessMode getPropertyAccessLevelForUser(String swid, String propid, String username) {
    UserCredentials user = UserDatabase.get().getUser(username);
    AccessMode mode = new AccessMode();
    mode.setMode("Read");
    Software software = null;
    try {
      software = this.getSoftware(swid);
    } catch (Exception e) {
      mode.setMode("Read");
    }
    
    if (user != null && user.getRoles().contains("admin")) {
      mode.setMode("Write");
    } else if (software.getPermission().authExists(username, propid)) {
      String level = PermUtils.getAccessLevelForUser(software, username, propid);
      mode.setMode(level);
    } else if (software.getPermission().authExists("*", propid)) {
      String level = PermUtils.getAccessLevelForUser(software, "*", propid);
      mode.setMode(level);
    } else {
      mode.setMode("Read");
    }

    return mode;
  }
  
  public List<String> getPermissionTypes() {
    return this.perm_repo.getPermissionTypes();
  }
  
  public Boolean setSoftwarePermissionForUser(User loggedinuser, Authorization authorization) {	  
    String swid = authorization.getAccessToObjId();
    String username = authorization.getAgentName();
    String accessmodeid = authorization.getAccessMode().getId();

    boolean updated = false;

    try {
      Permission perm = getSoftwarePermission(swid);
      if (loggedinuser.getRoles().contains("admin") || 
        PermUtils.hasOwnerAccess(perm, loggedinuser.getName())) {
        String permns = perm.getId() + "#";
        if (username.equals(allusers)) {
          perm.removeAuthsHavingTarget(authorization.getAccessToObjId());
        }

        Map<String, Authorization> auths = perm.getAuthorizations();
        for (Authorization authobj : auths.values()) {
          if (authobj.getAgentName().equals(username) &&
            authobj.getAccessToObjId().equals(authorization.getAccessToObjId())) {
            AccessMode mode = new AccessMode();
            mode.setId(accessmodeid);
            authobj.setAccessMode(mode);
            updated = true;
          }
        }
        if (!updated) {
          authorization.setId(permns + "Auth-" + GUID.get());
          if (username.equals(allusers)) {
            authorization.setAgentId(allusers);    
          } else {
            UserCredentials user = UserDatabase.get().getUser(username);
            authorization.setAgentId(this.getUserId(user));        	  
          }

          perm.addAuth(authorization);
        }

        this.perm_repo.commitPermission(perm, swid);
        return true;
      }
    } catch (Exception e) {}
    return false;
  }

  public Boolean setPropertyPermissionForUser(User loggedinuser, String swid, Authorization authorization) {
    String username = authorization.getAgentName();
    String accessmodeid = authorization.getAccessMode().getId();

    boolean updated = false;

    try {
      Permission perm = getSoftwarePermission(swid);
      if (loggedinuser.getRoles().contains("admin") || 
        PermUtils.hasOwnerAccess(perm, loggedinuser.getName())) {
        String permns = perm.getId() + "#";
        if (username.equals(allusers)) {
          perm.removeAuthsHavingTarget(authorization.getAccessToObjId());
        }
        
        Map<String, Authorization> auths = perm.getAuthorizations();
        for (Authorization authobj : auths.values()) {
          if (authobj.getAgentName().equals(username) && 
            authobj.getAccessToObjId().equals(authorization.getAccessToObjId())) {
            AccessMode mode = new AccessMode();
            mode.setId(accessmodeid);
            authobj.setAccessMode(mode);
            updated = true;
          }
        }
        
        if (!updated) {
          authorization.setId(permns + "Auth-" + GUID.get());
          if (username.equals(allusers)) {
            authorization.setAgentId(allusers);    
          } else {
            UserCredentials user = UserDatabase.get().getUser(username);
            authorization.setAgentId(this.getUserId(user));        	  
          }
          perm.addAuth(authorization);
        }

        this.perm_repo.commitPermission(perm, swid);
        return true;
      }
    } catch (Exception e) {}
    return false;
  }
  
  public boolean addSoftwareOwner(User loggedinuser, String swid, String ownername) {
    try {
	  Permission perm = getSoftwarePermission(swid);
      if (loggedinuser.getRoles().contains("admin") || 
        PermUtils.hasOwnerAccess(perm, loggedinuser.getName())) {
        if (ownername.equals(allusers)) {
          perm.removeAllOwners();
          perm.addOwnerid(allusers);
        } else {
          UserCredentials user = UserDatabase.get().getUser(ownername);
          perm.addOwnerid(this.getUserId(user));          
        }
        
        this.perm_repo.commitPermission(perm, swid);
        return true;
      }
    } catch (Exception e) {}
    return false;
  }

  public boolean removeSoftwareOwner(User loggedinuser, String swid, String ownername) {
    try {
	  Permission perm = getSoftwarePermission(swid);
      if (loggedinuser.getRoles().contains("admin") || 
        PermUtils.hasOwnerAccess(perm, loggedinuser.getName())) {
        if (ownername.equals(allusers)) {
          if (perm.removeOwnerid(allusers)) {
            this.perm_repo.commitPermission(perm, swid);
          } 
        } else {
          UserCredentials user = UserDatabase.get().getUser(ownername);
          if (perm.removeOwnerid(this.getUserId(user))) {
            this.perm_repo.commitPermission(perm, swid);
          }        
        }

        return true;
      }
    } catch (Exception e) {}
    return false;
  }
  
  public String getProvenanceGraph(String swid) throws Exception {
    return this.prov.getSoftwareProvenanceGraph(swid);
  }
  
  public Boolean getPermissionFeatureEnabled() {
    PropertyListConfiguration props = Config.get().getProperties();
    String isenabled = props.getString("perm_feature_enabled");
    if(isenabled != null && isenabled.equals("true")) {
      return true;
    }
    return false;
  }
  
  public boolean hasSoftware(String swid) throws Exception {
    KBAPI swkb = fac.getKB(swid, OntSpec.PLAIN);
    KBObject swobj = swkb.getIndividual(swid);
    if(swobj != null) 
      return true;
    return false;
  }
  
  private Entity getSoftwareEntity(KBAPI swkb, KBObject entityobj, String clsid) {
    IEntityAdapter adapter = EntityRegistrar.getAdapter(swkb, ontkb, enumkb, clsid);
    if(adapter != null)
      return adapter.getEntity(entityobj.getID());
    return null;
  }

  public String serializeXML(String swid) throws Exception {
    KBAPI swkb = fac.getKB(swid, OntSpec.PLAIN);
    return swkb.toAbbrevRdf(true);
  }
  
  public String serializeJsonLD(String swid) throws Exception {
    KBAPI swkb = fac.getKB(swid, OntSpec.PLAIN);
    return swkb.toJson(swid);
  }
  
  public boolean deleteSoftware(String swid, User loggedinuser) throws Exception {
	Permission perm = getSoftwarePermission(swid);
	if (loggedinuser.getRoles().contains("admin") || PermUtils.hasOwnerAccess(perm,  loggedinuser.getName()))
	{
	    KBAPI swkb = fac.getKB(swid, OntSpec.PLAIN);
	    //KBObject swobj = swkb.getIndividual(swid);
	    if (swkb.delete()) { // && (swobj != null)) {
	      deleteEnumerationFromVocabulary(swid);
	      this.prov.deleteSoftwareProvenance(swid);
	      this.perm_repo.deleteSoftwarePermission(swid);
	      return true;
	    }		
	}
    return false;
  }
  
  public boolean deleteEnumeration(String enumid) throws Exception {
    KBObject enumobj = enumkb.getIndividual(enumid);
    if(enumobj != null) {
      enumkb.deleteObject(enumobj, true, false);
      deleteEnumerationFromVocabulary(enumid);
      //vocabulary.setNeedsReload(true);
      return true; 
    }
    return false;
  }
  
  public void deleteEnumerationFromVocabulary(String enumid) {
    for(List<MetadataEnumeration> enumlist : enumerations.values()) {
      MetadataEnumeration delenum = null;
      for(MetadataEnumeration menum : enumlist) {
        if(menum.getId().equals(enumid)) {
          delenum = menum;
          break;
        }
      }
      if(delenum != null)
        enumlist.remove(delenum);
    }
  }
  
  public void addEnumerationToVocabulary(MetadataEnumeration menum) {
    List<MetadataEnumeration> enumlist = enumerations.get(menum.getType());
    if(enumlist != null && !enumlist.contains(menum)) {
      enumlist.add(menum);
    }
  }
  
  private String getUserId(UserCredentials user) {
	return this.USERNS() + user.getName().replaceAll("[^a-zA-Z0-9_]", "_");    
  }
}
