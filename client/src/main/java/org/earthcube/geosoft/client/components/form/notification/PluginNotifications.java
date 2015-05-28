package org.earthcube.geosoft.client.components.form.notification;

import org.earthcube.geosoft.client.components.form.SoftwareForm;
import org.earthcube.geosoft.shared.classes.util.GUID;
import org.earthcube.geosoft.shared.plugins.PluginResponse;
import org.gwtbootstrap3.client.ui.PanelGroup;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

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
  
  public void showNotificationsForSoftware(String softwareid) {
    for(int i=0; i<this.getWidgetCount(); i++) {
      PluginNotification notification = (PluginNotification) this.getWidget(i);
      if(notification.getPluginResponse().getSoftwareInfo().getId().equals(softwareid))
        notification.setVisible(true);
      else
        notification.setVisible(false);
    }
  }
}
