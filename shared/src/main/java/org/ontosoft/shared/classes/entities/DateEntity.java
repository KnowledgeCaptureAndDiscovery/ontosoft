package org.ontosoft.shared.classes.entities;

import java.util.Date;

public class DateEntity extends EnumerationEntity {
  Date value;

  @Override
  public Date getValue() {
    return this.value;
  }

  @SuppressWarnings("deprecation")
  @Override
  public void setValue(Object value) {
    if(value == null)
      this.value = null;
    else if(value instanceof Date)
      this.value = (Date) value;
    else if(value instanceof Long)
      this.value = new Date((Long)value);
  }
}
