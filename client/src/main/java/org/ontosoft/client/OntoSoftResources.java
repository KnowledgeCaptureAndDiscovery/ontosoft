package org.ontosoft.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

public interface OntoSoftResources extends ClientBundle {
  public static final OntoSoftResources INSTANCE =  GWT.create(OntoSoftResources.class);

  @Source("html/home.html")
  public TextResource home();
}
