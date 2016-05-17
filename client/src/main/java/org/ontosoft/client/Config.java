package org.ontosoft.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;

public class Config {

  public native static String getServerURL() /*-{
    return $wnd.CONFIG.SERVER;
  }-*/;

  public native static String getPortalTitle() /*-{
    return $wnd.CONFIG.TITLE;
  }-*/;
  
  public native static String getOKColor() /*-{
    return $wnd.CONFIG.COLORS.ok;
  }-*/;
  
  public native static String getErrorColor() /*-{
    return $wnd.CONFIG.COLORS.error;
  }-*/;
  
  public native static String getHomeHTML() /*-{
    return $wnd.CONFIG.HOME;
  }-*/;

  public static List<Map<String, String>> getExternalServers() {
    List<Map<String, String>> servers = new ArrayList<Map<String,String>>();
    JsArray<JavaScriptObject> serversJs = _getExternalServers();
    for(int i=0; i<serversJs.length(); i++) {
      JavaScriptObject serverJs = serversJs.get(i);
      Map<String, String> externalServer = new HashMap<String, String>();
      externalServer.put("name", _getKey(serverJs, "name"));
      externalServer.put("server", _getKey(serverJs, "server"));
      externalServer.put("client", _getKey(serverJs, "client"));
      servers.add(externalServer);
    }
    return servers;
  }
  private native static JsArray<JavaScriptObject> _getExternalServers() /*-{
    return $wnd.CONFIG.EXTERNAL_SERVERS;
  }-*/;
  private native static String _getKey(JavaScriptObject obj, String key) /*-{
    return obj[key];
  }-*/;
  
  
}
