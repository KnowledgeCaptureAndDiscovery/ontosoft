package org.ontosoft.client.rest;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.notify.client.constants.NotifyType;
import org.gwtbootstrap3.extras.notify.client.ui.Notify;
import org.gwtbootstrap3.extras.notify.client.ui.NotifySettings;

import com.google.gwt.core.client.GWT;

public class AppNotification {
  
  public static void notifySuccess(String message, int delay) {
    NotifySettings settings = NotifySettings.newSettings();
    settings.setType(NotifyType.SUCCESS);
    settings.setDelay(delay);
    settings.setAllowDismiss(false);
    Notify.notify("", message, IconType.SMILE_O, settings);    
  }
  
  public static void notifyFailure(String message) {
    NotifySettings settings = NotifySettings.newSettings();
    settings.setType(NotifyType.DANGER);
    settings.setAllowDismiss(false);
    GWT.log("Error: "+message);
    Notify.notify("", message, IconType.WARNING, settings);
  }
  
}
