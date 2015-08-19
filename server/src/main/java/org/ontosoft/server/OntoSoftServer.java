package org.ontosoft.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.ontosoft.server.api.impl.SoftwareResource;
import org.ontosoft.server.filters.CORSResponseFilter;

class OntoSoftServer extends ResourceConfig {
	
	public OntoSoftServer() {
		register(SoftwareResource.class);
		register(CORSResponseFilter.class);
	}
}
