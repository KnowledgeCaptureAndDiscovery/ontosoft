package org.ontosoft.shared.utils;

import java.util.Map;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.permission.Permission;

public class PermUtils
{
  static public boolean hasOwnerAccess(Permission perm, String username)
  {
	  if (username == null || !username.equals(perm.getOwner().getName()))
		  return false;
	  return true;
  }
  
  static public String getAccessLevelForUser(Permission perm, String username)
  {
	  String level = "Read";
	  Map<String, Authorization> authorizations = perm.getAuthorizations();
	  if (authorizations != null && authorizations.size() > 0)
	  {
		  for (Map.Entry<String, Authorization> authorization : authorizations.entrySet())
		  {
			  if (authorization.getValue().getAgentName().equals(username))
			  {
				  level = authorization.getValue().getAccessMode().getMode();
				  break;
			  }
		  }
	  }
	  
	  return level;
  }
}