package org.ontosoft.client.application.home;

import org.ontosoft.client.OntoSoftResources;
import org.ontosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class HomeView extends ParameterizedViewImpl implements HomePresenter.MyView {

  @UiField
  HTML html;
  
  interface Binder extends UiBinder<Widget, HomeView> {
  }

  @Inject
  public HomeView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initMaterial();
    html.setHTML(OntoSoftResources.INSTANCE.home().getText());
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
