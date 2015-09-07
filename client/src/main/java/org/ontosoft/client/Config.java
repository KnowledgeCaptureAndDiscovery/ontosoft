package org.ontosoft.client;

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

}
