package org.ontosoft.shared.classes.provenance;

public class Agent {
  String id;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getName() {
    return id.replaceAll(".*\\/", "");
  }
}
