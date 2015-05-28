package org.earthcube.geosoft.server.repository;

import java.nio.ByteBuffer;
import java.util.UUID;

public class EntityUtilities {
  public static String shortUUID() {
    UUID uuid = UUID.randomUUID();
    long l = ByteBuffer.wrap(uuid.toString().getBytes()).getLong();
    return Long.toString(l, Character.MAX_RADIX);
  }
}
