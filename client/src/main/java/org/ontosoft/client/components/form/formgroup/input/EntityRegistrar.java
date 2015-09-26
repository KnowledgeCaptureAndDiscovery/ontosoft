package org.ontosoft.client.components.form.formgroup.input;

import java.util.HashMap;

import org.ontosoft.client.generator.EntityFactory;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.shared.GWT;

public class EntityRegistrar {
  private static EntityFactory entityFactory = GWT.create(EntityFactory.class);
  
  private static HashMap<String, String> inputClasses = new HashMap<String, String>();
  private static HashMap<String, String> entityClasses = new HashMap<String, String>();
  
  public static boolean registerInputClass(String typeid, String inputClass) {
    if(entityFactory.hasClass(inputClass)) {
      inputClasses.put(typeid, inputClass);
      return true;
    }
    return false;
  }
  
  public static boolean registerEntityClass(String typeid, String entityClass) {
    if(entityFactory.hasClass(entityClass)) {
      entityClasses.put(typeid, entityClass);
      return true;
    }
    return false;
  }
  
  public static IEntityInput getInput(Entity entity, MetadataProperty mprop, Vocabulary vocabulary) 
      throws Exception {
    String inputClass = inputClasses.get(mprop.getRange());
    if(inputClass != null) {
      Object item = entityFactory.instantiate(inputClass);
      if(item == null) {
        GWT.log("Cannot instantiate input for "+mprop.getRange());
        throw new Exception("Cannot instantiate input for "+mprop.getRange());
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
  
  public static Entity getEntity(String id, Object value, MetadataProperty mprop) 
      throws Exception {
    String entityClass = entityClasses.get(mprop.getRange());
    if(entityClass != null) {
      Object item = entityFactory.instantiate(entityClass);
      if(item == null) {
        GWT.log("Cannot instantiate entity for "+mprop.getRange());
        throw new Exception("Cannot instantiate entity for "+mprop.getRange());
      }
      else if(item instanceof Entity) {
        Entity entity = (Entity) item;
        entity.setId(id);
        entity.setType(mprop.getRange());
        if(value != null)
          entity.setValue(value);
        return entity;
      }
      else {
        GWT.log("Item not an extension of Entity");
        throw new Exception("Item not an extension of Entity");
      }
    }
    return null;
  }
}
