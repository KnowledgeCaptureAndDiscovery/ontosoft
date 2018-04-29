package org.ontosoft.client.application;

import java.util.Arrays;

import org.ontosoft.client.application.browse.BrowseView;
import org.ontosoft.client.application.version.browse.VersionBrowseView;
import org.ontosoft.client.application.version.list.SoftwareVersionListView;
import org.ontosoft.client.application.version.publish.PublishVersionView;
import org.ontosoft.client.application.compare.CompareView;
import org.ontosoft.client.application.list.SoftwareListView;
import org.ontosoft.client.application.publish.PublishView;
import org.ontosoft.client.application.users.UserView;
import org.ontosoft.client.place.NameTokens;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class ApplicationPresenter extends
    Presenter<ApplicationPresenter.MyView, ApplicationPresenter.MyProxy> {

  @ProxyCodeSplit
  public interface MyProxy extends Proxy<ApplicationPresenter> {
  }

  public interface MyView extends ParameterizedView {
  }

  public static final NestedSlot CONTENT_SLOT = new NestedSlot();

  @Inject
  public ApplicationPresenter(EventBus eventBus, final MyView view, MyProxy proxy,
      final PlaceManager placemanager, final PublishView publishview, final PublishVersionView publishversionview,
      final BrowseView browseview, final VersionBrowseView versionbrowseview, final SoftwareListView listview,
      final SoftwareVersionListView versionlistview,
      final CompareView compareview, final UserView userview) {
    super(eventBus, view, proxy, RevealType.Root);
    
    final PlaceRequest.Builder builder = new PlaceRequest.Builder();
    
    // Add history change handler
    History.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(final ValueChangeEvent<String> event) {
        String token = event.getValue();
        String[] tokens = token.split("/");
        String nametoken = tokens[0];
        ParameterizedView sectionview = null;
        if(nametoken.equals(NameTokens.publish))
          sectionview = publishview;
        else if(nametoken.equals(NameTokens.publishversion))
            sectionview = publishversionview;
        else if(nametoken.equals(NameTokens.browse))
          sectionview = browseview;
        else if(nametoken.equals(NameTokens.version))
    	  sectionview = versionbrowseview;
        else if(nametoken.equals(NameTokens.versions))
          sectionview = versionlistview;
        else if(nametoken.equals(NameTokens.list))
          sectionview = listview;        
        else if(nametoken.equals(NameTokens.compare))
          sectionview = compareview;
        else if(nametoken.equals(NameTokens.users))
          sectionview = userview;        

        // Reveal called view with parameters
        if(sectionview != null) {
          placemanager.revealPlace(builder.nameToken(nametoken).build(), false);
          String[] params = new String[] {};
          if (tokens.length > 1)
            params = Arrays.copyOfRange(tokens, 1, tokens.length);
          sectionview.initializeParameters(params);
        }
        
        // Tell application view about the parameters in case it wants to change something
        view.initializeParameters(tokens);
      }
    });
    if(History.getToken() !=  null)
      History.fireCurrentHistoryState();
  }
}
