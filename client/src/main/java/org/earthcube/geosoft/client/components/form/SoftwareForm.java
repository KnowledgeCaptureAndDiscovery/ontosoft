package org.earthcube.geosoft.client.components.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.earthcube.geosoft.client.components.form.events.HasPluginHandlers;
import org.earthcube.geosoft.client.components.form.events.HasSoftwareHandlers;
import org.earthcube.geosoft.client.components.form.events.PluginResponseEvent;
import org.earthcube.geosoft.client.components.form.events.PluginResponseHandler;
import org.earthcube.geosoft.client.components.form.events.SoftwareChangeEvent;
import org.earthcube.geosoft.client.components.form.events.SoftwareChangeHandler;
import org.earthcube.geosoft.client.components.form.events.SoftwareSaveEvent;
import org.earthcube.geosoft.client.components.form.events.SoftwareSaveHandler;
import org.earthcube.geosoft.client.components.form.formgroup.PropertyFormGroup;
import org.earthcube.geosoft.client.components.form.formgroup.input.EntityInput;
import org.earthcube.geosoft.client.components.form.formgroup.input.EntityInputRegistrar;
import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataCategory;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataType;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class SoftwareForm extends Composite 
implements HasSoftwareHandlers, HasPluginHandlers {
  private HandlerManager handlerManager;

  @UiField
  Form form;
  
  @UiField
  TabPane requiredtab, optionaltab;
  
  @UiField
  TabListItem requiredtabitem, optionaltabitem;
  
  Vocabulary vocabulary;
  Software software;
  
  Map<String, PropertyFormGroup> propeditors;
  
  interface Binder extends UiBinder<Widget, SoftwareForm> { };
  private static Binder uiBinder = 
      GWT.create(Binder.class);
  
  public SoftwareForm() {
    initWidget(uiBinder.createAndBindUi(this));
    propeditors = new HashMap<String, PropertyFormGroup>();
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  @Override
  public HandlerRegistration addSoftwareSaveHandler(
      SoftwareSaveHandler handler) {
    return handlerManager.addHandler(SoftwareSaveEvent.TYPE, handler);
  }
  
  @Override
  public HandlerRegistration addSoftwareChangeHandler(
      SoftwareChangeHandler handler) {
    return handlerManager.addHandler(SoftwareChangeEvent.TYPE, handler);
  }
  
  @Override
  public HandlerRegistration addPluginResponseHandler(
      PluginResponseHandler handler) {
    return handlerManager.addHandler(PluginResponseEvent.TYPE, handler);
  }
  
  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public void setVocabulary(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
    if(this.vocabulary != null)
      registerEntityInputs();
  }

  public Software getSoftware() {
    return software;
  }

  public void setSoftware(Software software) {
    this.software = software;
  }
  
  public void setRawPropertyValues(String propid, List<Object> values) {
    PropertyFormGroup ed = propeditors.get(propid);
    ed.setRawValue(values);
    software.addPropertyValues(propid, ed.getValue());
    fireEvent(new SoftwareChangeEvent(software));    
  }
  
  public void setPropertyValues(String propid, List<Entity> entities) {
    PropertyFormGroup ed = propeditors.get(propid);
    ed.setValue(entities);
    software.addPropertyValues(propid, ed.getValue());
    fireEvent(new SoftwareChangeEvent(software));    
  }
  
  private void registerEntityInputs() {
    final String pkg = EntityInput.class.getName().replaceAll("(.*\\.).*?$", "$1");
    for(MetadataType type : vocabulary.getTypes().values()) {
      ArrayList<String> queue = new ArrayList<String>();
      //GWT.log("-------- "+type.getName());
      queue.add(type.getId());
      boolean registered = false;
      while(!queue.isEmpty()) {
        MetadataType qtype = vocabulary.getType(queue.remove(0));
        if(qtype != null) {
          String qInputWidgetClass = pkg + qtype.getName() + "Input";
          //GWT.log("Checking for "+qtype.getName()+ " = "+qInputWidgetClass);
          if(EntityInputRegistrar.register(type.getId(), qInputWidgetClass)) {
            registered = true;
            break;
          }
          queue.add(qtype.getParent());
        }
      }
      if(!registered)
        GWT.log("** Cannot find adapter for "+type.getId());
    }
  }

  public void showCategoryItems(String categoryId) {
    MetadataCategory mcat = vocabulary.getCategory(categoryId);
    if(mcat == null)
      return;
    
    for(PropertyFormGroup editor : propeditors.values())
      editor.setVisible(false);

    boolean hasOptional = false;
    boolean hasRequired = false;
    for(MetadataProperty prop: vocabulary.getPropertiesInCategory(mcat)) {
      if(propeditors.containsKey(prop.getId())) {
        if(!prop.isRequired())
          hasOptional=true;
        else
          hasRequired=true;        
        propeditors.get(prop.getId()).setVisible(true);
      }
    }
    
    optionaltab.setVisible(hasOptional);
    optionaltabitem.setVisible(hasOptional);
    
    requiredtab.setVisible(hasRequired);
    requiredtabitem.setVisible(hasRequired);
    
    requiredtab.setActive(hasRequired);
    requiredtabitem.setActive(hasRequired);
    optionaltab.setActive(!hasRequired && hasOptional);
    optionaltabitem.setActive(!hasRequired && hasOptional);
    
  }
  
  public void createFormItems() {
    if(this.vocabulary == null)
      return;
    
    propeditors.clear();
    requiredtab.clear();
    optionaltab.clear();
    
    MetadataType swtype = vocabulary.getType(software.getType());
    List<MetadataProperty> swprops = vocabulary.getPropertiesForType(swtype);
    swprops = vocabulary.filterUneditableProperties(swprops);    
    swprops = vocabulary.orderProperties(swprops);

    for(final MetadataProperty mprop : swprops) {
      PropertyFormGroup editor = new PropertyFormGroup(mprop, this);
      editor.addPluginResponseHandler(new PluginResponseHandler() {
        @Override
        public void onPluginResponse(PluginResponseEvent event) {
          fireEvent(event);
        }
      });
      if(mprop.isRequired())
        requiredtab.add(editor);
      else
        optionaltab.add(editor);
      propeditors.put(mprop.getId(), editor);
    }
  }
  
  @UiHandler("optionaltabitem")
  public void onClickOptionalTab(ClickEvent event) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        layout();
      }
    });
  }
  
  @UiHandler("requiredtabitem")
  public void onClickRequiredTab(ClickEvent event) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        layout();
      }
    });
  }

  public void layout() {
    for(PropertyFormGroup ed : propeditors.values())
      ed.layout();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if(visible)
      this.layout();
  }

}
