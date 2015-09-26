package org.ontosoft.shared.classes;

import java.util.Date;
import java.util.List;

import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.provenance.Activity;
import org.ontosoft.shared.classes.provenance.Agent;
import org.ontosoft.shared.classes.provenance.ProvEntity;
import org.ontosoft.shared.classes.provenance.Provenance;

public class SoftwareSummary {
  String id;
  String name;
  String type;
  String label;
  String description;
  
  List<String> authors;
  String user;
  long time;
  String updateUser;
  long updateTime;

  public SoftwareSummary() { }
  
  public SoftwareSummary(Software sw) {
    this.setId(sw.getId());
    this.setName(sw.getName());
    this.setType(sw.getType());
    this.setLabel(sw.getLabel());
    
    setProvenanceDetails(sw.getProvenance());
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getAuthors() {
    return authors;
  }

  public void setAuthors(List<String> authors) {
    this.authors = authors;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }

  public String getUpdateUser() {
    return updateUser;
  }

  public void setUpdateUser(String updateUser) {
    this.updateUser = updateUser;
  }

  public long getUpdateTime() {
    return updateTime;
  }

  public void setUpdateTime(long updateTime) {
    this.updateTime = updateTime;
  }
  
  private void setProvenanceDetails(Provenance prov) {
    if(prov == null)
      return;
    
    ProvEntity swentity = prov.getEntity(this.getId());
    if(swentity != null) {
      Activity act = prov.getActivity(swentity.getGeneratedBy());
      if(act != null) {
        Agent agent = prov.getAgent(act.getAgentId());
        if(agent != null)
          this.setUser(agent.getName());
        Date time = act.getTime();
        if(time != null)
          this.setTime(time.getTime());
      }
    }
  }
}
