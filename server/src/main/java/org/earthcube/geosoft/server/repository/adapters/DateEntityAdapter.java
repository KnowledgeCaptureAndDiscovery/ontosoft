package org.earthcube.geosoft.server.repository.adapters;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.earthcube.geosoft.shared.classes.Entity;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class DateEntityAdapter extends EntityAdapter {

  public DateEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasDateValue");
  }
  
  @Override
  public boolean saveEntity(Entity entity) {
    try {
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      Object value = entity.getValue();
      if(!(value instanceof Date))
        value = fmt.parse(value.toString());
      Calendar cal = Calendar.getInstance();
      cal.setTime((Date)value);
      XSDDateTime dtime = new XSDDateTime(cal);
      Entity tmpentity = new Entity(entity.getId(), dtime, entity.getType());
      return super.saveEntity(tmpentity);
    } catch (ParseException e) {
      e.printStackTrace();
    }
    return false;
  }
  
  @Override
  public Entity getEntity(String id) {
    KBObject entityobj = this.kb.getIndividual(id);
    KBObject valobj = this.kb.getPropertyValue(entityobj, this.valueProperty);
    if(valobj.getValue() != null) {
      Date date = ((XSDDateTime)valobj.getValue()).asCalendar().getTime();
      SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
      return new Entity(id, fmt.format(date), this.entityClass.getID());
    }
    return null;
  }
}
