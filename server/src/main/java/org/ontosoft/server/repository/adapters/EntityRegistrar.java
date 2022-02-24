package org.ontosoft.server.repository.adapters;

import java.util.HashMap;

import edu.isi.kcap.ontapi.KBAPI;

public class EntityRegistrar {
  private static HashMap<String, Class<IEntityAdapter>> adapters = 
      new HashMap<String, Class<IEntityAdapter>>();
  
  public static void clear() {
    adapters.clear();
  }
  
  public static void register(String classid, Class<IEntityAdapter> adapter) {
    adapters.put(classid, adapter);
  }
  
  public static Class<IEntityAdapter> get(String classid) {
    return adapters.get(classid);
  }
  
  public static IEntityAdapter getAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String classid) {
    Class<IEntityAdapter> adapterClass = EntityRegistrar.get(classid);
    try {
      return adapterClass.getConstructor(KBAPI.class, KBAPI.class, String.class)
          .newInstance(kb, ontkb, classid);
    } catch(Exception e1) {
      try {
        return adapterClass.getConstructor(KBAPI.class, KBAPI.class, KBAPI.class, String.class)
            .newInstance(kb, ontkb, enumkb, classid);
      } catch(Exception e2) {
        e1.printStackTrace();
        e2.printStackTrace();
      }
    }
    return null;
  }
}
