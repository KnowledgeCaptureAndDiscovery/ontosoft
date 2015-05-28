package org.earthcube.geosoft.client.application.publish;

import org.earthcube.geosoft.client.application.ApplicationPresenter;
import org.earthcube.geosoft.client.application.ParameterizedView;
import org.earthcube.geosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class PublishPresenter extends
    Presenter<PublishPresenter.MyView, PublishPresenter.MyProxy> {
  
  @ProxyCodeSplit
  @NameToken(NameTokens.publish)
  public interface MyProxy extends ProxyPlace<PublishPresenter> { }

  public interface MyView extends ParameterizedView { }
  
  @Inject
  public PublishPresenter(EventBus eventBus, MyView view, final MyProxy proxy,
      final PlaceManager placemanager) {
    super(eventBus, view, proxy, ApplicationPresenter.TYPE_SetMainContent);
  }
}
