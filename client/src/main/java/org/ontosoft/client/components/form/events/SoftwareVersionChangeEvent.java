package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.SoftwareVersion;

import com.google.gwt.event.shared.GwtEvent;

public class SoftwareVersionChangeEvent extends GwtEvent<SoftwareVersionChangeHandler> {

  public static Type<SoftwareVersionChangeHandler> TYPE = new Type<SoftwareVersionChangeHandler>();
  
  private SoftwareVersion version;
  
  public SoftwareVersionChangeEvent(SoftwareVersion version) {
    this.version = version;
  }
  
  @Override
  public Type<SoftwareVersionChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SoftwareVersionChangeHandler handler) {
    handler.onSoftwareVersionChange(this);
  }

  public SoftwareVersion getSoftwareVersion() {
    return version;
  }
}