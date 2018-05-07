package org.ontosoft.server.repository.adapters;

import java.util.List;

import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.util.KBConstants;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public abstract class EntityAdapter implements IEntityAdapter {
  protected KBAPI kb;
  protected KBAPI ontkb;
  protected KBAPI enumkb;
  protected KBObject kbValueProperty;
  protected KBObject kbClass;
  protected Class<? extends Entity> entityClass;
  
  protected static String ontns = KBConstants.ONTNS();
  
  public EntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, 
      String clsid, String valpropid, Class<? extends Entity> entityClass) {
    this.kb = kb;
    this.ontkb = ontkb;
    this.enumkb = enumkb;
    this.setKBClass(clsid);
    this.entityClass = entityClass;
    this.setKBValueProperty(valpropid);
  }

  protected void setKBClass(String clsid) {
    if(clsid != null)
      this.kbClass = this.ontkb.getConcept(clsid);
  }
  
  protected void setKBValueProperty(String valpropid) {
    if(valpropid != null)
      this.kbValueProperty = this.ontkb.getProperty(valpropid);
  }
  
  protected Entity fetchEntityDetailsFromKB(Entity entity) {
    return this.fetchEntityDetailsFromKB(entity, this.kb);
  }
  
  protected Entity fetchEntityDetailsFromKB(Entity entity, KBAPI kb) {
    KBObject entityobj = kb.getIndividual(entity.getId());
    
    if(entityobj != null) {
      List<KBObject> types = this.ontkb.getAllClassesOfInstance(entityobj, false);
      entity.setType(this.kbClass.getID());
      entity.setLabel(kb.getLabel(entityobj));
      entity.setName(entityobj.getName());
      KBObject valobj = kb.getPropertyValue(entityobj, this.kbValueProperty);
      if(valobj != null)
        entity.setValue(valobj.getValue());
    }
    return entity;
  }
  
  protected boolean saveEntityInKB(Entity entity) {
    return this.saveEntityInKB(entity, this.kb);
  }
  
  protected boolean saveEntityInKB(Entity entity, KBAPI kb) {
    KBObject entityobj = kb.createObjectOfClass(entity.getId(), kbClass);
    if(entity.getValue() != null) {
      KBObject valobj = kb.createLiteral(entity.getValue());
      kb.setPropertyValue(entityobj, this.kbValueProperty, valobj);
    }
    return true;
  }


  @Override
  public Entity getEntity(String id) {
    try {
      Entity entity = entityClass.newInstance();
      entity.setId(id);
      return this.fetchEntityDetailsFromKB(entity);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public boolean saveEntity(Entity entity) {
    return this.saveEntityInKB(entity);
  }
}
