package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.Entity;

public interface IEntityAdapter {
  
  Entity getEntity(String id);
  
  boolean saveEntity(Entity entity);
  
}
