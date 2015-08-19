package org.ontosoft.client.components.form.formgroup.input.events;

import com.google.gwt.event.shared.EventHandler;

public interface EntityChangeHandler extends EventHandler {
  void onEntityChange(EntityChangeEvent event);
}
