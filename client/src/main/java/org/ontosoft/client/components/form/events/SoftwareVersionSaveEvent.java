package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.entities.SoftwareVersion;

import com.google.gwt.event.shared.GwtEvent;

public class SoftwareVersionSaveEvent extends GwtEvent<SoftwareVersionSaveHandler> {

  public static Type<SoftwareVersionSaveHandler> TYPE = new Type<SoftwareVersionSaveHandler>();
  
  private final SoftwareVersion version;
  
  public SoftwareVersionSaveEvent(SoftwareVersion software) {
    this.version = software;
  }
  
  @Override
  public Type<SoftwareVersionSaveHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SoftwareVersionSaveHandler handler) {
    handler.onSave(this);
  }

  public SoftwareVersion getSoftwareVersion() {
    return version;
  }
}
