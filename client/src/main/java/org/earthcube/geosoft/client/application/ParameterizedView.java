package org.earthcube.geosoft.client.application;

import com.gwtplatform.mvp.client.View;

public interface ParameterizedView extends View {
  public void initializeParameters(String[] parameters);
}
