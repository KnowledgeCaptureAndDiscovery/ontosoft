package org.ontosoft.server.repository.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ontosoft.shared.classes.entities.ComplexEntity;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.util.GUID;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class ComplexEntityAdapter extends EntityAdapter {
  protected ArrayList<KBObject> properties;
  
  public ComplexEntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String clsid) {
    super(kb, ontkb, enumkb, clsid, null, ComplexEntity.class);
    properties = ontkb.getPropertiesOfClass(this.kbClass, true);
  }

  @Override
  public Entity getEntity(String id) {
    KBObject entityobj = this.kb.getIndividual(id);
    ComplexEntity entity = (ComplexEntity) super.getEntity(id);
    
    HashMap<String, List<Entity>> subentities = new HashMap<String, List<Entity>>();
    for(KBObject propobj: properties) {
      List<Entity> entities = new ArrayList<Entity>();
      KBObject range = this.ontkb.getPropertyRange(propobj);
      for(KBObject valobj: kb.getPropertyValues(entityobj, propobj)) {
        IEntityAdapter adapter = EntityRegistrar.getAdapter(kb, ontkb, enumkb, range.getID());
        if(adapter != null) {
          Entity subentity = adapter.getEntity(valobj.getID());
          if(subentity != null)
            entities.add(subentity);
        }
      }
      subentities.put(propobj.getID(), entities);
    }
    entity.setValue(subentities);
    
    if(subentities.keySet().size() > 0)
      //entity.setValue(null);
    	return entity;

    return null;
  }

  
  @Override
  @SuppressWarnings("unchecked")
  public boolean saveEntity(Entity entity) {
    KBObject entityobj = this.kb.createObjectOfClass(entity.getId(), kbClass);
    
    HashMap<String, List<Entity>> subentitypropvals = 
        (HashMap<String, List<Entity>>) entity.getValue();
    
    for(String propid : subentitypropvals.keySet()) {
      KBObject swprop = this.ontkb.getProperty(propid);
      if (swprop != null) {
        List<Entity> subentityhashes = subentitypropvals.get(propid);
        
        for(Entity subentity: subentityhashes) {
          // Get entity adapter for class
          IEntityAdapter adapter = EntityRegistrar.getAdapter(kb, ontkb, enumkb, subentity.getType());
          if(adapter != null) {
            if(subentity.getId() == null) {
              subentity.setId(entity.getId() + "-" + GUID.get());
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
