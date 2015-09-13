package org.ontosoft.server.users;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import org.ontosoft.server.util.Config;
import org.ontosoft.shared.classes.users.UserCredentials;
import org.ontosoft.shared.classes.users.UserSession;

public class UserDatabase {
  private static int expire_in_days = 1; // Session expiry
  private String dbdir;
  private Connection dbc;
  
  private static UserDatabase singleton = null;
  public static UserDatabase get() {
    if(singleton == null)
      singleton = new UserDatabase();
    return singleton;
  }
  
  public UserDatabase() {
    initializeDB();
  }
 
  /**
   * DB Initialization
   */
  private void initializeDB() {
    if(Config.get() != null) {
      this.dbdir = Config.get().getProperties().getString("storage.db");
      this.dbc = connectDB();
      createSchema();
    }
  }
  
  public Connection connectDB() {
    try {
      String driver = "org.apache.derby.jdbc.EmbeddedDriver";
      Class.forName(driver);
      String url = "jdbc:derby:"+this.dbdir+";create=true";
      return DriverManager.getConnection(url);
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
  public static void shutdownDB() {
    if(singleton != null && singleton.dbc != null) {
      try {
        singleton.dbc.close();
        String url = "jdbc:derby:"+singleton.dbdir+";shutdown=true";
        singleton.dbc = DriverManager.getConnection(url);
        singleton.dbc.close();
      } catch (SQLException e) {
        //e.printStackTrace();
      }
    }
  }
  
  private void createSchema() {
    try {
      DatabaseMetaData meta = this.dbc.getMetaData();
      
      /* Users Table */
      ResultSet res = meta.getTables(null,  null,  "USERS", null);
      if(!res.next()) {
        String sql = "CREATE TABLE users "
                      + "("
                      + "id INTEGER NOT NULL, "
                      + "username VARCHAR(36) UNIQUE NOT NULL, "
                      + "password VARCHAR(255) NOT NULL, "
                      + "fullname VARCHAR(255) NOT NULL, "
                      + "PRIMARY KEY (id)"
                      + ")";
        dbc.createStatement().executeUpdate(sql);
        
        // TODO: Temporary -- Remove
        String isql = "INSERT INTO users VALUES "
            + "(1, 'admin', 'admin123', 'Administrator'), "
            + "(2, 'varun', 'varun123', 'Varun Ratnakar') ";
        dbc.createStatement().executeUpdate(isql);
        
        System.out.println("Created Users Table");
      }
      res.close();
      
      /* Roles Table */
      ResultSet res2 = meta.getTables(null,  null,  "ROLES", null);
      if(!res2.next()) {
        String sql = "CREATE TABLE roles "
                      + "("
                      + "id INTEGER NOT NULL, "
                      + "role VARCHAR(255) NOT NULL, "
                      + "PRIMARY KEY (id)"
                      + ")";
        dbc.createStatement().executeUpdate(sql);
        
        String isql = "INSERT INTO roles VALUES "
            + "(1, 'admin'), "
            + "(2, 'user'), "
            + "(3, 'importer')";
        dbc.createStatement().executeUpdate(isql);
        
        System.out.println("Created Roles Table");
      }
      res2.close();
      
      /* User Roles Table */
      ResultSet res3 = meta.getTables(null,  null,  "USER_ROLES", null);
      if(!res3.next()) {
        String sql = "CREATE TABLE user_roles "
                      + "("
                      + "userid INTEGER NOT NULL, "
                      + "roleid INTEGER NOT NULL "
                      + ")";
        dbc.createStatement().executeUpdate(sql);
        
        // TODO: Temporary -- Remove
        String isql = "INSERT INTO user_roles VALUES "
            + "(1, 1), "
            + "(1, 2), "
            + "(2, 2), "
            + "(2, 3) ";
        dbc.createStatement().executeUpdate(isql);
        
        System.out.println("Created User Roles Table");
      }
      res3.close();
      
      /* Sessions Table */
      ResultSet res4 = meta.getTables(null,  null,  "SESSIONS", null);
      if(!res4.next()) {
        String sql = "CREATE TABLE sessions "
                      + "("
                      + "sessionid CHAR(36) NOT NULL, "            
                      + "userid INTEGER NOT NULL, "
                      + "timestamp TIMESTAMP NOT NULL"
                      + ")";
        dbc.createStatement().executeUpdate(sql);
        System.out.println("Created Sessions Table");
      }
      res4.close();
      
      /* Remove any old sessions */
      String sql = "DELETE FROM sessions WHERE " 
                   + "{fn TIMESTAMPDIFF( SQL_TSI_DAY, TIMESTAMP, CURRENT_TIMESTAMP)} > " 
                    + expire_in_days;
      dbc.createStatement().executeUpdate(sql);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
  
  public UserSession login(UserCredentials credentials) {
    if(credentials == null)
      return null;
    
    try {
      String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
      PreparedStatement ps = dbc.prepareStatement(sql);
      ps.setString(1, credentials.getName());
      ps.setString(2, credentials.getPassword());
      ResultSet rs = ps.executeQuery();
      int userid = 0;
      if(rs.next())
        userid = rs.getInt(1);
      rs.close();
      ps.close();
      
      if(userid > 0) {
        UUID uuid = UUID.randomUUID();
        String isql = "INSERT INTO sessions VALUES (?, ?, CURRENT_TIMESTAMP)";
        PreparedStatement ips = dbc.prepareStatement(isql);
        ips.setString(1, uuid.toString());
        ips.setInt(2, userid);
        ips.executeUpdate();
        ips.close();
        return validateSession(
            new UserSession(credentials.getName(), uuid.toString(), new ArrayList<String>()));
      }
      
      return null;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  public UserSession validateSession(UserSession session) {
    User user = UserDatabase.get().getUser(session);
    if(user != null) {
      session.setRoles(user.getRoles());
      return session;
    }
    return null;
  }
  
  public boolean logout(UserSession session) {
    if(session == null)
      return true;
    
    try {
      String isql = "DELETE FROM sessions WHERE sessionid = ?";
      PreparedStatement ips = dbc.prepareStatement(isql);
      ips.setString(1, session.getSessionid());
      ips.executeUpdate();
      ips.close();
      return true;
    }
    catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
  
  public User getUser(UserSession session) {
    if(session == null)
      return null;

    User user = null;
    try {
      // Get user id, name, password
      String sql = "SELECT b.id, b.username, b.password FROM "
          + "sessions a "
          + "LEFT JOIN users b ON a.userid=b.id "
          + "WHERE sessionid = ? AND b.username = ?";
      PreparedStatement ps = dbc.prepareStatement(sql);
      ps.setString(1, session.getSessionid());
      ps.setString(2, session.getUsername());
      ResultSet rs = ps.executeQuery();
      if(rs.next()) {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        String password = rs.getString(3);
        user = new User(id, name, password, new ArrayList<String>());
      }
      rs.close();
      ps.close();
      
      // Get user roles
      if(user != null) {
        sql = "SELECT b.role FROM user_roles a "
            + "LEFT JOIN roles b ON a.roleid=b.id "
            + "WHERE a.userid = ?";
        
        ps = dbc.prepareStatement(sql);
        ps.setInt(1, user.getId());
        rs = ps.executeQuery();
        ArrayList<String> roles = new ArrayList<String>();
        while(rs.next()) {
          String role = rs.getString(1);
          roles.add(role);
        }
        user.setRoles(roles);
        rs.close();
        ps.close();
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    return user;
  }
}
