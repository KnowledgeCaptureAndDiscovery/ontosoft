package org.earthcube.geosoft.client.application.home;

import org.earthcube.geosoft.client.application.ApplicationPresenter;
import org.earthcube.geosoft.client.application.ParameterizedView;
import org.earthcube.geosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class HomePresenter extends
    Presenter<HomePresenter.MyView, HomePresenter.MyProxy> {

  @ProxyCodeSplit
  @NameToken(NameTokens.home)
  public interface MyProxy extends ProxyPlace<HomePresenter> {
  }

  public interface MyView extends ParameterizedView {
  }

  @Inject
  public HomePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
    super(eventBus, view, proxy, ApplicationPresenter.TYPE_SetMainContent);
  }

}
