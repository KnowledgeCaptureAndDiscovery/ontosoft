package org.earthcube.geosoft.server.repository.adapters;

import org.earthcube.geosoft.shared.classes.Entity;

public interface IEntityAdapter {
  
  Entity getEntity(String id);
  
  boolean saveEntity(Entity entity);
  
}
