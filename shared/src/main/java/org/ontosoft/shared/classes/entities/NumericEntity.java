package org.ontosoft.shared.classes.entities;

public class NumericEntity extends EnumerationEntity {
  double value;

  @Override
  public Double getValue() {
    return this.value;
  }

  @Override
  public void setValue(Object value) {
    if(value == null)
      return;
    if (!(value instanceof Double))
      this.value = new Double(value.toString());
    else
      this.value = (Double) value;
  }
}
