package org.ontosoft.shared.classes.vocabulary;

import java.util.ArrayList;
import java.util.List;

public class MetadataClass extends MetadataEntity {
  String parent;
  List<String> children;
  
  public MetadataClass() {
    this.children = new ArrayList<String>();
  }

  public String getParent() {
    return parent;
  }

  public void setParent(String parent) {
    this.parent = parent;
  }

  public List<String> getChildren() {
    return children;
  }

  public void setChildren(ArrayList<String> children) {
    this.children = children;
  }

  public void addChild(String child) {
    this.children.add(child);
  }
}
