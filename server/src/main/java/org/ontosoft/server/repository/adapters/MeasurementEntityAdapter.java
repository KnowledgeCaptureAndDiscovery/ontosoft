package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.MeasurementEntity;

import edu.isi.kcap.ontapi.KBAPI;
import edu.isi.kcap.ontapi.KBObject;

public class MeasurementEntityAdapter extends EntityAdapter {

  protected KBObject unitsProperty;

  public MeasurementEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasNumericValue", MeasurementEntity.class);
    this.unitsProperty = ontkb.getProperty(ontns + "hasUnits");
  }

  @Override
  public Entity getEntity(String id) {
    MeasurementEntity entity = (MeasurementEntity) super.getEntity(id);
    KBObject entityobj = this.kb.getIndividual(id);
    KBObject unitsobj = this.kb.getPropertyValue(entityobj, this.unitsProperty);
    if(unitsobj != null && unitsobj.getValue() != null)
      entity.setUnits(unitsobj.getValue().toString());
    return entity;
  }

  
  @Override
  public boolean saveEntity(Entity entity) {
    MeasurementEntity mentity = (MeasurementEntity) entity;
    if (super.saveEntity(mentity)) {
      // Save units
      KBObject entityobj = this.kb.getIndividual(mentity.getId());
      KBObject unitsobj = this.kb.createLiteral(mentity.getUnits());
      if (unitsobj != null) {
        this.kb.setPropertyValue(entityobj, this.unitsProperty, unitsobj);
        return true;
      }
    }
    return false;
  }
}
