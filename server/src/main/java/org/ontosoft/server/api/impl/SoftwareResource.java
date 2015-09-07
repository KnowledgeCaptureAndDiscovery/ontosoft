package org.ontosoft.server.api.impl;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.ontosoft.server.repository.SoftwareRepository;
import org.ontosoft.shared.api.SoftwareService;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.Plugin;
import org.ontosoft.shared.plugins.PluginRegistrar;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("software")
public class SoftwareResource implements SoftwareService {

  @Context
  HttpServletResponse response;
  @Context
  HttpServletRequest request;

  SoftwareRepository repo;
  
  public SoftwareResource(@Context HttpServletRequest req) {
    this.repo = SoftwareRepository.get(req);
  }

  /**
   * Queries
   */

  // TODO: Check User Authentication security here
  @GET
  @Produces("application/json")
  @Override
  public List<SoftwareSummary> list() {
    try {
      return this.repo.getAllSoftware();
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }

  @GET
  @Path("{name}")
  @Produces("application/json")
  @Override
  public Software get(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      return this.repo.getSoftware(swid);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }

  @GET
  @Path("{name}")
  @Produces("application/rdf+xml")
  @Override
  public String getGraph(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      return this.repo.serializeXML(swid);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }
  
  @GET
  @Path("vocabulary")
  @Produces("application/json")
  @Override
  public Vocabulary getVocabulary() {
    try {
      Vocabulary vocab = this.repo.getVocabulary();
      if(vocab.isNeedsReload()) {
        System.out.println("Vocabulary needs reloading !!");
        vocab.setNeedsReload(false);
      }
      return vocab;
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }
  
  @GET
  @Path("vocabulary/reload")
  @Produces("text/html")
  @Override
  public void reloadVocabulary() {
    try {
      this.repo.reloadKBCaches();
      this.repo.initializeVocabularyFromKB();
      response.sendRedirect("");
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }
  
  @POST
  @Path("list/facets")
  @Produces("application/json")
  @Consumes("application/json")
  @Override
  public List<SoftwareSummary> listWithFacets(@JsonProperty("facets") List<EnumerationFacet> facets) {
    try {
      return this.repo.getAllSoftwareWithFacets(facets);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }
  
  @GET
  @Path("type/enumerations")
  @Produces("application/json")
  @Override
  public List<MetadataEnumeration> getEnumerationsForType(@QueryParam("type") String type) {
    if(!type.startsWith("http:"))
      type = KBConstants.ONTNS() + type;
    return this.repo.getEnumerationsForType(type);
  }

  @GET
  @Path("enumerations")
  @Produces("application/json")
  public Map<String, List<MetadataEnumeration>> getEnumerations() {
    return this.repo.getEnumerations();
  }
  
  /**
   * Edits
   */

  @POST
  @Produces("application/json")
  @Consumes("application/json")
  @Override
  public Software publish(@JsonProperty("software") Software software) {
    try {
      String swid = this.repo.addSoftware(software);
      if(swid != null) {
        software.setId(swid);
        return this.repo.getSoftware(swid);
        //response.sendRedirect(swid);
        //return software;
      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception in add: " + e.getMessage());
    }
  }

  @PUT
  @Path("{name}")
  @Consumes("application/json")
  @Produces("application/json")
  @Override
  public Software update(@PathParam("name") String name,
      @JsonProperty("software") Software software) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      if (!this.repo.updateSoftware(software, swid))
        throw new RuntimeException("Could not update " + name);
      return software;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception in update: " + e.getMessage());
    }
  }

  @DELETE
  @Path("{name}")
  @Produces("text/html")
  @Override
  public void delete(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      if (!this.repo.deleteSoftware(swid))
        throw new RuntimeException("Could not delete " + name);
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception in delete: " + e.getMessage());
    }
  }
  
  @DELETE
  @Path("enumerations/{name}")
  @Produces("text/html")
  public void deleteEnumeration(@PathParam("name") String name) {
    try {
      String enumid = name;
      if(!name.startsWith("http:"))
        enumid = repo.ENUMNS() + name;
      if (!this.repo.deleteEnumeration(enumid))
        throw new RuntimeException("Could not delete " + name);
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception in delete: " + e.getMessage());
    }    
  }
  
  /**
   * Query via POST
   */
  @POST
  @Path("plugin/{name}/run")
  @Produces("application/json")
  @Consumes("application/json")
  @Override  
  public PluginResponse runPlugin(
      @PathParam("name") String name,
      @JsonProperty("software") Software software) {
    Plugin plugin = PluginRegistrar.getPluginByName(name);
    if(plugin != null) {
      PluginResponse response = plugin.run(software);
      if(response != null)
        response.setSoftwareInfoFromSoftware(software);
      return response;
    }
    return null;
  }
  
  /**
   * Exports
   */
  // TODO: Export Queries (Should do IP based authentication here -- verify with repo exports list)
}
