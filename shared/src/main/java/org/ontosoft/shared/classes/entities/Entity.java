package org.ontosoft.shared.classes.entities;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

@JsonSubTypes({
  @Type(name="EnumerationEntity", value=EnumerationEntity.class),  
  @Type(name="TextEntity", value=TextEntity.class),
  @Type(name="DateEntity", value=DateEntity.class),
  @Type(name="NumericEntity", value=NumericEntity.class),
  @Type(name="MeasurementEntity", value=MeasurementEntity.class),
  @Type(name="Location", value=Location.class),
  @Type(name="ComplexEntity", value=ComplexEntity.class),
  @Type(name="Software", value=Software.class),
  @Type(name="SoftwareVersion", value=SoftwareVersion.class)
})
@JsonTypeInfo(use=Id.NAME)
public abstract class Entity {
  String id;
  String name;
  String type;
  String label;

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
  
  public abstract Object getValue();

  public abstract void setValue(Object value);

  public String toString() {
    if(this.getValue() != null)
      return this.getValue().toString();
    return null;
  }
}