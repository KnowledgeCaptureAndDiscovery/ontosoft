package org.earthcube.geosoft.shared.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class PluginRegistrar {

  // Plugins structure: {<propertyid>: [{<name> : <plugin>} .. ]
  private static Map<String, Map<String, Plugin>> plugins
    = new HashMap<String, Map<String, Plugin>>();
  
  public static void registerPlugin(Plugin plugin) {
    Map<String, Plugin> propPlugins = plugins.get(plugin.getPropertyId());
    if(propPlugins == null)
      propPlugins = new HashMap<String, Plugin>();
    if(!propPlugins.containsKey(plugin.getName()))
      propPlugins.put(plugin.getName(), plugin);
    plugins.put(plugin.getPropertyId(), propPlugins);
  }
  
  public static Collection<Plugin> getPluginsForProperty(String propertyId) {
    if(plugins.containsKey(propertyId))
      return plugins.get(propertyId).values();
    return null;
  }
  
  public static Plugin getPluginByName(String name) {
    for(Map<String, Plugin> propPlugins : plugins.values()) {
      if(propPlugins.containsKey(name))
        return propPlugins.get(name);
    }
    return null;
  }
}
