package org.ontosoft.shared.utils;

import java.util.Map;

import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.permission.Permission;

public class PermUtils {
  static public boolean hasOwnerAccess(Permission perm, String username) {
    if (username == null || !username.equals(perm.getOwner().getName()))
      return false;
    return true;
  }

  static public String getAccessLevelForUser(Software software, String username, String targetid) {
    return getAccessLevelForUser(software.getPermission(), username, targetid);
  }

  static public String getAccessLevelForUser(SoftwareSummary summary, String username, String targetid) {
    return getAccessLevelForUser(summary.getPermission(), username, targetid);		
  }
	
  static public String getAccessLevelForUser(Permission perm, String username, String targetid) {
    String level = "Read";
    Map<String, Authorization> authorizations = perm.getAuthorizations();
    if (authorizations != null && authorizations.size() > 0) {
      for (Map.Entry<String, Authorization> authorization : authorizations.entrySet()) {
        if (authorization.getValue().getAgentName().equals(username) &&
          authorization.getValue().getAccessToObjId().equals(targetid)) {
          level = authorization.getValue().getAccessMode().getMode();
          break;
        }
      }
    }
    return level;		
  }
}