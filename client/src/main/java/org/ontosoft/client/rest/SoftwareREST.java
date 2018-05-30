package org.ontosoft.client.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.REST;
import org.fusesource.restygwt.client.Resource;
import org.fusesource.restygwt.client.RestServiceProxy;
import org.ontosoft.client.authentication.AuthenticatedDispatcher;
import org.ontosoft.shared.api.SoftwareService;
import org.ontosoft.shared.classes.FunctionSummary;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.SoftwareVersionSummary;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.entities.SoftwareFunction;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.search.EnumerationFacet;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.permission.Permission;
import org.ontosoft.shared.classes.permission.AccessMode;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;

public class SoftwareREST {
  public static HashMap<String, SoftwareREST> singletons =
      new HashMap<String, SoftwareREST>();

  public static String LOCAL = "local";
  
  private Vocabulary vocabulary;
  private List<SoftwareSummary> softwareList;
  private List<SoftwareVersionSummary> softwareVersionList;
  private List<FunctionSummary> functionList;
  private SoftwareService service;
  
  private HashMap<String, Software> softwareCache = 
      new HashMap<String, Software>();
  private HashMap<String, SoftwareVersion> softwareVersionCache = 
	      new HashMap<String, SoftwareVersion>();
  private HashMap<String, SoftwareFunction> softwareFunctionCache = 
	      new HashMap<String, SoftwareFunction>();
  private HashMap<String, List<MetadataEnumeration>> enumCache = 
      new HashMap<String, List<MetadataEnumeration>>();
  
  private ArrayList<Callback<Vocabulary, Throwable>> vocabulary_callbacks =
      new ArrayList<Callback<Vocabulary, Throwable>>();  
  private ArrayList<Callback<List<SoftwareSummary>, Throwable>> list_callbacks =
      new ArrayList<Callback<List<SoftwareSummary>, Throwable>>();
  private ArrayList<Callback<List<SoftwareVersionSummary>, Throwable>> list_version_callbacks =
	      new ArrayList<Callback<List<SoftwareVersionSummary>, Throwable>>();
  private ArrayList<Callback<List<FunctionSummary>, Throwable>> list_function_callbacks =
	      new ArrayList<Callback<List<FunctionSummary>, Throwable>>();
  private ArrayList<Callback<Boolean, Throwable>> perm_callbacks =
      new ArrayList<Callback<Boolean,Throwable>>();
  
  private HashMap<String, ArrayList<Callback<List<MetadataEnumeration>, Throwable>>> 
    enum_callbacks =
      new HashMap<String, ArrayList<Callback<List<MetadataEnumeration>, Throwable>>>();
  
  private Boolean permFeatureEnabled = null;
  
  public static SoftwareREST get(String key) {
    if(singletons.containsKey(key))
      return singletons.get(key);
    
    SoftwareREST service = new SoftwareREST(key);
    singletons.put(key, service);
    return service;
  }
  
  public SoftwareREST (String url) {
    //Defaults.setServiceRoot(Config.getServerURL());
    Defaults.setDateFormat(null);
    Defaults.setDispatcher(new AuthenticatedDispatcher());
    this.service = GWT.create(SoftwareService.class);
    ((RestServiceProxy)this.service).setResource(
        new Resource(url));
  }
  
  public void clearSwCache() {
    softwareCache.clear();
    permFeatureEnabled = null;
  }
  
  public void getVocabulary(final Callback<Vocabulary, Throwable> callback,
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
        }).call(this.service).getVocabulary();        
      }
      else {
        vocabulary_callbacks.add(callback);
      }
    }
  }
  
  public void getSoftwareList(final Callback<List<SoftwareSummary>, Throwable> callback,
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
        }).call(this.service).list();
      }
      else {
        list_callbacks.add(callback);
      }
    }
  }
  
  public void getSoftwareVersionList(String software, final Callback<List<SoftwareVersionSummary>, Throwable> callback,
      boolean reload) {
    if(softwareVersionList != null && !reload) {
      callback.onSuccess(softwareVersionList);
    }
    else {
      softwareVersionList = null;
      if(list_version_callbacks.isEmpty()) {
        list_version_callbacks.add(callback);
        REST.withCallback(new MethodCallback<List<SoftwareVersionSummary>>() {
          @Override
          public void onSuccess(Method method, List<SoftwareVersionSummary> swlist) {
            softwareVersionList = swlist;
            for(Callback<List<SoftwareVersionSummary>, Throwable> cb : list_version_callbacks)
              cb.onSuccess(softwareVersionList);    
            list_callbacks.clear();
          }
          @Override
          public void onFailure(Method method, Throwable exception) {
            AppNotification.notifyFailure("Could not load software list");
            callback.onFailure(exception);
          }
        }).call(this.service).versions(software);
      }
      else {
        list_version_callbacks.add(callback);
      }
    }
  }
  
  public void getFunctionList(final Callback<List<FunctionSummary>, Throwable> callback,
      boolean reload) {
    if(functionList != null && !reload) {
      callback.onSuccess(functionList);
    }
    else {
      functionList = null;
      if(list_function_callbacks.isEmpty()) {
        list_function_callbacks.add(callback);
        REST.withCallback(new MethodCallback<List<FunctionSummary>>() {
          @Override
          public void onSuccess(Method method, List<FunctionSummary> swlist) {
        	functionList = swlist;
            for(Callback<List<FunctionSummary>, Throwable> cb : list_function_callbacks)
              cb.onSuccess(functionList);    
            list_function_callbacks.clear();
          }
          @Override
          public void onFailure(Method method, Throwable exception) {
            AppNotification.notifyFailure("Could not load software list");
            callback.onFailure(exception);
          }
        }).call(this.service).functions();
      }
      else {
        list_function_callbacks.add(callback);
      }
    }
  }
  
  public void getSoftwareListFaceted(List<EnumerationFacet> facets,
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
    }).call(this.service).listWithFacets(facets);
  }
  
  public void getSoftwareVersionListFaceted(List<EnumerationFacet> facets, String software,
      final Callback<List<SoftwareVersionSummary>, Throwable> callback) {
    REST.withCallback(new MethodCallback<List<SoftwareVersionSummary>>() {
      @Override
      public void onSuccess(Method method, List<SoftwareVersionSummary> swlist) {
        callback.onSuccess(swlist);            
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).listSoftwareVersionWithFacets(facets, software);
  }
  
  public void getFunctionListFaceted(List<EnumerationFacet> facets,
      final Callback<List<FunctionSummary>, Throwable> callback) {
    REST.withCallback(new MethodCallback<List<FunctionSummary>>() {
      @Override
      public void onSuccess(Method method, List<FunctionSummary> swlist) {
        callback.onSuccess(swlist);            
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).listFunctionWithFacets(facets);
  }
	  
  public void getSoftware(final String swname, final Callback<Software, Throwable> callback,
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
      }).call(this.service).get(URL.encodeQueryString(swname));
    }
  }
  
  public void getSoftwareVersion(final String swname, final String vname, final Callback<SoftwareVersion, Throwable> callback,
      final boolean reload) {
    //GWT.log(softwareCache.keySet().toString() + ": "+reload);
    if(softwareVersionCache.containsKey(vname) && !reload) {
      callback.onSuccess(softwareVersionCache.get(vname));
    }    
    else {
      REST.withCallback(new MethodCallback<SoftwareVersion>() {
        @Override
        public void onSuccess(Method method, SoftwareVersion sw) {
          //GWT.log("caching "+sw.getName());
          if(sw != null) {
            softwareVersionCache.put(sw.getName(), sw);
            if(reload)
              AppNotification.notifySuccess(sw.getLabel() + " reloaded", 1000);
            callback.onSuccess(sw);
          }
          else {
            AppNotification.notifyFailure("Could not find "+vname);
            callback.onFailure(new Throwable("Software details could not be found"));
          }
        }
        @Override
        public void onFailure(Method method, Throwable exception) {
          GWT.log("Could nto fetch software: "+vname, exception);
          AppNotification.notifyFailure("Could not fetch software: "+vname);
          callback.onFailure(exception);
        }
      }).call(this.service).getVersion(URL.encodeQueryString(swname), URL.encodeQueryString(vname));
    }
  }
  
  public void getSoftwareFunction(final String swname, final String vname, final String fname, 
		  final Callback<SoftwareFunction, Throwable> callback,
      final boolean reload) {
    //GWT.log(softwareCache.keySet().toString() + ": "+reload);
    if(softwareFunctionCache.containsKey(vname) && !reload) {
      callback.onSuccess(softwareFunctionCache.get(fname));
    }    
    else {
      REST.withCallback(new MethodCallback<SoftwareFunction>() {
        @Override
        public void onSuccess(Method method, SoftwareFunction f) {
          //GWT.log("caching "+sw.getName());
          if(f != null) {
            softwareFunctionCache.put(f.getName(), f);
            if(reload)
              AppNotification.notifySuccess(f.getLabel() + " reloaded", 1000);
            callback.onSuccess(f);
          }
          else {
            AppNotification.notifyFailure("Could not find "+vname);
            callback.onFailure(new Throwable("Software details could not be found"));
          }
        }
        @Override
        public void onFailure(Method method, Throwable exception) {
          GWT.log("Could nto fetch software: "+vname, exception);
          AppNotification.notifyFailure("Could not fetch software: "+vname);
          callback.onFailure(exception);
        }
      }).call(this.service).getSoftwareFunction(URL.encodeQueryString(swname), URL.encodeQueryString(vname), URL.encodeQueryString(fname));
    }
  }
  
  public void getSoftwareRDF(final String swid, 
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
  
  public void getEnumerationsForType(final String typeid,
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
        }).call(this.service).getEnumerationsForType(typeid);
      }
      else {
        type_enum_callbacks.add(callback);
      }
      enum_callbacks.put(typeid, type_enum_callbacks);
    }
  }
  
  public void publishSoftware(final Software software, 
      final Callback<Software, Throwable> callback) {
    REST.withCallback(new MethodCallback<Software>() {
      @Override
      public void onSuccess(Method method, Software sw) {
        if(sw != null) {
          softwareCache.put(sw.getName(), sw);
          softwareList.add(new SoftwareSummary(sw));
          AppNotification.notifySuccess(software.getLabel() + " published. Now enter some details !", 1500);
          callback.onSuccess(sw);
        }
        else {
          AppNotification.notifyFailure("Could not publish");
          callback.onFailure(new Throwable("Returned null"));
        }
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not publish");
        callback.onFailure(exception);
      }
    }).call(this.service).publish(software);    
  }
  
  public void publishSoftwareVersion(final String software, 
	  final SoftwareVersion version, 
      final Callback<SoftwareVersion, Throwable> callback) {
    REST.withCallback(new MethodCallback<SoftwareVersion>() {
      @Override
      public void onSuccess(Method method, SoftwareVersion sw) {
        if(sw != null) {
          softwareVersionCache.put(sw.getName(), sw);
          softwareVersionList.add(new SoftwareVersionSummary(sw));
          AppNotification.notifySuccess(version.getLabel() + " published. Now enter some details !", 1500);
          callback.onSuccess(sw);
        }
        else {
          AppNotification.notifyFailure("Could not publish");
          callback.onFailure(new Throwable("Returned null"));
        }
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not publish");
        callback.onFailure(exception);
      }
    }).call(this.service).publishVersion(software, version);    
  }
  
  public void updateSoftware(final Software software, 
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
    }).call(this.service).update(software.getName(), software);    
  }
  
  public void updateSoftwareVersion(final String software, final SoftwareVersion version, 
      final Callback<SoftwareVersion, Throwable> callback) {
    REST.withCallback(new MethodCallback<SoftwareVersion>() {
      @Override
      public void onSuccess(Method method, SoftwareVersion sw) {
        softwareVersionCache.put(sw.getName(), sw);
        AppNotification.notifySuccess(version.getLabel() + " saved", 1000);
        callback.onSuccess(sw);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not save "+version.getLabel());
        callback.onFailure(exception);
      }
    }).call(this.service).updateVersion(software, version.getName(), version);    
  }
  
  public void deleteSoftware(final String swname, 
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
    }).call(this.service).delete(swname);    
  }
  
  public void deleteSoftwareVersion(final String swname, final String vname, 
      final Callback<Void, Throwable> callback) {
    REST.withCallback(new MethodCallback<Void>() {
      @Override
      public void onSuccess(Method method, Void v) {
        softwareVersionCache.remove(swname);
        for(SoftwareSummary sum: softwareVersionList)
          if(sum.getName().equals(swname))
            softwareVersionList.remove(sum);
        callback.onSuccess(v);
        AppNotification.notifySuccess(swname+" deleted", 1000);
      }
      @Override
      public void onFailure(Method method, Throwable exception) {
        AppNotification.notifyFailure("Could not delete "+vname);
        callback.onFailure(exception);
      }
    }).call(this.service).deleteSoftwareVersion(swname, vname);    
  }
  
  public void runPlugin(final String pluginname, final Software software, 
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
    }).call(this.service).runPlugin(pluginname, software);  
  }
  
  public void getPermissionTypes (
    final Callback<List<String>, Throwable> callback) {
    REST.withCallback(new MethodCallback<List<String>>() {
      @Override
      public void onSuccess(Method method, List<String> permlist) {
        callback.onSuccess(permlist);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).getPermissionTypes();
  }
  
  public void setSoftwarePermissionForUser(String name, Authorization authorization,
    final Callback<Boolean, Throwable> callback) {
    REST.withCallback(new MethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean success) {
        callback.onSuccess(success);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).setSoftwarePermissionForUser(name, authorization);
  }

  public void getSoftwarePermissions(String name,
    final Callback<Permission, Throwable> callback) {
    REST.withCallback(new MethodCallback<Permission>() {
      @Override
      public void onSuccess(Method method, Permission permission) {
        callback.onSuccess(permission);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).getSoftwarePermissions(name);
  }
  
  public void getSoftwareAccessLevelForUser(String swname, String username,
    final Callback<AccessMode, Throwable> callback) {
      REST.withCallback(new MethodCallback<AccessMode>() {
      @Override
      public void onSuccess(Method method, AccessMode accessmode) {
        callback.onSuccess(accessmode);            
      }
  
      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).getSoftwareAccessLevelForUser(swname, username);
  }
  
  public void getPropertyAccessLevelForUser(String swname, String propid, String username,
    final Callback<AccessMode, Throwable> callback) {
    REST.withCallback(new MethodCallback<AccessMode>() {
      @Override
      public void onSuccess(Method method, AccessMode accessmode) {
        callback.onSuccess(accessmode);            
      }
  
      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).getPropertyAccessLevelForUser(swname, propid, username);
  }
  
  public void setPropertyPermissionForUser(String name, Authorization authorization,
    final Callback<Boolean, Throwable> callback) {
    REST.withCallback(new MethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean success) {
        callback.onSuccess(success);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).setPropertyPermissionForUser(name, authorization);
  }
  
  public void addSoftwareOwner(String swname, String username,
    final Callback<Boolean, Throwable> callback) {
    REST.withCallback(new MethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean success) {
        callback.onSuccess(success);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).addSoftwareOwner(swname, username);
  }

  public void removeSoftwareOwner(String swname, String username,
    final Callback<Boolean, Throwable> callback) {
    REST.withCallback(new MethodCallback<Boolean>() {
      @Override
      public void onSuccess(Method method, Boolean success) {
        callback.onSuccess(success);            
      }

      @Override
      public void onFailure(Method method, Throwable exception) {
        callback.onFailure(exception);
      }
    }).call(this.service).removeSoftwareOwner(swname, username);
  }
  
  public void getPermissionFeatureEnabled(final Callback<Boolean, Throwable> callback) {
    if(permFeatureEnabled != null) {
      callback.onSuccess(permFeatureEnabled);
    }
    else {
      if(perm_callbacks.isEmpty()) {
        perm_callbacks.add(callback);
        REST.withCallback(new MethodCallback<Boolean>() {
          @Override
          public void onFailure(Method method, Throwable exception) {
            callback.onFailure(exception);
          }
    
    	  @Override
    	  public void onSuccess(Method method, Boolean response) {
    	    permFeatureEnabled = response;
          for(Callback<Boolean, Throwable> cb : perm_callbacks)
            cb.onSuccess(response);    
          perm_callbacks.clear();
    	  }
        }).call(this.service).getPermissionFeatureEnabled();
      }
      else {
        perm_callbacks.add(callback);
      }
    }
  }

}