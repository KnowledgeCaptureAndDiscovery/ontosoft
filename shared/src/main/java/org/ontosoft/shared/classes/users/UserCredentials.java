package org.ontosoft.shared.classes.users;

import java.util.ArrayList;
import java.util.List;

public class UserCredentials {
  int id;
  String name;
  String password;
  List<String> roles;

  public UserCredentials() {
    roles = new ArrayList<String>();
  }
  
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(ArrayList<String> roles) {
    this.roles = roles;
  }
}
