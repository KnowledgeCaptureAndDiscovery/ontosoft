package org.ontosoft.shared.classes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Entity {
  String id;
  String type;
  Object value;

  @JsonCreator
  public Entity(
      @JsonProperty("id") String id,
      @JsonProperty("value") Object value, 
      @JsonProperty("type") String type) {
    this.id = id;
    this.value = value;
    this.type = type;
  }
  
  public void copyFrom(Entity e) {
    this.setId(e.getId());
    this.setValue(e.getValue());
    this.setType(e.getType());
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Object getValue() {
    return value;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public String toString() {
    if(this.value != null)
      return this.value.toString();
    return null;
  }
}