package org.earthcube.geosoft.client.application.compare;

import org.earthcube.geosoft.client.application.ApplicationPresenter;
import org.earthcube.geosoft.client.application.ParameterizedView;
import org.earthcube.geosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class ComparePresenter extends
    Presenter<ComparePresenter.MyView, ComparePresenter.MyProxy> {

  @ProxyStandard
  @NameToken(NameTokens.compare)
  public interface MyProxy extends ProxyPlace<ComparePresenter> {
  }

  public interface MyView extends ParameterizedView {
  }

  @Inject
  public ComparePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
    super(eventBus, view, proxy, ApplicationPresenter.TYPE_SetMainContent);
  }
}
