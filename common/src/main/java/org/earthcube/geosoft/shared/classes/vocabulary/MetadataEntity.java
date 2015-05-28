package org.earthcube.geosoft.shared.classes.vocabulary;

import java.util.List;

public class MetadataEntity {
  String id;
  String name;
  String label;
  
  String parent;
  List<String> children;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getName() {
    return this.name;
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
  
  @Override
  public int hashCode() {
    return (id + "_" + label).hashCode();
  }
}
