package org.ontosoft.client.components.form.events;

import com.google.gwt.event.shared.EventHandler;

public interface SoftwareChangeHandler extends EventHandler {
  void onSoftwareChange(SoftwareChangeEvent event);
}
