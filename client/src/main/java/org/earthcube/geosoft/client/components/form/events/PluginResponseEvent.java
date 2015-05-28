package org.earthcube.geosoft.client.components.form.events;

import org.earthcube.geosoft.shared.plugins.PluginResponse;
import com.google.gwt.event.shared.GwtEvent;

public class PluginResponseEvent extends GwtEvent<PluginResponseHandler> {

  public static Type<PluginResponseHandler> TYPE = new Type<PluginResponseHandler>();
  
  private final PluginResponse response;
  
  public PluginResponseEvent(PluginResponse response) {
    this.response = response;
  }
  
  @Override
  public Type<PluginResponseHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(PluginResponseHandler handler) {
    handler.onPluginResponse(this);
  }

  public PluginResponse getPluginResponse() {
    return response;
  }
}
