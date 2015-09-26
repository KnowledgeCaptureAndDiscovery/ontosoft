package org.ontosoft.shared.classes.entities;

import org.ontosoft.shared.classes.provenance.Provenance;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Software extends ComplexEntity {  
  Provenance provenance;
  
  @JsonIgnore
  boolean dirty;
  
  public boolean isDirty() {
    return dirty;
  }

  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  public Provenance getProvenance() {
    return provenance;
  }

  public void setProvenance(Provenance provenance) {
    this.provenance = provenance;
  }
}
