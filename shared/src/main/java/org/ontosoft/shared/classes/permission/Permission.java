package org.ontosoft.shared.classes.permission;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Permission {
  String id;
  List<Agent> owners;
  String type;
  Map<String, Authorization> authorizations;
  
  public Permission() {
    this.authorizations = new HashMap<String, Authorization>();
    this.owners = new ArrayList<Agent>();
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public void addOwnerid(String id) {
    if (!owneridExists(id)) {
      Agent owner = new Agent();
      owner.setId(id);
      this.owners.add(owner);    	
    }
  }

  public boolean removeOwnerid(String id) {
	int index = 0;
	boolean removed = false;
    for (Agent owner: owners) {
      if (owner.getId().equals(id)) {
        this.owners.remove(index);
        removed = true;
        break;
      }
      index++;
	}
    return removed;
  }
  
  public void removeAllOwners() {
    owners.clear();
  }
  
  public List<Agent> getOwners() {
    return this.owners;
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
  
  public boolean owneridExists(String ownerid) {
    boolean exists = false;
    
    for (Agent owner: owners) {
      if (owner.getId().equals(ownerid)) {
        exists = true;
        break;
      }
    }
    return exists;
  }
  
  public boolean ownernameExists(String ownername) {
    boolean exists = false;

    for (Agent owner: owners) {
      if (owner.getName().equals(ownername)) {
        exists = true;
        break;
      }
    }
    return exists;
  }
  
  public void addOrUpdateAuth(Authorization authorization) {
    boolean modeupdated = false;
    for(Authorization auth:getAuthorizations().values()) {
      if(auth.getAgentName().equals(authorization.getAgentName()) &&
         auth.getAccessToObjId().equals(authorization.getAccessToObjId())) {
         auth.setAccessMode(authorization.getAccessMode());
         modeupdated = true;
      }
    }
      
    if (!modeupdated) {
      addAuth(authorization);
    }
  }
  
  public void removeAuthsHavingTarget(String accessToObjId) {
    for(Iterator<Map.Entry<String, Authorization>> it = getAuthorizations().entrySet().iterator(); it.hasNext();) {
      Map.Entry<String, Authorization> entry = it.next();
      if(entry.getValue().getAccessToObjId().equals(accessToObjId)) {
        it.remove();
      }
    }	  
  }
  
  public boolean authExists(String username, String accessToObjId) {
    boolean exists = false;
    for(Authorization auth:getAuthorizations().values()) {
      if(auth.getAgentName().equals(username) &&
        auth.getAccessToObjId().equals(accessToObjId)) {
        exists = true;
      }
    }
	return exists;
  }
}
