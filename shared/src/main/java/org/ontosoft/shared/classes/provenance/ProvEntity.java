package org.ontosoft.shared.classes.provenance;

public class ProvEntity {
  private String id;
  private String revisionOf;
  private String generatedBy;
  private String invalidatedBy;

  public ProvEntity() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getRevisionOf() {
    return revisionOf;
  }

  public void setRevisionOf(String revisionOf) {
    this.revisionOf = revisionOf;
  }

  public String getGeneratedBy() {
    return generatedBy;
  }

  public void setGeneratedBy(String generatedBy) {
    this.generatedBy = generatedBy;
  }

  public String getInvalidatedBy() {
    return invalidatedBy;
  }

  public void setInvalidatedBy(String invalidatedBy) {
    this.invalidatedBy = invalidatedBy;
  }
}
