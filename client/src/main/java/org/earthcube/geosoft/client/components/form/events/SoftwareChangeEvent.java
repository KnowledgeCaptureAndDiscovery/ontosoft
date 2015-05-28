package org.earthcube.geosoft.client.components.form.events;

import org.earthcube.geosoft.shared.classes.Software;

import com.google.gwt.event.shared.GwtEvent;

public class SoftwareChangeEvent extends GwtEvent<SoftwareChangeHandler> {

  public static Type<SoftwareChangeHandler> TYPE = new Type<SoftwareChangeHandler>();
  
  private Software software;
  
  public SoftwareChangeEvent(Software software) {
    this.software = software;
  }
  
  @Override
  public Type<SoftwareChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SoftwareChangeHandler handler) {
    handler.onSoftwareChange(this);
  }

  public Software getSoftware() {
    return software;
  }
}