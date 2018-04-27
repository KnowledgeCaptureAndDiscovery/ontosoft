package org.ontosoft.client.application.versions;

import org.ontosoft.client.application.ApplicationPresenter;
import org.ontosoft.client.application.ParameterizedView;
import org.ontosoft.client.place.NameTokens;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;

public class SoftwareVersionListPresenter extends
    Presenter<SoftwareVersionListPresenter.MyView, SoftwareVersionListPresenter.MyProxy> {

  @ProxyCodeSplit
  @NameToken(NameTokens.versions)
  public interface MyProxy extends ProxyPlace<SoftwareVersionListPresenter> {
  }

  public interface MyView extends ParameterizedView {
  }

  @Inject
  public SoftwareVersionListPresenter(EventBus eventBus, MyView view, MyProxy proxy) {
    super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
  }
}
