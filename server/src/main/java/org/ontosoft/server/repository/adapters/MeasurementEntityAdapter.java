package org.ontosoft.server.repository.adapters;

import java.util.HashMap;

import org.ontosoft.shared.classes.Entity;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class MeasurementEntityAdapter extends NumericEntityAdapter {

  protected KBObject unitsProperty;
  private String valueKey = "value";
  private String unitsKey = "units";
  
  public MeasurementEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, clsid);
    this.unitsProperty = ontkb.getProperty(ontns + "hasUnits");
  }

  @Override
  public Entity getEntity(String id) {
    KBObject entityobj = this.kb.getIndividual(id);
    HashMap<String, Object> value = new HashMap<String, Object>();
    KBObject valobj = this.kb.getPropertyValue(entityobj, this.valueProperty);
    KBObject unitsobj = this.kb.getPropertyValue(entityobj, this.unitsProperty);
    value.put(valueKey, valobj.getValue());
    value.put(unitsKey, unitsobj.getValue());
    return new Entity(id, value, this.entityClass.getID());

  }

  
  @Override
  @SuppressWarnings("unchecked")
  public boolean saveEntity(Entity entity) {
    if(entity.getValue() instanceof HashMap) {
      HashMap<String, Object> value = (HashMap<String, Object>) entity.getValue();
      Entity valentity = new Entity(entity.getId(), value.get(valueKey), entity.getType());
      // Save value
      if(super.saveEntity(valentity)) {
        // Save units
        KBObject entityobj = this.kb.getIndividual(entity.getId());
        KBObject unitsobj = this.kb.createLiteral(value.get(unitsKey));
        if(unitsobj != null) {
          this.kb.setPropertyValue(entityobj, this.unitsProperty, unitsobj);
          return true;
        }
      }
    }
    return false;
  }
}
