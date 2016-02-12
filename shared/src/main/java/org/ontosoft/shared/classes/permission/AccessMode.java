package org.ontosoft.shared.classes.permission;

import org.ontosoft.shared.classes.util.KBConstants;

public class AccessMode {
  String id;
  String mode;

  public String RMODEURI() {
    return KBConstants.PERMNS() + "Read";
  }

  public String WMODEURI() {
    return KBConstants.PERMNS() + "Write";
  }
  
  public void setId(String id) {
    this.id = id;
    this.mode = id.replaceAll(".*#", "");
  }
  
  public String getId() {
	  return id;
  }
  
  public String getMode() {
    return this.mode;
  }
  
  public void setMode(String mode) {
    this.mode = mode;
    if (mode.equals("Write")) {
    	this.id = WMODEURI();
    } else {
    	this.id = RMODEURI();
    }
  }
}
