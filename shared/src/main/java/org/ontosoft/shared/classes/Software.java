package org.ontosoft.shared.classes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ontosoft.shared.classes.Entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Software {
  String id;
  String name;
  String type;
  String label;
  Map<String, List<Entity>> propertyValues;
  
  @JsonIgnore
  boolean dirty;

  public Software() {
    this.propertyValues = new HashMap<String, List<Entity>>();
  }
  
  public Software(String id) {
    this.id = id;
    this.propertyValues = new HashMap<String, List<Entity>>();
  }

  @JsonCreator
  public Software(@JsonProperty("id") String id,
      @JsonProperty("propertyValues") Map<String, List<Entity>> propertyValues) {
    this.id = id;
    this.propertyValues = propertyValues;
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public Entity getPropertyValue(String propertyId) {
    List<Entity> entities = this.getPropertyValues(propertyId);
    if(entities != null && entities.size() > 0)
      return entities.get(0);
    return null;
  }

  public List<Entity> getPropertyValues(String propertyId) {
    return propertyValues.get(propertyId);
  }
  
  public Map<String, List<Entity>> getPropertyValues() {
    return propertyValues;
  }

  public void setPropertyValues(Map<String, List<Entity>> propertyValues) {
    this.propertyValues = propertyValues;
  }
  
  public void addPropertyValues(String prop, List<Entity> entities) {
    this.propertyValues.put(prop, entities);
  }

  public void setPropertyValue(String propertyId, Entity entity) {
    List<Entity> entities = this.getPropertyValues(propertyId);
    if(entities == null)
      entities = new ArrayList<Entity>();
    entities.clear();
    entities.add(entity);
    this.propertyValues.put(propertyId, entities);
  }

  public void addPropertyValue(String prop, Entity entity) {
    List<Entity> entities = this.propertyValues.get(prop);
    if(entities == null)
      entities = new ArrayList<Entity>();
    entities.add(entity);
    this.propertyValues.put(prop, entities);
  }
  
  public void updatePropertyValue(String prop, Entity entity) {
    List<Entity> entities = this.propertyValues.get(prop);
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
    this.propertyValues.put(prop, entities);
  }

  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }
}
