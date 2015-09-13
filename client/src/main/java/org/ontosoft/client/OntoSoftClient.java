package org.ontosoft.client;

import org.ontosoft.client.authentication.SessionStorage;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class OntoSoftClient implements EntryPoint {
  int num = 0;
  
  public void onModuleLoad() {
    SessionStorage.loadSession();
    loadMaterialScripts();
  }
  
  private void loadMaterialScripts() {
    ScriptInjector.fromUrl(GWT.getModuleBaseURL() + "/js/material.min.js")
    .setCallback(new Callback<Void, Exception>() {
      public void onSuccess(Void result) { num++; if(num == 2) initializeMaterial();}
      public void onFailure(Exception reason) { }
    }).setWindow(ScriptInjector.TOP_WINDOW).inject();
    
    ScriptInjector.fromUrl(GWT.getModuleBaseURL() + "/js/ripples.min.js")
     .setCallback(new Callback<Void, Exception>() {
       public void onSuccess(Void result) { num++; if(num == 2) initializeMaterial();}
       public void onFailure(Exception reason) { }
    }).setWindow(ScriptInjector.TOP_WINDOW).inject();
  }
  
  private void initializeMaterial() {
    ScriptInjector.fromString("$(document).ready(function() { $.material.init(); })")
      .setWindow(ScriptInjector.TOP_WINDOW).inject();    
  }
}

