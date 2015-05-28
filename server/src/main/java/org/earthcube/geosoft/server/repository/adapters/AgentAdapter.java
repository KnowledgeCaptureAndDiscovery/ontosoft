package org.earthcube.geosoft.server.repository.adapters;

import edu.isi.wings.ontapi.KBAPI;

public class AgentAdapter extends EnumerationEntityAdapter {

  public AgentAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String clsid) {
    super(kb, ontkb, enumkb, clsid);
  }

}
