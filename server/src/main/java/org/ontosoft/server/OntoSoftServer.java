package org.ontosoft.server;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;

import javax.annotation.PreDestroy;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.ontosoft.server.api.impl.SoftwareResource;
import org.ontosoft.server.api.impl.UserResource;
import org.ontosoft.server.filters.AcceptHeaderFilter;
import org.ontosoft.server.filters.CORSResponseFilter;
import org.ontosoft.server.filters.UserAuthenticationFilter;
import org.ontosoft.server.users.UserDatabase;

class OntoSoftServer extends ResourceConfig {

  public OntoSoftServer() {
    // Headers
    register(AcceptHeaderFilter.class);
    register(CORSResponseFilter.class);
    
    // User roles and authentication
    register(RolesAllowedDynamicFeature.class);
    register(UserAuthenticationFilter.class);

    // Main Resources
    register(UserResource.class);
    register(SoftwareResource.class);
    
    // OpenAPI documentation
    register(ApiListingResource.class);   
    register(SwaggerSerializers.class);
    initializeSwagger();
  }
  
  private void initializeSwagger() {
    BeanConfig beanConfig = new BeanConfig();
    beanConfig.setVersion("1.0.0");
    beanConfig.setSchemes(new String[]{"http"});
    beanConfig.setBasePath("/repository");
    beanConfig.setResourcePackage("org.ontosoft.server.api.impl");
    beanConfig.setScan(true);    
  }

  @PreDestroy
  public void onDestroy() {
    // Cleanup tasks
    UserDatabase.shutdownDB();
  }

}
