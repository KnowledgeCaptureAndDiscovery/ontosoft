package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.entities.DateEntity;
import edu.isi.kcap.ontapi.KBAPI;

public class DateEntityAdapter extends EntityAdapter {

  public DateEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasDateValue", DateEntity.class);
  }
  
}
