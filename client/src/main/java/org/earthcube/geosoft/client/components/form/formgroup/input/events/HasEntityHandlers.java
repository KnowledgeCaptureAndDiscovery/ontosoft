package org.earthcube.geosoft.client.components.form.formgroup.input.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasEntityHandlers extends HasHandlers {
  HandlerRegistration addEntityChangeHandler(EntityChangeHandler handler);
}
