package org.ontosoft.shared.classes.vocabulary;

public class MetadataEnumeration extends MetadataEntity {
  String type;

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  @Override
  public int hashCode() {
    return this.id.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof MetadataEnumeration))
      return false;
    MetadataEnumeration en = (MetadataEnumeration) obj;    
    return this.getId().equals(en.getId());
  }
}
