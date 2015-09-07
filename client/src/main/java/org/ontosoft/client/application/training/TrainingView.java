package org.ontosoft.client.application.training;

import org.gwtbootstrap3.client.ui.PageHeader;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TrainingView extends ParameterizedViewImpl 
  implements TrainingPresenter.MyView {

  @UiField
  PageHeader title;
  
  interface Binder extends UiBinder<Widget, TrainingView> {
  }

  @Inject
  public TrainingView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    title.setText(Config.getPortalTitle() + " Training");
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
}
