package org.earthcube.geosoft.client.application.router;

import org.earthcube.geosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class RouterView extends ParameterizedViewImpl 
  implements RouterPresenter.MyView {

  interface Binder extends UiBinder<Widget, RouterView> {
  }

  @Inject
  public RouterView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
