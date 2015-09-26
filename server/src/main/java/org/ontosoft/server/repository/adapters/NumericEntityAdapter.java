package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.entities.NumericEntity;

import edu.isi.wings.ontapi.KBAPI;

public class NumericEntityAdapter extends EntityAdapter {

  public NumericEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasNumericValue", NumericEntity.class);
  }

}
