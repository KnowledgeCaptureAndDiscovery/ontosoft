package org.ontosoft.shared.classes.entities;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("TextEntity")
public class TextEntity extends EnumerationEntity {
  String value;
  
  @Override
  public String getValue() {
    return this.value;
  }

  @Override
  public void setValue(Object value) {
    if(value == null)
      this.value = null;
    else if(value instanceof String)
      this.value = (String) value;
    else
      value = value.toString();
  }

}
