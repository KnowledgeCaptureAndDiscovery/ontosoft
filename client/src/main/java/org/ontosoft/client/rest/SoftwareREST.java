package org.ontosoft.client.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.ontosoft.client.Config;
import org.ontosoft.client.authentication.AuthenticatedDispatcher;
import org.ontosoft.shared.api.SoftwareService;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

public class SoftwareREST {
  public static SoftwareService softwareService;

  private static Vocabulary vocabulary;
  private static List<SoftwareSummary> softwareList;
  private static HashMap<String, Software> softwareCache = 
      new HashMap<String, Software>();
  private static HashMap<String, List<MetadataEnumeration>> enumCache = 
      new HashMap<String, List<MetadataEnumeration>>();
  
  private static ArrayList<Callback<Vocabulary, Throwable>> vocabulary_callbacks =
      new ArrayList<Callback<Vocabulary, Throwable>>();  
  private static ArrayList<Callback<List<SoftwareSummary>, Throwable>> list_callbacks =
      new ArrayList<Callback<List<SoftwareSummary>, Throwable>>();
  
  private static HashMap<String, ArrayList<Callback<List<MetadataEnumeration>, Throwable>>> 
    enum_callbacks =
      new HashMap<String, ArrayList<Callback<List<MetadataEnumeration>, Throwable>>>();
  
  public static SoftwareService getSoftwareService() {
    if(softwareService == null) {
      Defaults.setServiceRoot(Config.getServerURL());
      Defaults.setDateFormat(null);
      Defaults.setDispatcher(new AuthenticatedDispatcher());
      softwareService = GWT.create(SoftwareService.class);
    }
    return softwareService;
  }
  
  public static void getVocabulary(final Callback<Vocabulary, Throwable> callback,
      boolean reload) {
    if(vocabulary != null && !reload) {
      callback.onSuccess(vocabulary);
    }
    else {
      vocabulary = null;
      if(vocabulary_callbacks.isEmpty()) {
        vocabulary_callbacks.add(callback);
        REST.withCallback(new MethodCallback<Vocabulary>() {
          @Override
          public void onSuccess(Method method, Vocabulary vocab) {
            vocabulary = vocab;
            for(Callback<Vocabulary, Throwable> cb : vocabulary_callbacks)
              cb.onSuccess(vocabulary);
            vocabulary_callbacks.clear();
          }
          @Override
          public void onFailure(Method method, Throwable exception) {
            AppNotification.notifyFailure("Could not load vocabulary");
            callback.onFailure(exception);
          }
        }).call(getSoftwareService()).getVocabulary();        
      }
      else {
        vocabulary_callbacks.add(callback);
      }
    }
  }
  
  public static void getSoftwareList(final Callback<List<SoftwareSummary>, Throwable> callback,
      boolean reload) {
    if(softwareList != null && !reload) {
      callback.onSuccess(softwareList);
    }
    else {
      softwareList = null;
      if(list_callbacks.isEmpty()) {
        list_callbacks.add(callback);
        REST.withCallback(new MethodCallback<List<SoftwareSummary>>() {
          @Override
          public void onSuccess(Method method, List<SoftwareSummary> swlist) {
            softwareList = swlist;
            for(Callback<List<SoftwareSummary>, Throwable> cb : list_callbacks)
              cb.onSuccess(softwareList);    
            list_callbacks.clear();
          }
          @Override
          public void onFailure(Method method, Throwable exception) {
            AppNotification.notifyFailure("Could not load software list");
            callback.onFailure(exception);
          }
        }).call(getSoftwareService()).list();
      }
      else {
        list_callbacks.add(callback);
      }
    }
  }
  
  public static void getSoftwareListFaceted(List<EnumerationFacet> facets,
      final Callback<List<SoftwareSummary>, Throwable> callback) {
    REST.withCallback(new MethodCallback<List<SoftwareSummary>>() {
      @Override
      public void onSuccess(Method method, List<SoftwareSummary> swlist) {
        callback.onSuccess(swlist);            
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(getSoftwareService()).listWithFacets(facets);
  }
  
  public static void getSoftware(final String swname, final Callback<Software, Throwable> callback,
      final boolean reload) {
    //GWT.log(softwareCache.keySet().toString() + ": "+reload);
    if(softwareCache.containsKey(swname) && !reload) {
      callback.onSuccess(softwareCache.get(swname));
    }    
    else {
      REST.withCallback(new MethodCallback<Software>() {
        @Override
        public void onSuccess(Method method, Software sw) {
          //GWT.log("caching "+sw.getName());
          if(sw != null) {
            softwareCache.put(sw.getName(), sw);
            if(reload)
              AppNotification.notifySuccess(sw.getLabel() + " reloaded", 1000);
            callback.onSuccess(sw);
          }
          else {
            AppNotification.notifyFailure("Could not find "+swname);
            callback.onFailure(new Throwable("Software details could not be found"));
          }
        }
        @Override
        public void onFailure(Method method, Throwable exception) {
          GWT.log("Could nto fetch software: "+swname, exception);
          AppNotification.notifyFailure("Could not fetch software: "+swname);
          callback.onFailure(exception);
        }
      }).call(getSoftwareService()).get(URL.encodeQueryString(swname));
    }
  }
  
  public static void getSoftwareRDF(final String swid, 
      final Callback<String, Throwable> callback) {
    RequestBuilder rb = new RequestBuilder(RequestBuilder.GET, swid);
    rb.setHeader("Accept", "application/rdf+xml");
    try {
      rb.sendRequest(null, new RequestCallback() {
        @Override
        public void onResponseReceived(Request request, Response response) {
          callback.onSuccess(response.getText());
        }
        @Override
        public void onError(Request request, Throwable exception) {
          AppNotification.notifyFailure("Could not find " + swid);
          callback.onFailure(new Throwable(
              "Software graph could not be found"));
        }
      });
    } catch (Exception e) {
      AppNotification.notifyFailure("Could not find " + swid);
    }
  }
  
  public static void getEnumerationsForType(final String typeid,
      final Callback<List<MetadataEnumeration>, Throwable> callback) {
    if(enumCache.containsKey(typeid)) {
      callback.onSuccess(enumCache.get(typeid));
    }
    else {
      ArrayList<Callback<List<MetadataEnumeration>, Throwable>> type_enum_callbacks =
          enum_callbacks.get(typeid);
      if(type_enum_callbacks == null) {
        type_enum_callbacks = 
          new ArrayList<Callback<List<MetadataEnumeration>, Throwable>>();
      }
      
      if(type_enum_callbacks.isEmpty()) {
        type_enum_callbacks.add(callback);
        REST.withCallback(new MethodCallback<List<MetadataEnumeration>>() {
          @Override
          public void onSuccess(Method method, List<MetadataEnumeration> enumlist) {
            enumCache.put(typeid, enumlist);
            for(Callback<List<MetadataEnumeration>, Throwable> cb : enum_callbacks.get(typeid))
              cb.onSuccess(enumlist);
            enum_callbacks.get(typeid).clear();
          }
          @Override
          public void onFailure(Method method, Throwable exception) {
            AppNotification.notifyFailure("Could not load enumerations for "+typeid);
            callback.onFailure(exception);
          }
        }).call(getSoftwareService()).getEnumerationsForType(typeid);
      }
      else {
        type_enum_callbacks.add(callback);
      }
      enum_callbacks.put(typeid, type_enum_callbacks);
    }
  }
  
  public static void publishSoftware(final Software software, 
      final Callback<Software, Throwable> callback) {
    REST.withCallback(new MethodCallback<Software>() {
      @Override
      public void onSuccess(Method method, Software sw) {
        softwareCache.put(sw.getName(), sw);
        softwareList.add(new SoftwareSummary(sw));
        AppNotification.notifySuccess(software.getLabel() + " published. Now enter some details !", 1500);
        callback.onSuccess(sw);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not publish");
        callback.onFailure(exception);
      }
    }).call(getSoftwareService()).publish(software);    
  }
  
  public static void updateSoftware(final Software software, 
      final Callback<Software, Throwable> callback) {
    REST.withCallback(new MethodCallback<Software>() {
      @Override
      public void onSuccess(Method method, Software sw) {
        softwareCache.put(sw.getName(), sw);
        AppNotification.notifySuccess(software.getLabel() + " saved", 1000);
        callback.onSuccess(sw);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not save "+software.getLabel());
        callback.onFailure(exception);
      }
    }).call(getSoftwareService()).update(software.getName(), software);    
  }
  
  public static void deleteSoftware(final String swname, 
      final Callback<Void, Throwable> callback) {
    REST.withCallback(new MethodCallback<Void>() {
      @Override
      public void onSuccess(Method method, Void v) {
        softwareCache.remove(swname);
        for(SoftwareSummary sum: softwareList)
          if(sum.getName().equals(swname))
            softwareList.remove(sum);
        callback.onSuccess(v);
        AppNotification.notifySuccess(swname+" deleted", 1000);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not delete "+swname);
        callback.onFailure(exception);
      }
    }).call(getSoftwareService()).delete(swname);    
  }
  
  public static void runPlugin(final String pluginname, final Software software, 
      final Callback<PluginResponse, Throwable> callback) {
    
    REST.withCallback(new MethodCallback<PluginResponse>() {
      @Override
      public void onSuccess(Method method, PluginResponse response) {
        String msg = pluginname+" Plugin: "
            + "Got a response for "+response.getSoftwareInfo().getLabel();
        AppNotification.notifySuccess(msg, 1500);
        callback.onSuccess(response);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure(pluginname+" Plugin: Could not run");
        callback.onFailure(exception);
      }
    }).call(getSoftwareService()).runPlugin(pluginname, software);  
  }
}
