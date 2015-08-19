package org.ontosoft.client.application.training;

import org.ontosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TrainingView extends ParameterizedViewImpl 
  implements TrainingPresenter.MyView {

  interface Binder extends UiBinder<Widget, TrainingView> {
  }

  @Inject
  public TrainingView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
