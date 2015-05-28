package org.earthcube.geosoft.shared.plugins;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.SoftwareSummary;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PluginResponse {
  String message;
  Plugin plugin;
  SoftwareSummary softwareInfo;
  Map<String, List<Object>> suggestedMetadata;

  @JsonCreator
  public PluginResponse(@JsonProperty("plugin") Plugin plugin) {
    this.plugin = plugin;
    this.suggestedMetadata = new HashMap<String, List<Object>>();
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public Plugin getPlugin() {
    return plugin;
  }

  public void setPlugin(Plugin plugin) {
    this.plugin = plugin;
  }

  public SoftwareSummary getSoftwareInfo() {
    return softwareInfo;
  }

  public void setSoftwareInfo(SoftwareSummary softwareInfo) {
    this.softwareInfo = softwareInfo;
  }
  
  public void setSoftwareInfoFromSoftware(Software software) {
    this.softwareInfo = new SoftwareSummary(software);
  }

  public Map<String, List<Object>> getSuggestedMetadata() {
    return suggestedMetadata;
  }

  public List<Object> getSuggestedMetadata(String propertyId) {
    return suggestedMetadata.get(propertyId);
  }

  public void setSuggestedMetadata(Map<String, List<Object>> suggestedMetadata) {
    this.suggestedMetadata = suggestedMetadata;
  }

  public void addSuggestedMetadata(String prop, List<Object> entities) {
    this.suggestedMetadata.put(prop, entities);
  }

  public void setSuggestedMetadata(String propertyId, List<Object> entities) {
    this.suggestedMetadata.put(propertyId, entities);
  }

  public void addSuggestedMetadata(String prop, Object entity) {
    List<Object> entities = this.suggestedMetadata.get(prop);
    if (entities == null)
      entities = new ArrayList<Object>();
    entities.add(entity);
    this.suggestedMetadata.put(prop, entities);
  }
}
