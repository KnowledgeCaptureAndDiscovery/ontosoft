package org.ontosoft.client.components.form.facet.events;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;

public interface HasFacetHandlers extends HasHandlers {
  HandlerRegistration addFacetSelectionHandler(FacetSelectionHandler handler);
}
