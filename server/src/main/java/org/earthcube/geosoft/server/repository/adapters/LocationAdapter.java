package org.earthcube.geosoft.server.repository.adapters;

import java.net.URI;

import org.earthcube.geosoft.shared.classes.Entity;

import edu.isi.wings.ontapi.KBAPI;

public class LocationAdapter extends EntityAdapter {
  
  public LocationAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasURI");
  }
  
  @Override
  public boolean saveEntity(Entity entity) {
    Object value = entity.getValue();
    if (!(value instanceof URI))
      value = URI.create(entity.getValue().toString());
    Entity tmpentity = new Entity(entity.getId(), value, entity.getType());
    return super.saveEntity(tmpentity);
  }
}
