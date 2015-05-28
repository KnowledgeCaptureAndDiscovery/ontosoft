package org.earthcube.geosoft.client.components.chart.events;

import com.google.gwt.event.shared.EventHandler;

public interface CategorySelectionHandler extends EventHandler {
  void onSelection(CategorySelectionEvent event);
}
