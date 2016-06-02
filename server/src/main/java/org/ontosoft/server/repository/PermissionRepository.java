package org.ontosoft.server.repository;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration.plist.PropertyListConfiguration;
import org.ontosoft.server.users.User;
import org.ontosoft.server.util.Config;
import org.ontosoft.shared.classes.permission.Permission;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Agent;
import org.ontosoft.shared.classes.users.UserCredentials;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;
import edu.isi.wings.ontapi.KBTriple;
import edu.isi.wings.ontapi.OntFactory;
import edu.isi.wings.ontapi.OntSpec;

public class PermissionRepository {
  OntFactory fac;
  KBAPI permontkb;
  KBAPI foafkb;
  public static String ACL_GRAPH = "permission";

  String ontns, owlns, rdfns, rdfsns, foafns;
  String server;
  String authclass, readclass, writeclass;

  private String PERMURI(String swid) {
    return swid + "/" + ACL_GRAPH;
  }

  private String USERURI() {
    return server.replaceAll("\\/$", "") + "/users/"; 
  }

  private String USERNS() {
    return USERURI();
  }

  public PermissionRepository() {
    initializeKB();
  }

  private void initializeKB() {
    PropertyListConfiguration props = Config.get().getProperties();
    this.server = props.getString("server");

    String tdbdir = props.getString("storage.tdb");
    File tdbdirf = new File(tdbdir);
    if(!tdbdirf.exists() && !tdbdirf.mkdirs()) {
      System.err.println("Cannot create tdb directory : "+tdbdirf.getAbsolutePath());
    }

    owlns = KBConstants.OWLNS();
    rdfns = KBConstants.RDFNS();
    rdfsns = KBConstants.RDFSNS();
    ontns = KBConstants.PERMNS();
    foafns = KBConstants.FOAFNS();

    authclass = ontns + "Authorization";
    readclass = ontns + "Read";
    writeclass = ontns + "Write";

    this.fac = new OntFactory(OntFactory.JENA, tdbdir);
    try {
      this.permontkb = fac.getKB(KBConstants.PERMURI(), OntSpec.PLAIN, false, true);
      this.foafkb = fac.getKB(KBConstants.FOAFURI(), OntSpec.PLAIN, false, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public Permission createSoftwarePermisson(String swid, User owner) throws Exception {
    Permission perm = new Permission();
    String permid = PERMURI(swid);
    perm.setId(permid);

    String permtype = "Write";
    String permns = permid + "#";
    Authorization auth = new Authorization();
    auth.setId(permns + "Auth-" + GUID.get());
    AccessMode mode = new AccessMode();
    mode.setMode(permtype);
    auth.setAccessMode(mode);
    auth.setAgentId(this.getUserId(owner));
    auth.setAccessToObjId(swid);

    perm.addOwnerid(this.getUserId(owner));
    perm.setType(KBConstants.PERMNS() + "Access");
    perm.addAuth(auth);

    return perm;
  }

  public boolean deleteSoftwarePermission(String swid) throws Exception {
    KBAPI permkb = fac.getKB(PERMURI(swid), OntSpec.PLAIN, true);
    return permkb.delete();
  }

  public boolean commitPermission(Permission perm, String swid) throws Exception {
    KBAPI permkb = fac.getKB(perm.getId(), OntSpec.PLAIN, true);
    
    KBObject authcls = this.permontkb.getConcept(authclass);
    KBObject readcls = this.permontkb.getConcept(readclass);
    KBObject writecls = this.permontkb.getConcept(writeclass);
    KBObject agentcls = this.foafkb.getConcept(foafns + "Agent");

    KBObject swobj = permkb.getResource(swid);

    KBObject ownerprop = this.permontkb.getProperty(ontns + "owner");
    KBObject modeprop = this.permontkb.getProperty(ontns + "mode");
    KBObject agentprop = this.permontkb.getProperty(ontns + "agent");
    KBObject accesstoprop = this.permontkb.getProperty(ontns + "accessTo");
    KBObject typeprop = this.permontkb.getProperty(rdfns + "type");
    
    for(KBTriple t : permkb.genericTripleQuery(swobj, ownerprop, null)) {
      permkb.removeTriple(t);
    }
    
    List<Agent> ownerAgents = perm.getOwners();
    for (Agent ownerAgent: ownerAgents) {
      KBObject ownerobj = permkb.getIndividual(ownerAgent.getId());
      if(ownerobj == null) {
        ownerobj = permkb.createObjectOfClass(ownerAgent.getId(), agentcls);
      }
      permkb.addTriple(swobj, ownerprop, ownerobj);   
    }

    for(KBTriple t : permkb.genericTripleQuery(null, typeprop, authcls)) {
      KBObject authobj = t.getSubject();
      if (!perm.getAuthorizations().containsKey(authobj.getID())) {
    	permkb.removeTriple(t);
        for(KBTriple kt : permkb.genericTripleQuery(authobj, null, null)) {
          permkb.removeTriple(kt);
        }
      }
    }
    
    for(Authorization auth : perm.getAuthorizations().values()) {
      KBObject modeobj = readcls;
      if (auth.getAccessMode().getId().equals(auth.getAccessMode().WMODEURI()))
        modeobj = writecls;

      KBObject agentobj = permkb.getIndividual(auth.getAgentId());
      if(agentobj == null) {
        if (auth.getAgentId() != null)
          agentobj = permkb.createObjectOfClass(auth.getAgentId(), agentcls);
      }

      KBObject accesstoobj = permkb.getResource(auth.getAccessToObjId());
      
      if(modeobj != null && agentobj != null && accesstoobj != null) {
        KBObject authobj = permkb.getIndividual(auth.getId());
        if(authobj == null)
          authobj = permkb.createObjectOfClass(auth.getId(), authcls); 
        
        for(KBTriple t : permkb.genericTripleQuery(authobj, modeprop, null))
          permkb.removeTriple(t);
        
        permkb.addTriple(authobj, modeprop, modeobj);
        permkb.setPropertyValue(authobj, agentprop, agentobj);
        permkb.setPropertyValue(authobj, accesstoprop, accesstoobj);
      }
    }
    return permkb.save();
  }
  
  public String getSoftwarePermissionGraph(String swid) throws Exception {
    Permission perm = new Permission();
    String permid = PERMURI(swid);
    perm.setId(permid);

    KBAPI permkb = fac.getKB(permid, OntSpec.PLAIN);
    return permkb.toAbbrevRdf(false);
  }

  public Permission getSoftwarePermission(String swid) throws Exception {
    Permission perm = new Permission();
    String permid = PERMURI(swid);
    perm.setId(permid);

    KBAPI permkb = fac.getKB(permid, OntSpec.PLAIN);

    KBObject authcls = this.permontkb.getConcept(authclass);

    KBObject modeprop = this.permontkb.getProperty(ontns + "mode");
    KBObject agentprop = this.permontkb.getProperty(ontns + "agent");
    KBObject ownerprop = this.permontkb.getProperty(ontns + "owner");
    KBObject accesstoprop = this.permontkb.getProperty(ontns + "accessTo");
    KBObject typeprop = this.permontkb.getProperty(rdfns + "type");

    // Get Authorizations
    for(KBTriple t : permkb.genericTripleQuery(null, typeprop, authcls)) {
      KBObject authobj = t.getSubject();
      Authorization auth = new Authorization();
      auth.setId(authobj.getID());

      KBObject modeobj = permkb.getPropertyValue(authobj, modeprop);
      KBObject agentobj = permkb.getPropertyValue(authobj, agentprop);
      KBObject accesstoobj = permkb.getPropertyValue(authobj, accesstoprop);

      if(modeobj != null && agentobj != null && accesstoobj != null) {
        AccessMode mode = new AccessMode();
        mode.setId(modeobj.getID());
        auth.setAccessMode(mode);
        auth.setAgentId(agentobj.getID());
        auth.setAccessToObjId(accesstoobj.getID());
        perm.addAuth(auth);
      }
    }

    KBObject swobj = permkb.getResource(swid);
    
    if (swobj != null) {
      for(KBTriple t : permkb.genericTripleQuery(swobj, ownerprop, null)) {
        KBObject owner = t.getObject();
        perm.addOwnerid(owner.getID());  
      }
    }    
    return perm;
  }

  public List<String> getPermissionTypes() {
    return Arrays.asList("Read", "Write");
  }

  private String getUserId(UserCredentials user) {
    return this.USERNS() + user.getName().replaceAll("[^a-zA-Z0-9_]", "_");    
  }
}
