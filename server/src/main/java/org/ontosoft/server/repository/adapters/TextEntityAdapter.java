package org.ontosoft.server.repository.adapters;

import org.ontosoft.shared.classes.entities.TextEntity;

import edu.isi.kcap.ontapi.KBAPI;

public class TextEntityAdapter extends EntityAdapter {
  public TextEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasTextValue", TextEntity.class);
  }
}
