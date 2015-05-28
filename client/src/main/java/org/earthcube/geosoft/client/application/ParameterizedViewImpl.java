package org.earthcube.geosoft.client.application;

import com.gwtplatform.mvp.client.ViewImpl;

abstract public class ParameterizedViewImpl extends ViewImpl implements ParameterizedView {

  @Override
  abstract public void initializeParameters(String[] parameters);

  /* Theme specific call - to be called after the view is rendered */
  protected native void initMaterial() /*-{
    if($wnd && $wnd.jQuery && $wnd.jQuery.material)
      $wnd.jQuery.material.init(); 
  }-*/;

}
