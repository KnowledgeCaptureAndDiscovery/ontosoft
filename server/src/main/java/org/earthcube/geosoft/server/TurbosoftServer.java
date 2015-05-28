package org.earthcube.geosoft.server;

import org.glassfish.jersey.server.ResourceConfig;
import org.earthcube.geosoft.server.api.impl.SoftwareResource;
import org.earthcube.geosoft.server.filters.CORSResponseFilter;

class TurbosoftServer extends ResourceConfig {
	
	public TurbosoftServer() {
		register(SoftwareResource.class);
		register(CORSResponseFilter.class);
	}
}
