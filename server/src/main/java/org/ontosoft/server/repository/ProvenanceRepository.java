package org.ontosoft.server.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.ontosoft.server.users.User;
import org.ontosoft.server.util.Config;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.provenance.Activity;
import org.ontosoft.shared.classes.provenance.Agent;
import org.ontosoft.shared.classes.provenance.ProvEntity;
import org.ontosoft.shared.classes.provenance.Provenance;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.util.KBConstants;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;
import edu.isi.wings.ontapi.KBTriple;
import edu.isi.wings.ontapi.OntFactory;
import edu.isi.wings.ontapi.OntSpec;

public class ProvenanceRepository {
  OntFactory fac;
  KBAPI ontkb;
  
  String ontns, owlns, rdfns, rdfsns;
  String server;

  private String PROVURI(String swid) {
    return swid + "/provenance";
  }

  private String USERURI() {
    return server.replaceAll("\\/$", "") + "/users/"; 
  }
  
  private String USERNS() {
    return USERURI();
  }
  
  public ProvenanceRepository() {
    initializeKB();
  }
  
  private void initializeKB() {
    PropertyListConfiguration props = Config.get().getProperties();
    this.server = props.getString("server");

    String tdbdir = props.getString("storage.tdb");
    File tdbdirf = new File(tdbdir);
    if(!tdbdirf.exists() && !tdbdirf.mkdirs()) {
      System.err.println("Cannot create tdb directory : "+tdbdirf.getAbsolutePath());
    }

    owlns = KBConstants.OWLNS();
    rdfns = KBConstants.RDFNS();
    rdfsns = KBConstants.RDFSNS();
    ontns = KBConstants.PROVNS();

    this.fac = new OntFactory(OntFactory.JENA, tdbdir);
    try {
      this.ontkb = fac.getKB(KBConstants.PROVURI(), OntSpec.PLAIN, false, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public Provenance getSoftwareProvenance(String swid) throws Exception {
    Provenance prov = new Provenance();
    String provid = PROVURI(swid);
    prov.setId(provid);
    
    KBAPI provkb = fac.getKB(provid, OntSpec.PLAIN);
    
    KBObject agentcls = ontkb.getConcept(ontns + "Agent");
    KBObject activitycls = ontkb.getConcept(ontns + "Activity");
    KBObject entitycls = ontkb.getConcept(ontns + "Entity");
    
    KBObject agentprop = ontkb.getProperty(ontns + "wasAssociatedWith");
    KBObject actprop = ontkb.getProperty(ontns + "wasGeneratedBy");
    KBObject timeprop = ontkb.getProperty(ontns + "endedAtTime");
    KBObject revprop = ontkb.getProperty(ontns + "wasRevisionOf");
    KBObject invprop = ontkb.getProperty(ontns + "wasInvalidatedBy");
    KBObject typeprop = ontkb.getProperty(rdfns+"type");
    
    // Get agents
    for(KBTriple t : provkb.genericTripleQuery(null, typeprop, agentcls)) {
      KBObject agentobj = t.getSubject();
      Agent agent = new Agent();
      agent.setId(agentobj.getID());
      prov.addAgent(agent);
    }

    // Get activities
    for(KBTriple t : provkb.genericTripleQuery(null, typeprop, activitycls)) {
      KBObject activityobj = t.getSubject();
      Activity activity = new Activity();
      activity.setId(activityobj.getID());
      
      KBObject agentobj = provkb.getPropertyValue(activityobj, agentprop);
      if(agentobj != null)
        activity.setAgentId(agentobj.getID());
      
      KBObject timeobj = provkb.getPropertyValue(activityobj, timeprop);
      if(timeobj != null && timeobj.getValue() != null) {
        Date time = (Date)timeobj.getValue();
        activity.setTime(time);
      }
      prov.addActivity(activity);
    }
    
    // Get entities
    for(KBTriple t : provkb.genericTripleQuery(null, typeprop, entitycls)) {
      KBObject entityobj = t.getSubject();
      ProvEntity entity = new ProvEntity();
      entity.setId(entityobj.getID());
      
      KBObject activityobj = provkb.getPropertyValue(entityobj, actprop);
      if(activityobj != null)
        entity.setGeneratedBy(activityobj.getID());
      
      KBObject revobj = provkb.getPropertyValue(entityobj, revprop);
      if(revobj != null)
        entity.setRevisionOf(revobj.getID());
      
      KBObject invobj = provkb.getPropertyValue(entityobj, invprop);
      if(invobj != null)
        entity.setInvalidatedBy(invobj.getID());
      
      prov.addEntity(entity);
    }
    
    return prov;
  }
  
  public String getSoftwareProvenanceGraph(String swid) throws Exception {
    KBAPI provkb = fac.getKB(PROVURI(swid), OntSpec.PLAIN);
    return provkb.toAbbrevRdf(true);
  }
  
  public Provenance getAddProvenance(Software sw, User user) throws Exception {
    Provenance prov = new Provenance();
    prov.setId(PROVURI(sw.getId()));
    String provns = prov.getId() + "#";
    
    Agent agent = new Agent();
    agent.setId(getUserId(user));
    prov.addAgent(agent);
    
    Activity activity = new Activity();
    activity.setId(provns + "Create-" + GUID.get());
    activity.setTime(new Date());
    activity.setAgentId(agent.getId());
    prov.addActivity(activity);
    
    ProvEntity swentity = new ProvEntity();
    swentity.setId(sw.getId());
    swentity.setGeneratedBy(activity.getId());
    prov.addEntity(swentity);
    
    Map<String, List<Entity>> propEntities = sw.getValue();
    for(String propid : propEntities.keySet()) {
      List<Entity> entities = propEntities.get(propid);
      for(Entity entity : entities) {
        ProvEntity pentity = new ProvEntity();
        pentity.setId(entity.getId());
        pentity.setGeneratedBy(activity.getId());
        prov.addEntity(pentity);
      }
    }
    
    return prov;
  }
  
  public boolean deleteSoftwareProvenance(String swid) throws Exception {
    KBAPI provkb = fac.getKB(PROVURI(swid), OntSpec.PLAIN, true);
    return provkb.delete();
  }
  
  public boolean addProvenance(Provenance prov) throws Exception {
    KBAPI provkb = fac.getKB(prov.getId(), OntSpec.PLAIN, true);
    
    KBObject agentcls = ontkb.getConcept(ontns + "Agent");
    KBObject activitycls = ontkb.getConcept(ontns + "Activity");
    KBObject entitycls = ontkb.getConcept(ontns + "Entity");
    
    KBObject agentprop = ontkb.getProperty(ontns + "wasAssociatedWith");
    KBObject actprop = ontkb.getProperty(ontns + "wasGeneratedBy");
    KBObject timeprop = ontkb.getProperty(ontns + "endedAtTime");
    KBObject revprop = ontkb.getProperty(ontns + "wasRevisionOf");
    KBObject invprop = ontkb.getProperty(ontns + "wasInvalidatedBy");
    
    // Add agents
    for(Agent agent : prov.getAgents().values()) {
      KBObject agentobj = provkb.getIndividual(agent.getId());
      if(agentobj == null) {
        agentobj = provkb.createObjectOfClass(agent.getId(), agentcls);
      }
    }
    // Add activities
    for(Activity activity : prov.getActivities().values()) {
      KBObject activityobj = provkb.getIndividual(activity.getId());
      if(activityobj == null) {
        activityobj = provkb.createObjectOfClass(activity.getId(), activitycls);
        
        KBObject agentobj = provkb.getIndividual(activity.getAgentId());
        if(agentobj != null)
          provkb.setPropertyValue(activityobj, agentprop, agentobj);
        
        KBObject timeobj = provkb.createLiteral(activity.getTime());
        if(timeobj != null)
          provkb.setPropertyValue(activityobj, timeprop, timeobj);
      }
    }
    // Add entities
    for(ProvEntity entity : prov.getEntities().values()) {
      KBObject entityobj = provkb.getIndividual(entity.getId());
      if(entityobj == null)
        entityobj = provkb.createObjectOfClass(entity.getId(), entitycls);
      
      if(entity.getGeneratedBy() != null) {
        KBObject activityobj = provkb.getIndividual(entity.getGeneratedBy());
        if (activityobj != null)
          provkb.setPropertyValue(entityobj, actprop, activityobj);
      }
      if (entity.getRevisionOf() != null) {
        KBObject reventityobj = provkb.getIndividual(entity.getRevisionOf());
        if (reventityobj != null)
          provkb.setPropertyValue(entityobj, revprop, reventityobj);
      }
      if (entity.getInvalidatedBy() != null) {
        KBObject invactivityobj = provkb.getResource(entity.getInvalidatedBy());
        if (invactivityobj != null)
          provkb.setPropertyValue(entityobj, invprop, invactivityobj);
      }
    }
    return provkb.save();
  }
  
  private String getUserId(User user) {
    return this.USERNS() + user.getName().replaceAll("[^a-zA-Z0-9_]", "_");    
  }
  
  public Provenance getUpdateProvenance(Software cursw, Software newsw,
      User user) throws Exception {
    // Compare softwares
    Provenance prov = new Provenance();
    prov.setId(PROVURI(cursw.getId()));
    String provns = prov.getId() + "#";
    
    Agent agent = new Agent();
    agent.setId(getUserId(user));
    prov.addAgent(agent);
    
    Activity activity = new Activity();
    activity.setId(provns + "Update-" + GUID.get());
    activity.setTime(new Date());
    activity.setAgentId(agent.getId());
    prov.addActivity(activity);
    

    Map<String, List<Entity>> curPropEntities = cursw.getValue();
    Map<String, List<Entity>> newPropEntities = newsw.getValue();
    for(String propid : curPropEntities.keySet()) {
      List<Entity> curEntities = curPropEntities.get(propid);
      List<Entity> newEntities = newPropEntities.get(propid);
      
      Map<Entity, Entity> modified = new HashMap<Entity, Entity>();
      List<Entity> added = new ArrayList<Entity>();
      List<Entity> deleted = new ArrayList<Entity>();
      
      for(Entity newEntity : newEntities) {
        // If this entity id is not found in current entities
        boolean found = false;
        for(Entity curEntity : curEntities) {
          if(newEntity.getId().equals(curEntity.getId())) {
            found = true;
            if(!newEntity.getValue().toString().equals(curEntity.getValue().toString())) {
              modified.put(curEntity, newEntity);
            }
          }
        }
        if(!found) {
          added.add(newEntity);
        }
      }
      
      for(Entity curEntity : curEntities) {
        // If this entity id is not found in current entities
        boolean found = false;
        for(Entity newEntity : newEntities) {
          if(newEntity.getId().equals(curEntity.getId()))
            found = true;
        }
        if(!found) {
          deleted.add(curEntity);
        }
      }

      // Add entity details to provenance
      
      // Added entities
      for(Entity e : added) {
        ProvEntity pentity = new ProvEntity();
        pentity.setId(e.getId());
        pentity.setGeneratedBy(activity.getId());
        prov.addEntity(pentity);
      }
      // Modified entities
      for(Entity oldEntity : modified.keySet()) {
        Entity newEntity = modified.get(oldEntity);
        String newid = SoftwareRepository.get().createEntityId(newsw.getId(), newEntity);
        newEntity.setId(newid);
        
        ProvEntity pentity = new ProvEntity();
        pentity.setId(newid);
        pentity.setGeneratedBy(activity.getId());
        pentity.setRevisionOf(oldEntity.getId());
        prov.addEntity(pentity);
      }
      // Deleted entities
      for(Entity e : deleted) {
        ProvEntity pentity = new ProvEntity();
        pentity.setId(e.getId());
        pentity.setInvalidatedBy(activity.getId());
        prov.addEntity(pentity);
      }
    }
    return prov;
  }
  
}
