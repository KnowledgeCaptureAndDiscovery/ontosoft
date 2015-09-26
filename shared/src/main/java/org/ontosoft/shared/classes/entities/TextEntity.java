package org.ontosoft.shared.classes.entities;

public class TextEntity extends EnumerationEntity {
  String value;
  
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(Object value) {
    if(value instanceof String)
      this.value = (String) value;
    else
      value = value.toString();
  }

}
