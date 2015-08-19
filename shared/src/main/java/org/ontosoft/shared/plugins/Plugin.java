package org.ontosoft.shared.plugins;

import org.ontosoft.shared.classes.Software;

public class Plugin {
  String name;
  String label;
  String propertyId;
  String valueMatchRegex;
  String icon;
  boolean asynchronous;
  boolean clientPlugin;
  boolean automaticallyTriggered;

  public Plugin() {
  }
  
  public Plugin(String name, String label, String propertyId) {
    this.name = name;
    this.label = label;
    this.propertyId = propertyId;
    this.icon = "fa-gear";
    this.valueMatchRegex = ".*";
    this.asynchronous = false;
    this.clientPlugin = false;
    this.automaticallyTriggered = false;
  }

  public String getName() {
    return name;
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

  public String getPropertyId() {
    return propertyId;
  }

  public void setPropertyId(String propertyId) {
    this.propertyId = propertyId;
  }

  public String getValueMatchRegex() {
    return valueMatchRegex;
  }

  public void setValueMatchRegex(String regex) {
    this.valueMatchRegex = regex;
  }

  public String getIcon() {
    return icon;
  }

  public void setIcon(String icon) {
    this.icon = icon;
  }

  public boolean isAsynchronous() {
    return asynchronous;
  }

  public void setAsynchronous(boolean isAsynchronous) {
    this.asynchronous = isAsynchronous;
  }

  public boolean isClientPlugin() {
    return clientPlugin;
  }

  public void setClientPlugin(boolean isClientPlugin) {
    this.clientPlugin = isClientPlugin;
  }

  public boolean isAutomaticallyTriggered() {
    return automaticallyTriggered;
  }

  public void setAutomaticallyTriggered(boolean isAutomaticallyTriggered) {
    this.automaticallyTriggered = isAutomaticallyTriggered;
  }

  public PluginResponse run(Software software) {
    return null;
  }

}
