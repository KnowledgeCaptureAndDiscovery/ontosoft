package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.entities.Entity;

public interface IEntityAdapter {
  
  Entity getEntity(String id);
  
  boolean saveEntity(Entity entity);
  
}
