package org.ontosoft.server.repository.plugins;

import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.plugins.Plugin;
import org.ontosoft.shared.plugins.PluginResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class GithubPlugin extends Plugin {
  String acceptHeader = "application/vnd.github.drax-preview+json";
  String apiurl = "https://api.github.com";

  public GithubPlugin() {
    super(
        "Github", "Get github metadata", 
        KBConstants.ONTNS()+"hasCodeLocation");
    this.setValueMatchRegex(".*github\\.com.*");
    this.setIcon("fa-github");
    //this.setAutomaticallyTriggered(true);
  }

  @Override
  public PluginResponse run(Software software) {
    String ontns = KBConstants.ONTNS();
    PluginResponse response = new PluginResponse(this);
    Entity codeLoc = software.getPropertyValue(ontns + "hasCodeLocation");
    if(codeLoc != null) {
      String location = (String) codeLoc.getValue();
      if(location.matches(this.getValueMatchRegex())) {
        Pattern repopat = Pattern.compile(".*github\\.com\\/(.+?)\\/(.+)\\/?.*");
        Matcher mat = repopat.matcher(location);
        if(mat.find()) {
          String userid = mat.group(1);
          String repoid = mat.group(2);
          this.addRepositoryMetadata(userid, repoid, response);
          this.addLanguageMetadata(userid, repoid, response);
          this.addDocumentationMetadata(userid, repoid, response);
        }
      }
    }
    return response;
  }

  private Collection<String> addLanguageMetadata(String userid, String repoid,
      PluginResponse response) {
    String ontns = KBConstants.ONTNS();
    String resource = "/repos/"+userid+"/"+repoid+"/languages";

    HashMap<String, Object> vals = getResource(resource);
    for(String language : vals.keySet()) {
      response.addSuggestedMetadata(ontns + "hasImplementationLanguage", language);
    }
    return vals.keySet();
  }
  
  @SuppressWarnings("unchecked")
  private void addRepositoryMetadata(String userid, String repoid, 
      PluginResponse response) {
    String ontns = KBConstants.ONTNS();
    String resource = "/repos/"+userid+"/"+repoid;

    HashMap<String, Object> vals = getResource(resource);
    if(vals.containsKey("license")) {
      HashMap<String, String> licobj = (HashMap<String,String>)vals.get("license");
      if(licobj != null && licobj.containsKey("name"))
        response.addSuggestedMetadata(ontns + "hasLicense", licobj.get("name"));
    }
    if(vals.containsKey("homepage") && vals.get("homepage") != null)
      response.addSuggestedMetadata(ontns + "hasProjectWebsite", vals.get("homepage"));
    if(vals.containsKey("description") && !("".equals(vals.get("description"))))
      response.addSuggestedMetadata(ontns + "hasShortDescription", vals.get("description"));

  }
  
  private void addDocumentationMetadata(String userid, String repoid, 
      PluginResponse response) {
    String ontns = KBConstants.ONTNS();
    String resource = "/repos/"+userid+"/"+repoid+"/readme";
    HashMap<String, Object> vals = getResource(resource);
    if(vals.containsKey("download_url")) {
      String docurl = (String) vals.get("download_url");
      if(docurl != null && !docurl.equals(""))
        response.addSuggestedMetadata(ontns + "hasDocumentation", docurl);
    }
  }
  
  @SuppressWarnings("unchecked")
  private HashMap<String, Object> getResource(String resource) {
    try {
      Client client = ClientBuilder.newClient();
      WebTarget target = client.target(apiurl).path(resource);
      String json = target.request(acceptHeader).get(String.class);
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, HashMap.class);
    }
    catch (Exception e) {
      //e.printStackTrace();
    }
    return new HashMap<String, Object>();
  }
}
