package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.HandlerRegistration;

public interface HasPluginHandlers {
  HandlerRegistration addPluginResponseHandler(PluginResponseHandler handler);
}
