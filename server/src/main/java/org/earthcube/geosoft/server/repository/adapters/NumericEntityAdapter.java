package org.earthcube.geosoft.server.repository.adapters;

import org.earthcube.geosoft.shared.classes.Entity;

import edu.isi.wings.ontapi.KBAPI;

public class NumericEntityAdapter extends EntityAdapter {

  public NumericEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasNumericValue");
  }

  @Override
  public boolean saveEntity(Entity entity) {
    Object value = entity.getValue();
    if (!(value instanceof Float))
      value = new Float(entity.getValue().toString());
    Entity tmpentity = new Entity(entity.getId(), value, entity.getType());
    return super.saveEntity(tmpentity);
  }
}
