package org.ontosoft.server.repository.adapters;

import java.net.URI;

import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.Location;

import edu.isi.kcap.ontapi.KBAPI;

public class LocationAdapter extends EntityAdapter {
  
  public LocationAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasURI", Location.class);
  }
  
  @Override
  public boolean saveEntity(Entity entity) {
    if (!(entity.getValue() instanceof URI))
      entity.setValue(URI.create(entity.getValue().toString()));
    return super.saveEntity(entity);
  }
}
