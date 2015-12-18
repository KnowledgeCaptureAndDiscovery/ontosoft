package org.ontosoft.shared.classes.permission;

public class Authorization {
  String id;
  String agentId;
  String agentName;
  AccessMode accessMode;
  String accessToObjId;
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getAgentId () {
    return agentId;
  }

  public void setAgentId(String agentid) {
    this.agentId = agentid;
    this.agentName = agentid.replaceAll(".*\\/", "");
  }
	  
  public AccessMode getAccessMode () {
	  return accessMode;
  }

  public void setAccessMode(AccessMode mode) {
	  this.accessMode = mode;
  }

  public String getAccessToObjId () {
	  return accessToObjId;
  }

  public void setAccessToObjId(String id) {
	  this.accessToObjId = id;
  }
  
  public void setAgentName(String agentName) {
    this.agentName = agentName;
  }
	  
  public String getAgentName () {
	  return agentName;
  }
}
