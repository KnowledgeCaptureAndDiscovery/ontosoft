package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface SoftwareVersionChangeHandler extends EventHandler {
  void onSoftwareVersionChange(SoftwareVersionChangeEvent event);
}
