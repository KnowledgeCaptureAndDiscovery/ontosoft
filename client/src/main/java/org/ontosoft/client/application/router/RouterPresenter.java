package org.ontosoft.client.application.router;

import java.util.Arrays;

import org.ontosoft.client.application.ApplicationPresenter;
import org.ontosoft.client.application.ParameterizedView;
import org.ontosoft.client.application.browse.BrowseView;
import org.ontosoft.client.application.compare.CompareView;
import org.ontosoft.client.application.list.SoftwareListView;
import org.ontosoft.client.application.publish.PublishView;
import org.ontosoft.client.place.NameTokens;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class RouterPresenter extends
    Presenter<RouterPresenter.MyView, RouterPresenter.MyProxy> {

  @ProxyCodeSplit
  @NameToken(NameTokens.router)
  public interface MyProxy extends ProxyPlace<RouterPresenter> {
  }

  public interface MyView extends ParameterizedView {
  }

  @Inject
  public RouterPresenter(EventBus eventBus, MyView view, MyProxy proxy,
      final PlaceManager placemanager, final PublishView publishview,
      final BrowseView browseview, final SoftwareListView listview, 
      final CompareView compareview) {
    super(eventBus, view, proxy, ApplicationPresenter.CONTENT_SLOT);
    
    final PlaceRequest.Builder builder = new PlaceRequest.Builder();

    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(final ValueChangeEvent<String> event) {
        String token = event.getValue();
        String[] tokens = token.split("/");
        String nametoken = tokens[0];
        ParameterizedView sectionview = null;
        if(nametoken.equals(NameTokens.publish))
          sectionview = publishview;
        else if(nametoken.equals(NameTokens.browse))
          sectionview = browseview;
        else if(nametoken.equals(NameTokens.list))
          sectionview = listview;        
        else if(nametoken.equals(NameTokens.compare))
          sectionview = compareview;

        if(sectionview != null) {
          placemanager.revealPlace(builder.nameToken(nametoken).build(), false);
          String[] params = new String[] {};
          if (tokens.length > 1)
            params = Arrays.copyOfRange(tokens, 1, tokens.length);
          sectionview.initializeParameters(params);
        }
      }
    });
    if(History.getToken() !=  null)
      History.fireCurrentHistoryState();
  }
  
}
