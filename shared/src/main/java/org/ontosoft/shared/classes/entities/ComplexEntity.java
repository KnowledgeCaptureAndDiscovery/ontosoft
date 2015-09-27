package org.ontosoft.shared.classes.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class ComplexEntity extends EnumerationEntity {
  @JsonDeserialize(converter=ComplexEntityDeserializer.class)
  protected Map<String, List<Entity>> value;
  
  public ComplexEntity() {
    value = new TreeMap<String, List<Entity>>();
  }
  
  @Override
  public Map<String, List<Entity>> getValue() {
    return value;
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Object value) {
    if (value != null && value instanceof Map)
      this.value = (Map<String, List<Entity>>) value;
  }
  
  public Entity getPropertyValue(String propertyId) {
    List<Entity> entities = this.getPropertyValues(propertyId);
    if(entities != null && entities.size() > 0)
      return entities.get(0);
    return null;
  }

  public List<Entity> getPropertyValues(String propertyId) {
    return this.value.get(propertyId);
  }

  public void addPropertyValues(String prop, List<Entity> entities) {
    this.value.put(prop, entities);
  }

  public void setPropertyValue(String propertyId, Entity entity) {
    List<Entity> entities = this.getPropertyValues(propertyId);
    if(entities == null)
      entities = new ArrayList<Entity>();
    entities.clear();
    entities.add(entity);
    this.value.put(propertyId, entities);
  }

  public void addPropertyValue(String prop, Entity entity) {
    List<Entity> entities = this.value.get(prop);
    if(entities == null)
      entities = new ArrayList<Entity>();
    entities.add(entity);
    this.value.put(prop, entities);
  }
  
  public void updatePropertyValue(String prop, Entity entity) {
    List<Entity> entities = this.value.get(prop);
    if(entities == null) {
      // No existing entities
      entities = new ArrayList<Entity>();
      // This is a new entity (check if it is valid)
      if(entity.getValue() != null)
        entities.add(entity);
    }
    else {
      // Entity already exists for the property
      Entity curentity = null;
      for(Entity e : entities) {
        if(e.getId().equals(entity.getId())) {
          curentity = e;
          break;
        }
      }
      if(curentity == null) {
        // This is a new entity (check if it is valid)
        if(entity.getValue() != null)
          entities.add(entity);
      }
      else {
        if(entity.getValue() != null) {
          // Replace the existing entity
          int index = entities.indexOf(curentity);
          entities.add(index, entity);
          entities.remove(curentity);
        }
        else {
          // Remove the current entity
          entities.remove(curentity);
        }
      }
    }
    this.value.put(prop, entities);
  }
}
