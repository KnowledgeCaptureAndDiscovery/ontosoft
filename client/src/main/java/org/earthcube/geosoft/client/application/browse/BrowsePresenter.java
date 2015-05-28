package org.earthcube.geosoft.client.application.browse;

import org.earthcube.geosoft.client.application.ApplicationPresenter;
import org.earthcube.geosoft.client.application.ParameterizedView;
import org.earthcube.geosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class BrowsePresenter extends
    Presenter<BrowsePresenter.MyView, BrowsePresenter.MyProxy> {

  @ProxyCodeSplit
  @NameToken(NameTokens.browse)
  public interface MyProxy extends ProxyPlace<BrowsePresenter> { }

  public interface MyView extends ParameterizedView { }
  
  @Inject
  public BrowsePresenter(EventBus eventBus, MyView view, MyProxy proxy) {
    super(eventBus, view, proxy, ApplicationPresenter.TYPE_SetMainContent);
  }
}
