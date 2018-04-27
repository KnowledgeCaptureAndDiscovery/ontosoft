package org.ontosoft.server.api.impl;

import io.swagger.annotations.Api;

import java.util.List;
import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import org.ontosoft.server.repository.SoftwareRepository;
import org.ontosoft.server.users.User;
import org.ontosoft.shared.api.SoftwareService;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.provenance.Provenance;
import org.ontosoft.shared.classes.permission.Permission;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.Plugin;
import org.ontosoft.shared.plugins.PluginRegistrar;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;

import com.fasterxml.jackson.annotation.JsonProperty;

@Path("")
@Api(value="")
@DeclareRoles({"user", "admin", "importer"})
public class SoftwareResource implements SoftwareService {

  @Context
  HttpServletResponse response;
  @Context
  HttpServletRequest request;
  @Context
  SecurityContext securityContext;
  
  SoftwareRepository repo;
  
  public SoftwareResource() {
    this.repo = SoftwareRepository.get();
  }

  /**
   * Queries
   */

  @GET
  @Path("software")
  @Produces("application/json")
  @Override
  public List<SoftwareSummary> list() {
    try {
      return this.repo.getAllSoftware();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }
  
  @POST
  @Path("search")
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
  @Path("software/{name}")
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
  @Path("software/{name}/version/{version}")
  @Produces("application/json")
  @Override
  public Software getVersion(@PathParam("name") String name, @PathParam("version") String version) {
    try {
      String vid = version;
      String swid = name;
      if(!name.startsWith("http:"))
          swid = repo.LIBNS() + name;
      if(!name.startsWith("http:"))
          vid = swid + "/version/" + version;
      return this.repo.getSoftwareVersion(swid, vid);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }

  @GET
  @Path("software/{name}")
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
  @Path("software/{name}/provenance")
  @Produces("application/json")
  @Override
  public Provenance getProvenance(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      return this.repo.getProvenance(swid);
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }

  @GET
  @Path("software/{name}/provenance")
  @Produces("application/rdf+xml")
  @Override
  public String getProvenanceGraph(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      return this.repo.getProvenanceGraph(swid);
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
  @RolesAllowed("user")
  @Produces("text/html")
  @Override
  public String reloadVocabulary() {
    try {
      this.repo.reloadKBCaches();
      this.repo.initializeVocabularyFromKB();
      response.sendRedirect("");
      return "";
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception: " + e.getMessage());
    }
  }

  @GET
  @Path("software/enumerations")
  @Produces("application/json")
  public Map<String, List<MetadataEnumeration>> getEnumerations() {
    return this.repo.getEnumerations();
  }  
  
  @POST
  @Path("software/enumerations/type")
  @Produces("application/json")
  @Override
  public List<MetadataEnumeration> getEnumerationsForType(@JsonProperty("type") String type) {
    if(!type.startsWith("http:"))
      type = KBConstants.ONTNS() + type;
    return this.repo.getEnumerationsForType(type);
  }
  
  /**
   * Edits
   */

  @POST
  @Path("software")
  @Produces("application/json")
  @Consumes("application/json")
  @RolesAllowed("user")
  @Override
  public Software publish(@JsonProperty("software") Software software) {
    try {
      String swid = this.repo.addSoftware(software,
          (User) securityContext.getUserPrincipal());
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
  
  @POST
  @Path("software/{name}/version")
  @Produces("application/json")
  @Consumes("application/json")
  @RolesAllowed("user")
  @Override
	public Software publishVersion(@PathParam("name") String name, @JsonProperty("software") SoftwareVersion version) {
	  try {
	      String vid = this.repo.addSoftwareVersion(name, version,
	          (User) securityContext.getUserPrincipal());
	      if(vid != null) {
	        version.setId(vid);
	        return this.repo.getSoftwareVersion(name, vid);
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
  @Path("software/{name}")
  @Consumes("application/json")
  @Produces("application/json")
  @RolesAllowed("user")
  @Override
  public Software update(@PathParam("name") String name,
      @JsonProperty("software") Software software) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      if (!this.repo.updateSoftware(software, swid,
          (User) securityContext.getUserPrincipal()))
        throw new RuntimeException("Could not update " + name);
      return software;
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Exception in update: " + e.getMessage());
    }
  }

  @DELETE
  @Path("software/{name}")
  @Produces("text/html")
  @RolesAllowed("user")
  @Override
  public void delete(@PathParam("name") String name) {
    try {
      String swid = name;
      if(!name.startsWith("http:"))
        swid = repo.LIBNS() + name;
      if (!this.repo.deleteSoftware(swid, (User) securityContext.getUserPrincipal()))
        throw new RuntimeException("Could not delete " + name);
    } catch (Exception e) {
      //e.printStackTrace();
      throw new RuntimeException("Exception in delete: " + e.getMessage());
    }
  }
  
  @DELETE
  @Path("software/enumerations/{name}")
  @Produces("text/html")
  @RolesAllowed("user")
  @Override
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
   * Run Plugin
   */
  @POST
  @Path("plugin/{name}/run")
  @Produces("application/json")
  @Consumes("application/json")
  @RolesAllowed("user")
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

  @GET
  @Path("permission") 
  @Produces("application/json")
  @Override
  public List<String> getPermissionTypes()
  {
    return this.repo.getPermissionTypes();
  }
  
  @POST
  @Path("software/{name}/permission")
  @Produces("application/json")
  @Consumes("application/json")
  @RolesAllowed("user")
  @Override 
  public Boolean setSoftwarePermissionForUser(@PathParam("name") String name, 
    @JsonProperty("authorization") Authorization authorization) {
    String swid = name;
    if(!name.startsWith("http:"))
      swid = repo.LIBNS() + name;

    if (!swid.equals(authorization.getAccessToObjId()))
      return false;

    return this.repo.setSoftwarePermissionForUser((User) securityContext.getUserPrincipal(), authorization);
  }
  
  @GET
  @Path("software/{name}/permission")
  @Produces("application/json")
  @Consumes("application/json")
  @RolesAllowed("user")
  @Override 
  public Permission getSoftwarePermissions(@PathParam("name") String name) {
    String swid = name;
    if(!name.startsWith("http:"))
      swid = repo.LIBNS() + name;
    return this.repo.getSoftwarePermission(swid);
  }
  
  @GET
  @Path("software/{name}/permission")
  @Produces("application/rdf+xml")
  public String getSoftwarePermissionsGraph(@PathParam("name") String name) {
    String swid = name;
    if(!name.startsWith("http:"))
      swid = repo.LIBNS() + name;
    return this.repo.getSoftwarePermissionGraph(swid);
  }
  
  @GET
  @Path("software/{name}/permission/{username}")
  @Produces("application/json")
  @Consumes("application/json")
  public AccessMode getSoftwareAccessLevelForUser(@PathParam("name") String swname, 
    @PathParam("username") String username) {
    String swid = swname;
    if(!swname.startsWith("http:"))
      swid = repo.LIBNS() + swname;

    return this.repo.getSoftwareAccessLevelForUser(swid, username);
  }
  
  @GET
  @Path("software/{name}/property/{propname}/permission/{username}") 
  @Produces("application/json")  
  @Consumes("application/json")
  public AccessMode getPropertyAccessLevelForUser(@PathParam("name") String swname,
    @PathParam("propname") String propname, 
    @PathParam("username") String username) {
    String swid = swname;
    if(!swname.startsWith("http:"))
      swid = repo.LIBNS() + swname;
	  
    String propid = propname;
    if(!propname.startsWith("http:"))
      propid = KBConstants.ONTNS() + propname;
	  
    return this.repo.getPropertyAccessLevelForUser(swid, propid, username);
  }
  
  @POST
  @Path("software/{name}/property/permission")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean setPropertyPermissionForUser(@PathParam("name") String name, 
    @JsonProperty("authorization") Authorization authorization) {
    String swid = name;
    if(!name.startsWith("http:"))
      swid = repo.LIBNS() + name;

    return this.repo.setPropertyPermissionForUser((User) securityContext.getUserPrincipal(), 
      swid, authorization);
  }

  @POST
  @Path("software/{name}/owner/permission/{username}")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean addSoftwareOwner(@PathParam("name") String swname, 
    @PathParam("username") String username) {
    String swid = swname;
    if(!swname.startsWith("http:"))
      swid = repo.LIBNS() + swname;

    return this.repo.addSoftwareOwner((User) securityContext.getUserPrincipal(), 
      swid, username);
  }
  
  @DELETE
  @Path("software/{name}/owner/permission/{username}")
  @Produces("application/json")
  @Consumes("application/json")
  public Boolean removeSoftwareOwner(@PathParam("name") String swname, 
    @PathParam("username") String username) {
    String swid = swname;
    if(!swname.startsWith("http:"))
      swid = repo.LIBNS() + swname;

    return this.repo.removeSoftwareOwner((User) securityContext.getUserPrincipal(), 
      swid, username);	  
  }
  
  @GET
  @Path("software/permission/default") 
  @Produces("application/json")  
  @Consumes("application/json")
  public Boolean getPermissionFeatureEnabled() {
    return this.repo.getPermissionFeatureEnabled();
  }
  
  /**
   * Exports
   */
  // TODO: Export Queries (Roles allowed "importer")
}
