package org.earthcube.geosoft.server.repository.adapters;

import edu.isi.wings.ontapi.KBAPI;

public class TextEntityAdapter extends EntityAdapter {
  
  public TextEntityAdapter(KBAPI kb, KBAPI ontkb, String clsid) {
    super(kb, ontkb, null, clsid, ontns+"hasTextValue");
  }

}
