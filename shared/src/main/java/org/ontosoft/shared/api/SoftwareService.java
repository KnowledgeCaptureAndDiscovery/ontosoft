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
import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.DirectRestService;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("software")
public interface SoftwareService extends DirectRestService {
  /*
   * Query functions
   */
  @GET
  @Produces("application/json")
  public List<SoftwareSummary> list();

  @GET
  @Path("{name}")
  @Produces("application/json")
  public Software get(@PathParam("name") String name);

  @GET
  @Path("{name}")
  @Produces("application/rdf+xml")
  public String getGraph(@PathParam("name") String name);
  
  @GET
  @Path("vocabulary")
  @Produces("application/json")
  public Vocabulary getVocabulary();
  
  @GET
  @Path("vocabulary/reload")
  @Produces("text/html")
  public void reloadVocabulary();

  @GET
  @Path("type/enumerations")
  @Produces("application/json")
  public List<MetadataEnumeration> getEnumerationsForType(@QueryParam("type") String type);
  
  @GET
  @Path("enumerations")
  @Produces("application/json")
  public Map<String, List<MetadataEnumeration>> getEnumerations();
    
  @POST
  @Path("list/facets")
  @Produces("application/json")
  @Consumes("application/json")
  public List<SoftwareSummary> listWithFacets(
      @JsonProperty("facets") List<EnumerationFacet> facets);

  /*
   * Edit functions
   */
  @POST
  @Produces("application/json")
  @Consumes("application/json")
  public Software publish(@JsonProperty("software") Software software);

  @PUT
  @Path("{name}")
  @Produces("application/json")
  @Consumes("application/json")
  public Software update(@PathParam("name") String name,
      @JsonProperty("software") Software software);

  @DELETE
  @Path("{name}")
  @Produces("text/html")
  public void delete(@PathParam("name") String name);

  
  @DELETE
  @Path("enumerations/{name}")
  @Produces("text/html")
  public void deleteEnumeration(@PathParam("name") String name);
  
  
  /*
   * Query via POST
   */
  
  @POST
  @Path("plugin/{name}/run")
  @Produces("application/json")
  @Consumes("application/json")
  public PluginResponse runPlugin(
      @PathParam("name") String name,
      @JsonProperty("software") Software software);
}