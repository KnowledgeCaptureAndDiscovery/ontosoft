package org.earthcube.geosoft.client.components.form.formgroup.input;

import java.util.HashMap;

import org.earthcube.geosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.earthcube.geosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.gwtbootstrap3.client.ui.DoubleBox;
import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class MeasurementEntityInput extends HorizontalPanel implements IEntityInput {
  private HandlerManager handlerManager;

  Entity entity;
  MetadataProperty property;
  Vocabulary vocabulary;
  
  DoubleBox valuebox;
  TextBox unitsbox;
  
  public MeasurementEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary) {
    this.entity = e;
    this.setWidth("100%");
    this.property = prop;
    
    valuebox = new DoubleBox();
    valuebox.setPlaceholder(prop.getLabel());
    valuebox.addValidator(Validators.DOUBLE);
    this.add(valuebox);
    
    unitsbox = new TextBox();
    unitsbox.getElement().getStyle().setMarginLeft(4, Unit.PX);
    unitsbox.setPlaceholder("units");
    unitsbox.addValidator(Validators.NO_BLANK_STRINGS);     
    this.add(unitsbox);
    
    valuebox.addValueChangeHandler(new ValueChangeHandler<Double>() {
      @Override
      public void onValueChange(ValueChangeEvent<Double> event) {
        fireEvent(new EntityChangeEvent(getValue(event.getValue())));
      }
    });
    
    unitsbox.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        fireEvent(new EntityChangeEvent(getValue(event.getValue())));
      }
    });
    
    this.setValue(e);
  }
  
  @Override
  public Entity getValue() {
    HashMap<String, Object> value = new HashMap<String, Object>();
    value.put("value", valuebox.getValue());
    value.put("units", unitsbox.getValue());
    entity.setValue(value);
    return entity;
  }
  
  private Entity getValue(Double val) {
    HashMap<String, Object> value = new HashMap<String, Object>();
    value.put("value", val);
    value.put("units", unitsbox.getValue());
    entity.setValue(value);
    return entity;
  }
  
  private Entity getValue(String val) {
    HashMap<String, Object> value = new HashMap<String, Object>();
    value.put("value", valuebox.getValue());
    value.put("units", val);
    entity.setValue(value);
    return entity;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Entity entity) {
    if(entity.getValue() != null) {
      HashMap<String, Object> value = (HashMap<String, Object>) entity.getValue();
      valuebox.setValue((Double)value.get("value"));
      unitsbox.setValue((String)value.get("units"));
    }
    this.entity = entity;
  }
  

  @Override
  public void clearValue() {
    if(valuebox.getValue() != null || unitsbox.getValue() != null) {
      valuebox.setValue(null);
      unitsbox.setValue(null);     
      fireEvent(new EntityChangeEvent(getValue()));
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    return valuebox.validate(show) && unitsbox.validate(show);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    super.fireEvent(event);
    handlerManager.fireEvent(event);
  }
  
  @Override
  public HandlerRegistration addEntityChangeHandler(EntityChangeHandler handler) {
    return handlerManager.addHandler(EntityChangeEvent.TYPE, handler);
  }
  
  @Override
  public void layout() { }
}
