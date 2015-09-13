package org.ontosoft.shared.classes.provenance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Provenance {
  String id;
  Map<String, ProvEntity> entities;
  Map<String, Activity> activities;
  Map<String, Agent> agents;

  public Provenance() {
    this.entities = new HashMap<String, ProvEntity>();
    this.activities = new HashMap<String, Activity>();
    this.agents = new HashMap<String, Agent>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Map<String, ProvEntity> getEntities() {
    return entities;
  }

  public void setEntities(Map<String, ProvEntity> entities) {
    this.entities = entities;
  }

  public Map<String, Activity> getActivities() {
    return activities;
  }

  public void setActivities(Map<String, Activity> activities) {
    this.activities = activities;
  }

  public Map<String, Agent> getAgents() {
    return agents;
  }

  public void setAgents(Map<String, Agent> agents) {
    this.agents = agents;
  }
  
  public ProvEntity getEntity(String entityid) {
    return this.entities.get(entityid);
  }
  
  public Activity getActivity(String activityid) {
    return this.activities.get(activityid);
  }
  
  public Agent getAgent(String agentid) {
    return this.agents.get(agentid);
  }
  
  public void addEntity(ProvEntity entity) {
    this.entities.put(entity.getId(), entity);
  }
  
  public void addActivity(Activity activity) {
    this.activities.put(activity.getId(), activity);
  }
  
  public void addAgent(Agent agent) {
    this.agents.put(agent.getId(), agent);
  }

  public List<ProvEntity> getAllVersionsOf(String entityid) {
    List<ProvEntity> list = new ArrayList<ProvEntity>();
    while(this.entities.containsKey(entityid)) {
      ProvEntity e = this.entities.get(entityid);
      list.add(e);
      entityid = e.getRevisionOf();
    }
    return list;
  }

}
