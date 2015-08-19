package org.ontosoft.client.application.home;

import org.ontosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeView extends ParameterizedViewImpl implements HomePresenter.MyView {

  interface Binder extends UiBinder<Widget, HomeView> {
  }

  @Inject
  public HomeView(Binder binder) {
    initWidget(binder.createAndBindUi(this));

    initMaterial();
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
