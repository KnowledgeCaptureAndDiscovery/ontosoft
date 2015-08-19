package org.ontosoft.client.components.form.events;

import org.ontosoft.shared.classes.Software;

import com.google.gwt.event.shared.GwtEvent;

public class SoftwareSaveEvent extends GwtEvent<SoftwareSaveHandler> {

  public static Type<SoftwareSaveHandler> TYPE = new Type<SoftwareSaveHandler>();
  
  private final Software software;
  
  public SoftwareSaveEvent(Software software) {
    this.software = software;
  }
  
  @Override
  public Type<SoftwareSaveHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(SoftwareSaveHandler handler) {
    handler.onSave(this);
  }

  public Software getSoftware() {
    return software;
  }
}
