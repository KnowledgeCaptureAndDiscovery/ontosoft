package org.ontosoft.server.util;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration.plist.PropertyListConfiguration;

public class Config {
  static Config singleton = null;


  PropertyListConfiguration props = null;
  
  public static void load(HttpServletRequest request) {
    singleton = new Config(request);
  }
  
  public static Config get() {
    return singleton;
  }
  
  public Config(HttpServletRequest request) {
    this.props = loadServerConfiguration(request);
  }
  
  public PropertyListConfiguration getProperties() {
    return this.props;
  }
  
  private PropertyListConfiguration loadServerConfiguration(HttpServletRequest request) {
    String configFile = null;
    if(request != null) {
      ServletContext app = request.getSession().getServletContext();
      configFile = app.getInitParameter("config.file");
    }
    if (configFile == null) {
        String home = System.getProperty("user.home");
        if (home != null && !home.equals(""))
            configFile = home + File.separator + ".ontosoft"
                    + File.separator + "server.properties";
        else
            configFile = "/etc/ontosoft/server.properties";
    }
    // Create configFile if it doesn't exist (portal.properties)
    File cfile = new File(configFile);
    if (!cfile.exists()) {
        if (!cfile.getParentFile().exists() && !cfile.getParentFile().mkdirs()) {
            System.err.println("Cannot create config file directory : " + cfile.getParent());
            return null;
        }
        if (request != null)
            createDefaultServerConfig(request, configFile);
    }
    // Load properties from configFile
    PropertyListConfiguration props = new PropertyListConfiguration();
    try {
        props.load(configFile);
    } catch (Exception e) {
        e.printStackTrace();
    }
    return props;
  }
  
  private void createDefaultServerConfig(HttpServletRequest request, String configFile) {
    String server = request.getScheme() + "://" + request.getServerName() + ":"
            + request.getServerPort() + request.getContextPath();
    String storageDir = null;
    String home = System.getProperty("user.home");
    if (home != null && !home.equals(""))
        storageDir = home + File.separator + ".ontosoft" + File.separator + "storage";
    else
        storageDir = System.getProperty("java.io.tmpdir") +
                File.separator + "ontosoft" + File.separator + "storage";
    if (!new File(storageDir).mkdirs())
        System.err.println("Cannot create storage directory: " + storageDir);

    PropertyListConfiguration config = new PropertyListConfiguration();
    config.addProperty("storage.local", storageDir);
    config.addProperty("storage.tdb", storageDir + File.separator + "TDB");
    config.addProperty("storage.db", storageDir + File.separator + "DB");
    config.addProperty("server", server);
    config.addProperty("perm_feature_enabled", "true");
    
    try {
        config.save("file://" + configFile);
    } catch (Exception e) {
        e.printStackTrace();
    }
  }
}
