package org.earthcube.geosoft.client.components.form.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasSoftwareHandlers extends HasHandlers {
  HandlerRegistration addSoftwareSaveHandler(SoftwareSaveHandler handler);
  HandlerRegistration addSoftwareChangeHandler(SoftwareChangeHandler handler);
}
