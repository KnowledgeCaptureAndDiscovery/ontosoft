package org.earthcube.geosoft.client.gin;

import org.earthcube.geosoft.client.application.ApplicationModule;
import org.earthcube.geosoft.client.place.NameTokens;

import com.gwtplatform.mvp.client.annotations.DefaultPlace;
import com.gwtplatform.mvp.client.annotations.ErrorPlace;
import com.gwtplatform.mvp.client.annotations.UnauthorizedPlace;
import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;

public class MainModule extends AbstractPresenterModule {
  @Override
  protected void configure() {
    // Singletons
    install(new DefaultModule());
    install(new ApplicationModule());

    // Constants
    bindConstant().annotatedWith(DefaultPlace.class).to(NameTokens.home);
    bindConstant().annotatedWith(ErrorPlace.class).to(NameTokens.router);
    bindConstant().annotatedWith(UnauthorizedPlace.class).to(NameTokens.router);
  }
}
