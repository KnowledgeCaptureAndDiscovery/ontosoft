package org.ontosoft.server.repository.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.ontosoft.server.repository.EntityUtilities;
import org.ontosoft.shared.classes.Entity;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class ComplexEntityAdapter extends EntityAdapter {
  protected ArrayList<KBObject> properties;
  
  public ComplexEntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String clsid) {
    super(kb, ontkb, enumkb, clsid, null);
    properties = ontkb.getPropertiesOfClass(this.entityClass, true);
  }

  @Override
  public Entity getEntity(String id) {
    KBObject entityobj = this.kb.getIndividual(id);

    HashMap<String, List<Entity>> subentities = new HashMap<String, List<Entity>>();
    for(KBObject propobj: properties) {
      List<Entity> entities = new ArrayList<Entity>();
      KBObject range = this.ontkb.getPropertyRange(propobj);
      for(KBObject valobj: kb.getPropertyValues(entityobj, propobj)) {
        IEntityAdapter adapter = EntityRegistrar.getAdapter(kb, ontkb, enumkb, range.getID());
        if(adapter != null) {
          Entity entity = adapter.getEntity(valobj.getID());
          if(entity != null)
            entities.add(entity);
        }
      }
      subentities.put(propobj.getID(), entities);
    }
    if(subentities.keySet().size() > 0)
      return new Entity(entityobj.getID(), subentities, this.entityClass.getID());

    return null;
  }

  
  @Override
  @SuppressWarnings("unchecked")
  public boolean saveEntity(Entity entity) {
    KBObject entityobj = this.kb.createObjectOfClass(entity.getId(), entityClass);
    
    HashMap<String, List<LinkedHashMap<String, Object>>> subentitypropvals = 
        (HashMap<String, List<LinkedHashMap<String, Object>>>) entity.getValue();
    
    for(String propid : subentitypropvals.keySet()) {
      KBObject swprop = this.ontkb.getProperty(propid);
      if (swprop != null) {
        List<LinkedHashMap<String, Object>> subentityhashes = subentitypropvals.get(propid);
        
        for(LinkedHashMap<String, Object> subentityhash: subentityhashes) {
          Entity subentity = new Entity(
              (String)subentityhash.get("id"), 
              subentityhash.get("value"),
              (String)subentityhash.get("type"));
          
          // Get entity adapter for class
          IEntityAdapter adapter = EntityRegistrar.getAdapter(kb, ontkb, enumkb, subentity.getType());
          if(adapter != null) {
            if(subentity.getId() == null) {
              subentity.setId(entity.getId() + "-" + EntityUtilities.shortUUID());
            }
            if(adapter.saveEntity(subentity)) {
              KBObject subentityobj = kb.getIndividual(subentity.getId());
              kb.addPropertyValue(entityobj, swprop, subentityobj);
            }
          } else {
            System.out.println("No adapter registered for type: "+entity.getType());
            return false;
          }
        }
      }
    }
    return true;

  }
}
