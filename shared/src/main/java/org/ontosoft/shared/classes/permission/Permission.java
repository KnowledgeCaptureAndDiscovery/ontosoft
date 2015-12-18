package org.ontosoft.shared.classes.permission;

import java.util.HashMap;
import java.util.Map;

public class Permission {
  String id;
  Agent owner;
  String type;
  Map<String, Authorization> authorizations;
  
  public Permission() {
    this.authorizations = new HashMap<String, Authorization>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void setOwnerId(String id) {
    Agent owner = new Agent();
    owner.setId(id);
    this.owner = owner;
  }

  public Agent getOwner() {
  return this.owner;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
  
  public Map<String, Authorization> getAuthorizations() {
    return authorizations;
  }

  public void setAgents(Map<String, Authorization> authorizations) {
    this.authorizations = authorizations;
  }
  
  public Authorization getAgent(String authid) {
    return this.authorizations.get(authid);
  }
 
  public void addAuth(Authorization auth) {
    this.authorizations.put(auth.getId(), auth);
  }
}
