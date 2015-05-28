package org.earthcube.geosoft.client.components.form.formgroup.input.events;

import org.earthcube.geosoft.shared.classes.Entity;

import com.google.gwt.event.shared.GwtEvent;

public class EntityChangeEvent extends GwtEvent<EntityChangeHandler> {

  public static Type<EntityChangeHandler> TYPE = new Type<EntityChangeHandler>();
  
  private Entity entity;
  
  public EntityChangeEvent(Entity entity) {
    this.entity = entity;
  }
  
  @Override
  public Type<EntityChangeHandler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(EntityChangeHandler handler) {
    handler.onEntityChange(this);
  }

  public Entity getEntity() {
    return entity;
  }
}
