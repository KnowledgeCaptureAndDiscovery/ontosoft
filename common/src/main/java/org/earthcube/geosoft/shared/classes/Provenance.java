package org.earthcube.geosoft.shared.classes;

import java.util.Date;

public class Provenance {
  public Date timestamp;
  public String author;

  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }
}
