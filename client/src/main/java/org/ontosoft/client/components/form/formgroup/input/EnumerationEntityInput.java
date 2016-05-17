package org.ontosoft.client.components.form.formgroup.input;

import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.SuggestBox;
import org.ontosoft.client.Config;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.EnumerationEntity;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class EnumerationEntityInput implements IEntityInput {
  private HandlerManager handlerManager;

  EnumerationEntity entity;
  SuggestBox myinput;
  MetadataProperty property;
  HashMap<String, Entity> enumerations;
  SoftwareREST api = SoftwareREST.get(Config.getServerURL());
  
  public EnumerationEntityInput() {
    enumerations = new HashMap<String, Entity>();
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public Widget asWidget() {
    return myinput;
  }
  
  @Override
  public void createWidget(Entity e, final MetadataProperty prop, Vocabulary vocabulary) {
    this.property = prop;
    this.entity = (EnumerationEntity) e;
    
    // Create suggestions
    final MultiWordSuggestOracle oracle = new MultiWordSuggestOracle();
    final MetadataType type = vocabulary.getType(prop.getRange());
    this.api.getEnumerationsForType(type.getId(), 
        new Callback<List<MetadataEnumeration>, Throwable>() {
          @Override
          public void onSuccess(List<MetadataEnumeration> enumlist) {
            for(MetadataEnumeration menum : enumlist) {
              String label = menum.getLabel() != null ? menum.getLabel() : menum.getName();
              EnumerationEntity entity = new EnumerationEntity();
              entity.setId(menum.getId());
              entity.setValue(label);
              entity.setType(prop.getRange());
              enumerations.put(label, entity);
            }
            oracle.addAll(enumerations.keySet());
          }
          @Override
          public void onFailure(Throwable reason) {
            GWT.log("WARNING !! could not load enumerations for "+type.getId(), reason);
          }
        }
    );
    
    // Create suggest box
    myinput = new SuggestBox(oracle);
    myinput.setPlaceholder(prop.getLabel() + 
        (type.getLabel() != null ? " ("+type.getLabel()+")" : ""));
    myinput.addValidator(Validators.NO_BLANK_STRINGS); 
    
    myinput.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        fireEvent(new EntityChangeEvent(getValue(event.getValue())));
      }
    });
    myinput.addSelectionHandler(new SelectionHandler<Suggestion>() {
      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        fireEvent(new EntityChangeEvent(getValue(event.getSelectedItem().getReplacementString())));
      }
    });
    
    this.setValue(e);
  }

  @Override
  public Entity getValue() {
    String value = myinput.getValue();
    if(enumerations.containsKey(value))
      entity.copyFrom(enumerations.get(value));
    else
      entity.setValue(value);
    return entity;
  }
  
  private Entity getValue(String value) {;
    if(enumerations.containsKey(value))
      entity.copyFrom(enumerations.get(value));
    else
      entity.setValue(value);
    return entity;
  }

  @Override
  public void setValue(Entity entity) {
    EnumerationEntity ee = (EnumerationEntity) entity;
    if(ee.getLabel() != null)
      myinput.setValue(ee.getLabel());
    this.entity = ee;
  }
  
  @Override
  public void clearValue() {
    if(myinput.getValue() != null) {
      myinput.setValue(null);
      fireEvent(new EntityChangeEvent(getValue()));
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    return myinput.validate(show);
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
  public void layout() { }
  
  @Override
  public void disable() {
    myinput.setEnabled(false);
  }
}
