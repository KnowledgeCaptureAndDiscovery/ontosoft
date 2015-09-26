package org.ontosoft.shared.classes.entities;

public class EnumerationEntity extends Entity {
  @Override
  public Object getValue() {
    return this.getLabel();
  }

  @Override
  public void setValue(Object value) {
    if(value != null)
      this.setLabel(value.toString());
  }

}
