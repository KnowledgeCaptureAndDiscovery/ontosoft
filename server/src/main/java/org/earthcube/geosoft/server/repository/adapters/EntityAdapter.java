package org.earthcube.geosoft.server.repository.adapters;

import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.util.KBConstants;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class EntityAdapter implements IEntityAdapter {
  protected KBAPI kb;
  protected KBAPI ontkb;
  protected KBAPI enumkb;
  protected KBObject valueProperty;
  protected KBObject entityClass;
  
  protected static String ontns = KBConstants.ONTNS();
  
  public EntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, 
      String clsid, String valpropid) {
    this.kb = kb;
    this.ontkb = ontkb;
    this.enumkb = enumkb;
    this.setEntityClass(clsid);
    this.setEntityValueProperty(valpropid);
  }

  protected void setEntityClass(String clsid) {
    if(clsid != null)
      this.entityClass = this.ontkb.getConcept(clsid);
  }
  
  protected void setEntityValueProperty(String valpropid) {
    if(valpropid != null)
      this.valueProperty = this.ontkb.getProperty(valpropid);
  }
  
  @Override
  public Entity getEntity(String id) {
    KBObject entityobj = this.kb.getIndividual(id);
    KBObject valobj = this.kb.getPropertyValue(entityobj, this.valueProperty);
    return new Entity(id, valobj.getValue(), entityClass.getID());
  }
  
  @Override
  public boolean saveEntity(Entity entity) {
    KBObject entityobj = this.kb.createObjectOfClass(entity.getId(), entityClass);
    if(entity.getValue() == null)
      return false;
    KBObject valobj = this.kb.createLiteral(entity.getValue());
    this.kb.setPropertyValue(entityobj, this.valueProperty, valobj);
    return true;
  }
}
