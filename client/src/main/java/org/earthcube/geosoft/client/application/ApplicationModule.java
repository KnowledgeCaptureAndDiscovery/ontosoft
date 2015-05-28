package org.earthcube.geosoft.client.application;

import org.earthcube.geosoft.client.application.browse.BrowsePresenter;
import org.earthcube.geosoft.client.application.browse.BrowseView;
import org.earthcube.geosoft.client.application.community.CommunityPresenter;
import org.earthcube.geosoft.client.application.community.CommunityView;
import org.earthcube.geosoft.client.application.compare.ComparePresenter;
import org.earthcube.geosoft.client.application.compare.CompareView;
import org.earthcube.geosoft.client.application.home.HomePresenter;
import org.earthcube.geosoft.client.application.home.HomeView;
import org.earthcube.geosoft.client.application.list.SoftwareListPresenter;
import org.earthcube.geosoft.client.application.list.SoftwareListView;
import org.earthcube.geosoft.client.application.publish.PublishPresenter;
import org.earthcube.geosoft.client.application.publish.PublishView;
import org.earthcube.geosoft.client.application.router.RouterPresenter;
import org.earthcube.geosoft.client.application.router.RouterView;
import org.earthcube.geosoft.client.application.training.TrainingPresenter;
import org.earthcube.geosoft.client.application.training.TrainingView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

public class ApplicationModule extends AbstractPresenterModule {
  
  @Override
  protected void configure() {
    // Main Application
    bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, 
        ApplicationView.class, ApplicationPresenter.MyProxy.class);
    
    // Home, Publish, Browse, Community, Training, //Search
    bindPresenter(HomePresenter.class, HomePresenter.MyView.class,
        HomeView.class, HomePresenter.MyProxy.class);
    bindPresenter(PublishPresenter.class, PublishPresenter.MyView.class,
        PublishView.class, PublishPresenter.MyProxy.class);
    bindPresenter(BrowsePresenter.class, BrowsePresenter.MyView.class,
        BrowseView.class, BrowsePresenter.MyProxy.class);
    bindPresenter(CommunityPresenter.class, CommunityPresenter.MyView.class,
        CommunityView.class, CommunityPresenter.MyProxy.class);
    bindPresenter(TrainingPresenter.class, TrainingPresenter.MyView.class,
        TrainingView.class, TrainingPresenter.MyProxy.class);
    bindPresenter(RouterPresenter.class, RouterPresenter.MyView.class,
        RouterView.class, RouterPresenter.MyProxy.class);
    bindPresenter(SoftwareListPresenter.class, SoftwareListPresenter.MyView.class,
        SoftwareListView.class, SoftwareListPresenter.MyProxy.class);
    bindPresenter(ComparePresenter.class, ComparePresenter.MyView.class,
        CompareView.class, ComparePresenter.MyProxy.class);    
  }

}