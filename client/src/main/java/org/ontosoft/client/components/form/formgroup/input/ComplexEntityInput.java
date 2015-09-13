package org.ontosoft.client.components.form.formgroup.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class ComplexEntityInput extends FieldSet implements IEntityInput {
  private HandlerManager handlerManager;

  Entity entity;
  MetadataProperty property;
  Vocabulary vocabulary;
  
  HashMap<String, IEntityInput> inputs;
  
  public ComplexEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary) {
    this.inputs = new HashMap<String, IEntityInput>();
    this.property = prop;
    this.vocabulary = vocabulary;
    this.addStyleName("bordered-fieldset");
    
    MetadataType type = vocabulary.getType(this.property.getRange());
    for(MetadataProperty subprop : vocabulary.getPropertiesForType(type)) {
      String subentityid = e.getId() + "-" + GUID.get();  
      Entity subentity = new Entity(subentityid, null, subprop.getRange());
      try {
        subprop.setRequired(prop.isRequired());
        String tip = subprop.getQuestion();
        if(tip == null)
          tip = subprop.getLabel();
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setPlacement(Placement.BOTTOM);
        tooltip.setTrigger(Trigger.FOCUS);
        IEntityInput ip = EntityInputRegistrar.getInput(subentity, subprop, vocabulary);
        tooltip.add(ip.asWidget());
        this.add(tooltip);
        
        ip.addEntityChangeHandler(new EntityChangeHandler() {
          @Override
          public void onEntityChange(EntityChangeEvent event) {
            fireEvent(new EntityChangeEvent(getValue()));
          }
        });
        
        inputs.put(subprop.getId(), ip);
      }
      catch (Exception exception) {
        GWT.log("Problem adding sub widget: "+subprop.getId()+" for "+prop.getId(), exception);
      }
    }
    this.entity = e;
    this.setValue(e);
  }
  
  /***
   * Assumption: Multiple entries for each subproperty aren't allowed
   **/
  @Override
  public Entity getValue() {
    HashMap<String, List<HashMap<String, Object>>> subentities = 
        new HashMap<String, List<HashMap<String, Object>>>();
    for(String propid: inputs.keySet()) {
      IEntityInput input = inputs.get(propid);
      List<HashMap<String, Object>> entities = new ArrayList<HashMap<String, Object>>();
      Entity subentity = input.getValue();
      if(subentity != null) {
        HashMap<String, Object> entityhash = new HashMap<String, Object>();
        entityhash.put("id", subentity.getId());
        entityhash.put("value", subentity.getValue());
        entityhash.put("type", subentity.getType());
        entities.add(entityhash);
      }
      subentities.put(propid, entities);
    }
    this.entity.setValue(subentities);;
    return this.entity;
  }


  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Entity e) {
    HashMap<String, List<HashMap<String, Object>>> valuemap = 
        (HashMap<String, List<HashMap<String, Object>>>) e.getValue();
    
    if(valuemap != null) {
      for(String propid: inputs.keySet()) {
        IEntityInput input = inputs.get(propid);
        List<HashMap<String, Object>> subentityhashes = valuemap.get(propid);
        if(subentityhashes != null) {
          for(HashMap<String, Object> subentityhash: subentityhashes) {
            Entity subentity = new Entity(
                (String)subentityhash.get("id"), 
                subentityhash.get("value"),
                (String)subentityhash.get("type"));            
            input.setValue(subentity);
          }
        }
      }
    }
    this.entity = e;
  }

  @Override
  public void clearValue() {
    for(String propid: inputs.keySet()) {
      IEntityInput input = inputs.get(propid);
      input.clearValue();
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    for(String propid: inputs.keySet()) {
      IEntityInput input = inputs.get(propid);
      if(!input.validate(show))
        return false;
    }
    return true;
  }
  
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }
  
  @Override
  public HandlerRegistration addEntityChangeHandler(EntityChangeHandler handler) {
    return handlerManager.addHandler(EntityChangeEvent.TYPE, handler);
  }
  
  @Override
  public void layout() { 
    for(String propid: inputs.keySet()) {
      IEntityInput input = inputs.get(propid);
      input.layout();
    }    
  }
}