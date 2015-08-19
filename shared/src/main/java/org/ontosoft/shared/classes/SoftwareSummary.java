package org.ontosoft.shared.classes;

public class SoftwareSummary {
  String id;
  String name;
  String type;
  String label;

  public SoftwareSummary() { }
  
  public SoftwareSummary(Software sw) {
    this.setId(sw.getId());
    this.setName(sw.getName());
    this.setType(sw.getType());
    this.setLabel(sw.getLabel());
  }
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }
}
