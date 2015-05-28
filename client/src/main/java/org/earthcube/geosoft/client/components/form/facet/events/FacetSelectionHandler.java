package org.earthcube.geosoft.client.components.form.facet.events;

import com.google.gwt.event.shared.EventHandler;

public interface FacetSelectionHandler extends EventHandler {
  void onFacetSelection(FacetSelectionEvent event);
}
