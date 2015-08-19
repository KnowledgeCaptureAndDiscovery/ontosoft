package org.ontosoft.client.components.form.facet.events;


import org.ontosoft.shared.search.EnumerationFacet;

import com.google.gwt.event.shared.GwtEvent;

public class FacetSelectionEvent extends GwtEvent<FacetSelectionHandler> {

  public static Type<FacetSelectionHandler> TYPE = new Type<FacetSelectionHandler>();
  
  private final EnumerationFacet facet;
  
  public FacetSelectionEvent(EnumerationFacet facet) {
    this.facet = facet;
  }
  
  @Override
  public Type<FacetSelectionHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(FacetSelectionHandler handler) {
    handler.onFacetSelection(this);
  }

  public EnumerationFacet getFacet() {
    return facet;
  }
}
