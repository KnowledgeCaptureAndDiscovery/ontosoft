package org.ontosoft.shared.classes.vocabulary;

public class MetadataCategory extends MetadataClass {
  String sublabel;
  UIConfig uiConfig;

  public String getSublabel() {
    return sublabel;
  }

  public void setSublabel(String sublabel) {
    this.sublabel = sublabel;
  }

  public UIConfig getUiConfig() {
    return uiConfig;
  }

  public void setUiConfig(UIConfig uiConfig) {
    this.uiConfig = uiConfig;
  }

}
