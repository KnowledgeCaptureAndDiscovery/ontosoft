package org.ontosoft.shared.classes.util;

import java.util.HashMap;
import java.util.Map;

public class KBConstants {
  //private static String serveruri = "http://seagull.isi.edu:8080/ontosoft-server";  
  private static String serveruri = "http://seagull.isi.edu:8080/turbosoft-server";
  private static String liburi = "http://www.ontosoft.org/repository/software/";
  
//  private static String serveruri = "http://localhost:9090/ontosoft-server";
//  private static String liburi = null;
  
  private static String onturi = "http://ontosoft.org/software";
  private static String caturi = "http://ontosoft.org/softwareCategories";
  
  private static String provns = "http://www.w3.org/ns/prov#";
  private static String owlns = "http://www.w3.org/2002/07/owl#";
  private static String rdfns = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
  private static String rdfsns = "http://www.w3.org/2000/01/rdf-schema#";
  
  private static String dctermsns = "http://purl.org/dc/terms/";
  private static String dcns = "http://purl.org/dc/elements/1.1/";
  
  private static final Map<String, String> nsmap;
  static
  {
    nsmap = new HashMap<String, String>();
    nsmap.put("osw:", ONTNS());
    nsmap.put("cat:", CATNS());
    nsmap.put("lib:", LIBNS());
  }
  
  public static String ONTURI() {
    return onturi;
  }

  public static void ONTURI(String uri) {
    onturi = uri;
  }
  
  public static String ONTNS() {
    return onturi + "#";
  }
  
  public static String CATURI() {
    return caturi;
  }
  
  public static void CATURI(String uri) {
    caturi = uri;
  }
  
  public static String CATNS() {
    return caturi + "#";
  }
  
  public static String SERVERURI() {
    return serveruri;
  }
  
  public static void SERVERURI(String uri) {
      serveruri = uri;
  }
  
  public static String LIBURI() {
    if(liburi == null)
      return serveruri.replaceAll("\\/$", "") + "/software/";
    return liburi;
  }
  
  public static String LIBNS() {
    return LIBURI();
  }
  
  public static String ENUMURI() {
    return LIBURI() + "enumerations";
  }
  
  public static String ENUMNS() {
    return ENUMURI() + "#";
  }
  
  public static String PROVNS() {
    return provns;
  }
  
  public static String DCTERMSNS() {
    return dctermsns;
  }
  
  public static String DCNS() {
    return dcns;
  }
  
  public static String OWLNS() {
    return owlns;
  }
  
  public static String RDFNS() {
    return rdfns;
  }
  
  public static String RDFSNS() {
    return rdfsns;
  }
  
  public static String randomEntityId(String softwareid) {
    return softwareid + "#Entity-"+ GUID.get(12);
  }
}
