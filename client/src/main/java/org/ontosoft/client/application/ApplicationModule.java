package org.ontosoft.client.application;

import org.ontosoft.client.application.browse.BrowsePresenter;
import org.ontosoft.client.application.browse.BrowseView;
import org.ontosoft.client.application.community.CommunityPresenter;
import org.ontosoft.client.application.community.CommunityView;
import org.ontosoft.client.application.compare.ComparePresenter;
import org.ontosoft.client.application.compare.CompareView;
import org.ontosoft.client.application.function.compare.CompareFunctionPresenter;
import org.ontosoft.client.application.function.compare.CompareFunctionView;
import org.ontosoft.client.application.function.list.FunctionListPresenter;
import org.ontosoft.client.application.function.list.FunctionListView;
import org.ontosoft.client.application.home.HomePresenter;
import org.ontosoft.client.application.home.HomeView;
import org.ontosoft.client.application.list.SoftwareListPresenter;
import org.ontosoft.client.application.list.SoftwareListView;
import org.ontosoft.client.application.loader.LoaderPresenter;
import org.ontosoft.client.application.loader.LoaderView;
import org.ontosoft.client.application.publish.PublishPresenter;
import org.ontosoft.client.application.publish.PublishView;
import org.ontosoft.client.application.training.TrainingPresenter;
import org.ontosoft.client.application.training.TrainingView;
import org.ontosoft.client.application.users.UserPresenter;
import org.ontosoft.client.application.users.UserView;
import org.ontosoft.client.application.version.browse.VersionBrowsePresenter;
import org.ontosoft.client.application.version.browse.VersionBrowseView;
import org.ontosoft.client.application.version.compare.CompareVersionPresenter;
import org.ontosoft.client.application.version.compare.CompareVersionView;
import org.ontosoft.client.application.version.list.SoftwareVersionListPresenter;
import org.ontosoft.client.application.version.list.SoftwareVersionListView;
import org.ontosoft.client.application.version.publish.PublishVersionPresenter;
import org.ontosoft.client.application.version.publish.PublishVersionView;

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
    bindPresenter(PublishVersionPresenter.class, PublishVersionPresenter.MyView.class,
        PublishVersionView.class, PublishVersionPresenter.MyProxy.class);
    bindPresenter(BrowsePresenter.class, BrowsePresenter.MyView.class,
        BrowseView.class, BrowsePresenter.MyProxy.class);
    bindPresenter(VersionBrowsePresenter.class, VersionBrowsePresenter.MyView.class,
        VersionBrowseView.class, VersionBrowsePresenter.MyProxy.class);
    bindPresenter(CommunityPresenter.class, CommunityPresenter.MyView.class,
        CommunityView.class, CommunityPresenter.MyProxy.class);
    bindPresenter(TrainingPresenter.class, TrainingPresenter.MyView.class,
        TrainingView.class, TrainingPresenter.MyProxy.class);
    bindPresenter(LoaderPresenter.class, LoaderPresenter.MyView.class,
        LoaderView.class, LoaderPresenter.MyProxy.class);
    bindPresenter(SoftwareListPresenter.class, SoftwareListPresenter.MyView.class,
        SoftwareListView.class, SoftwareListPresenter.MyProxy.class);
    bindPresenter(SoftwareVersionListPresenter.class, SoftwareVersionListPresenter.MyView.class,
    	SoftwareVersionListView.class, SoftwareVersionListPresenter.MyProxy.class);
    bindPresenter(FunctionListPresenter.class, FunctionListPresenter.MyView.class,
    		FunctionListView.class, FunctionListPresenter.MyProxy.class);
    bindPresenter(ComparePresenter.class, ComparePresenter.MyView.class,
        CompareView.class, ComparePresenter.MyProxy.class);
    bindPresenter(CompareFunctionPresenter.class, CompareFunctionPresenter.MyView.class,
        CompareFunctionView.class, CompareFunctionPresenter.MyProxy.class);
    bindPresenter(CompareVersionPresenter.class, CompareVersionPresenter.MyView.class,
        CompareVersionView.class, CompareVersionPresenter.MyProxy.class);
    bindPresenter(UserPresenter.class, UserPresenter.MyView.class,
        UserView.class, UserPresenter.MyProxy.class);        
  }

}