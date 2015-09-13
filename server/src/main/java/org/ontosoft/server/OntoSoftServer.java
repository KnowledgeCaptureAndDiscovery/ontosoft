package org.ontosoft.server;

import javax.annotation.PreDestroy;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.ontosoft.server.api.impl.SoftwareResource;
import org.ontosoft.server.filters.AcceptHeaderFilter;
import org.ontosoft.server.filters.CORSResponseFilter;
import org.ontosoft.server.filters.UserAuthenticationFilter;
import org.ontosoft.server.users.UserDatabase;

class OntoSoftServer extends ResourceConfig {

  public OntoSoftServer() {
    register(AcceptHeaderFilter.class);
    register(CORSResponseFilter.class);
    register(RolesAllowedDynamicFeature.class);
    register(UserAuthenticationFilter.class);
    register(SoftwareResource.class);
  }

  @PreDestroy
  public void onDestroy() {
    // Cleanup tasks
    UserDatabase.shutdownDB();
  }

}
