package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasSoftwareVersionHandlers extends HasHandlers {
  HandlerRegistration addSoftwareVersionSaveHandler(SoftwareVersionSaveHandler handler);
  HandlerRegistration addSoftwareVersionChangeHandler(SoftwareVersionChangeHandler handler);
}
