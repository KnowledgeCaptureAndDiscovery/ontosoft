package org.ontosoft.client.application.loader;

import org.ontosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LoaderView extends ParameterizedViewImpl 
  implements LoaderPresenter.MyView {

  interface Binder extends UiBinder<Widget, LoaderView> {
  }

  @Inject
  public LoaderView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
