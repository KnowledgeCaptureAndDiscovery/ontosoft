package org.ontosoft.shared.classes.provenance;

import java.util.Date;

public class Activity {
  String id;
  Date time;
  String agentId;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Date getTime() {
    return time;
  }

  public void setTime(Date time) {
    this.time = time;
  }

  public String getAgentId () {
    return agentId;
  }

  public void setAgentId(String agentid) {
    this.agentId = agentid;
  }

}
