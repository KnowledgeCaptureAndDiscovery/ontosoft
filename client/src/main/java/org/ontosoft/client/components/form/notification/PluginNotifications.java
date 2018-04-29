package org.ontosoft.client.components.form.notification;

import org.gwtbootstrap3.client.ui.PanelGroup;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;
import org.ontosoft.client.components.form.SoftwareForm;
import org.ontosoft.client.components.form.SoftwareVersionForm;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.plugins.PluginResponse;

public class PluginNotifications extends PanelGroup {
  String id;
  
  public PluginNotifications() {
    this.id = "notifications-"+GUID.get(8);
  }

  public void addPluginResponse(PluginResponse response, SoftwareForm form) {
    PluginNotification notification = new PluginNotification(response, this.id, form);
    this.add(notification);
    Animate.animate(notification, Animation.FADE_IN_DOWN, 1, 400);
  }
  
  public void addPluginResponse(PluginResponse response, SoftwareVersionForm form) {
    PluginSoftwareVersionNotification notification = new PluginSoftwareVersionNotification(response, this.id, form);
    this.add(notification);
    Animate.animate(notification, Animation.FADE_IN_DOWN, 1, 400);
  }
  
  public void showNotificationsForSoftware(String softwareid) {
    for(int i=0; i<this.getWidgetCount(); i++) {
      PluginNotification notification = (PluginNotification) this.getWidget(i);
      if(notification.getPluginResponse().getSoftwareInfo().getId().equals(softwareid))
        notification.setVisible(true);
      else
        notification.setVisible(false);
    }
  }
  
  public void showNotificationsForSoftwareVersion(String softwareid) {
    for(int i=0; i<this.getWidgetCount(); i++) {
      PluginSoftwareVersionNotification notification = (PluginSoftwareVersionNotification) this.getWidget(i);
      if(notification.getPluginResponse().getSoftwareInfo().getId().equals(softwareid))
        notification.setVisible(true);
      else
        notification.setVisible(false);
    }
  }
}
