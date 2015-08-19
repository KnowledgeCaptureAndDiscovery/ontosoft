package org.ontosoft.shared.classes.vocabulary;

import java.util.Collection;

import org.ontosoft.shared.plugins.Plugin;

public class MetadataProperty extends MetadataEntity {
  String question;
  String domain;
  String range;

  String category;
  boolean required;
  boolean multiple; // Is allowed to have multiple values
  
  UIConfig uiConfig;
  SearchConfig searchConfig;
  
  Collection<Plugin> plugins;

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getRange() {
    return range;
  }

  public void setRange(String range) {
    this.range = range;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(boolean isMultiple) {
    this.multiple = isMultiple;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public UIConfig getUiConfig() {
    return uiConfig;
  }

  public void setUiConfig(UIConfig uiConfig) {
    this.uiConfig = uiConfig;
  }

  public SearchConfig getSearchConfig() {
    return searchConfig;
  }

  public void setSearchConfig(SearchConfig searchConfig) {
    this.searchConfig = searchConfig;
  }

  public Collection<Plugin> getPlugins() {
    return plugins;
  }

  public void setPlugins(Collection<Plugin> plugins) {
    this.plugins = plugins;
  }
}
