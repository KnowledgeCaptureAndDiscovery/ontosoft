package org.ontosoft.shared.api;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.fusesource.restygwt.client.DirectRestService;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.SoftwareVersionSummary;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.provenance.Provenance;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;
import org.ontosoft.shared.classes.permission.Permission;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("")
public interface SoftwareService extends DirectRestService {
  /*
   * Query functions
   */
  @GET
  @Path("software")
  @Produces("application/json")
  public List<SoftwareSummary> list();
  
  @GET
  @Path("versions")
  @Produces("application/json")
  public List<SoftwareVersionSummary> versions();
  
  /*
   * Query functions
   */
  @POST
  @Path("search")
  @Produces("application/json")
  @Consumes("application/json")
  public List<SoftwareSummary> listWithFacets(
      @JsonProperty("facets") List<EnumerationFacet> facets);
  
  @GET
  @Path("software/{name}")
  @Produces("application/json")
  public Software get(@PathParam("name") String name);
  
  @GET
  @Path("software/{name}/version/{version}")
  @Produces("application/json")
  public SoftwareVersion getVersion(@PathParam("name") String name, @PathParam("version") String version);

  @GET
  @Path("software/{name}")
  @Produces("application/rdf+xml")
  public String getGraph(@PathParam("name") String name);
  
  @GET
  @Path("software/{name}/provenance")
  @Produces("application/json")
  public Provenance getProvenance(@PathParam("name") String name);

  @GET
  @Path("software/{name}/provenance")
  @Produces("application/rdf+xml")
  public String getProvenanceGraph(@PathParam("name") String name);
  
  /* versions */
  @POST
  @Path("software/{name}/version")
  @Produces("application/json")
  @Consumes("application/json")
  public Software publishVersion(@PathParam("name") String name, @JsonProperty("version") SoftwareVersion version);
  
  @GET
  @Path("vocabulary")
  @Produces("application/json")
  public Vocabulary getVocabulary();
  
  @GET
  @Path("vocabulary/reload")
  @Produces("text/html")
  public String reloadVocabulary();
  
  @GET
  @Path("software/enumerations")
  @Produces("application/json")
  public Map<String, List<MetadataEnumeration>> getEnumerations();

  @POST
  @Path("software/enumerations/type")
  @Produces("application/json")
  public List<MetadataEnumeration> getEnumerationsForType(@JsonProperty("type") String type);

  /*
   * Edit functions
   */
  @POST
  @Path("software")
  @Produces("application/json")
  @Consumes("application/json")
  public Software publish(@JsonProperty("software") Software software);

  @PUT
  @Path("software/{name}")
  @Produces("application/json")
  @Consumes("application/json")
  public Software update(@PathParam("name") String name,
      @JsonProperty("software") Software software);
  
  @PUT
  @Path("software/{swname}/version/{vname}")
  @Produces("application/json")
  @Consumes("application/json")
  public Software updateVersion(@PathParam("swname") String swname, @PathParam("vname") String vname,
      @JsonProperty("version") SoftwareVersion version);

  @DELETE
  @Path("software/{name}")
  @Produces("text/html")
  public void delete(@PathParam("name") String name);

  @DELETE
  @Path("software/enumerations/{name}")
  @Produces("text/html")
  public void deleteEnumeration(@PathParam("name") String name);
  
  /**
   * Run Plugin
   */
  @POST
  @Path("plugin/{name}/run")
  @Produces("application/json")
  @Consumes("application/json")
  public PluginResponse runPlugin(
      @PathParam("name") String name,
      @JsonProperty("software") Software software);
  
  @GET
  @Path("permission") 
  @Produces("application/json")
  public List<String> getPermissionTypes();
  
  @POST
  @Path("software/{name}/permission")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean setSoftwarePermissionForUser(@PathParam("name") String name, 
		  @JsonProperty("authorization") Authorization authorization);
  
  @GET
  @Path("software/{name}/permission")
  @Produces("application/json")
  @Consumes("application/json")
  public Permission getSoftwarePermissions(@PathParam("name") String name);
  
  @GET
  @Path("software/{name}/permission")
  @Produces("application/rdf+xml")
  public String getSoftwarePermissionsGraph(@PathParam("name") String name);
  
  @GET
  @Path("software/{name}/permission/{username}") 
  @Produces("application/json")  
  @Consumes("application/json")
  public AccessMode getSoftwareAccessLevelForUser(@PathParam("name") String swname, 
		  @PathParam("username") String username);
  
  @GET
  @Path("software/{name}/property/{propname}/permission/{username}") 
  @Produces("application/json")  
  @Consumes("application/json")
  public AccessMode getPropertyAccessLevelForUser(@PathParam("name") String swname, 
    @PathParam("propname") String propname, 
    @PathParam("username") String username);
  
  @POST
  @Path("software/{name}/property/permission")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean setPropertyPermissionForUser(@PathParam("name") String name, 
    @JsonProperty("authorization") Authorization authorization);
  
  @POST
  @Path("software/{name}/owner/permission/{username}")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean addSoftwareOwner(@PathParam("name") String swname, 
    @PathParam("username") String username);
  
  @DELETE
  @Path("software/{name}/owner/permission/{username}")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean removeSoftwareOwner(@PathParam("name") String swname, 
    @PathParam("username") String username);
  
  @GET
  @Path("software/permission/default") 
  @Produces("application/json")  
  @Consumes("application/json")
  public Boolean getPermissionFeatureEnabled();
}
