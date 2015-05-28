package org.earthcube.geosoft.client.components.chart.events;

import com.google.gwt.event.shared.GwtEvent;

public class CategorySelectionEvent extends GwtEvent<CategorySelectionHandler> {

  public static Type<CategorySelectionHandler> TYPE = new Type<CategorySelectionHandler>();
  
  private final String categoryId;
  
  public CategorySelectionEvent(String categoryId) {
    this.categoryId = categoryId;
  }
  
  @Override
  public Type<CategorySelectionHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(CategorySelectionHandler handler) {
    handler.onSelection(this);
  }

  public String getCategory() {
    return categoryId;
  }
}
