package org.ontosoft.shared.classes.vocabulary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vocabulary {
  Map<String, MetadataType> types;
  Map<String, MetadataCategory> categories;
  Map<String, MetadataProperty> properties;
  
  boolean needsReload;
  
  public Vocabulary() {
    this.types = new HashMap<String, MetadataType>();
    this.categories = new HashMap<String, MetadataCategory>();
    this.properties = new HashMap<String, MetadataProperty>();
  }
  
  public boolean isNeedsReload() {
    return needsReload;
  }

  public void setNeedsReload(boolean needsReload) {
    this.needsReload = needsReload;
  }

  public void addType(MetadataType category) {
    this.types.put(category.getId(), category);
  }
  
  public void addCategory(MetadataCategory category) {
    this.categories.put(category.getId(), category);
  }
  
  public void addProperty(MetadataProperty property) {
    this.properties.put(property.getId(), property);
  }
  
  public Map<String, MetadataType> getTypes() {
    return this.types;
  }
  
  public Map<String, MetadataCategory> getCategories() {
    return this.categories;
  }
  
  public Map<String, MetadataProperty> getProperties() {
    return this.properties;
  }
  
  public MetadataType getType(String typeid) {
    return this.types.get(typeid);
  }

  public MetadataCategory getCategory(String categoryid) {
    return this.categories.get(categoryid);
  }
  
  public MetadataProperty getProperty(String propertyid) {
    return this.properties.get(propertyid);
  }
  
  
  public List<MetadataProperty> getPropertiesInCategory(MetadataCategory category) {
    HashMap<String, Boolean> cats = new HashMap<String, Boolean>();
    ArrayList<MetadataCategory> queue = new ArrayList<MetadataCategory>();
    queue.add(category);
    while(!queue.isEmpty()) {
      MetadataCategory qcat = queue.remove(0);
      cats.put(qcat.getId(), true);
      for(String subcatid : qcat.getChildren())
        queue.add(this.getCategory(subcatid));
    }
    
    ArrayList<MetadataProperty> list = new ArrayList<MetadataProperty>();
    for(String propid : this.properties.keySet()) {
      MetadataProperty prop = this.properties.get(propid);
      if(cats.containsKey(prop.category))
        list.add(prop);
    }
    return list;
  }
  
  public List<MetadataProperty> getPropertiesForType(MetadataType type) {
    HashMap<String, Boolean> domains = new HashMap<String, Boolean>();
    ArrayList<MetadataType> queue = new ArrayList<MetadataType>();
    queue.add(type);
    while(!queue.isEmpty()) {
      MetadataType qtype = queue.remove(0);
      if(qtype != null) {
        domains.put(qtype.getId(), true);
        queue.add(this.getType(qtype.getParent()));
      }
    }
    ArrayList<MetadataProperty> list = new ArrayList<MetadataProperty>();
    for(String propid : this.properties.keySet()) {
      MetadataProperty prop = this.properties.get(propid);
      if(domains.containsKey(prop.getDomain()))
        list.add(prop);
    }
    return list;
  }
  
  public List<MetadataProperty> orderProperties(List<MetadataProperty> list) {
    Collections.sort(list, new Comparator<MetadataProperty>() {
      public int compare(MetadataProperty prop1, MetadataProperty prop2) {
        UIConfig uiconf1 = prop1.getUiConfig();
        UIConfig uiconf2 = prop2.getUiConfig();
        int pos1 = uiconf1 != null ? uiconf1.getPosition() : 9999;
        int pos2 = uiconf2 != null ? uiconf2.getPosition() : 9999;
        pos1 += (prop1.isRequired() ? 0 : 9999);
        pos2 += (prop2.isRequired() ? 0 : 9999);
        return pos1 - pos2;
      }
    });
    return list;
  }
  
  public List<MetadataProperty> filterUneditableProperties(List<MetadataProperty> list) {
    ArrayList<MetadataProperty> filteredList = new ArrayList<MetadataProperty>();
    for(MetadataProperty prop : list) {
      UIConfig uiconf = prop.getUiConfig();
      if(uiconf == null || !uiconf.isUneditable())
        filteredList.add(prop);
    }
    return filteredList;
  }
  
  public MetadataCategory orderChildCategories(MetadataCategory cat) {
    final Vocabulary me = this;
    Collections.sort(cat.getChildren(), new Comparator<String>() {
      public int compare(String cat1id, String cat2id) {
        MetadataCategory cat1 = me.getCategory(cat1id);
        MetadataCategory cat2 = me.getCategory(cat2id);
        UIConfig uiconf1 = cat1.getUiConfig();
        UIConfig uiconf2 = cat2.getUiConfig();
        int pos1 = uiconf1 != null ? uiconf1.getPosition() : 9999;
        int pos2 = uiconf2 != null ? uiconf2.getPosition() : 9999;
        return pos1 - pos2;
      }
    });
    return cat;
  }
  
  public List<MetadataType> getSubTypes(MetadataType type) {
    ArrayList<MetadataType> types = new ArrayList<MetadataType>();
    ArrayList<MetadataType> queue = new ArrayList<MetadataType>();
    queue.add(type);
    while(!queue.isEmpty()) {
      MetadataType qtype = queue.remove(0);
      if(qtype != null) {
        types.add(qtype);
        for(String subtypeid : qtype.getChildren())
          queue.add(this.getType(subtypeid));
      }
    }
    return types;
  }
  
  public boolean isA(MetadataClass cls1, MetadataClass cls2) {
    ArrayList<MetadataClass> queue = new ArrayList<MetadataClass>();
    queue.add(cls1);
    while(!queue.isEmpty()) {
      MetadataClass qtype = queue.remove(0);
      if(qtype.getId().equals(cls2.getId()))
        return true;
      if(qtype.getParent() != null)
        queue.add(this.getType(qtype.getParent()));
    }
    return false;
  }
}
