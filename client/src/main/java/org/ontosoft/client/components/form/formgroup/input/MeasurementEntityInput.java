package org.ontosoft.client.components.form.formgroup.input;

import org.gwtbootstrap3.client.ui.DoubleBox;
import org.gwtbootstrap3.client.ui.TextBox;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.MeasurementEntity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class MeasurementEntityInput extends HorizontalPanel implements IEntityInput {
  private HandlerManager handlerManager;

  MeasurementEntity entity;
  MetadataProperty property;
  Vocabulary vocabulary;
  
  DoubleBox valuebox;
  TextBox unitsbox;
  
  public MeasurementEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary) {
    this.entity = (MeasurementEntity) e;
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
    entity.setValue(valuebox.getValue());
    entity.setUnits(unitsbox.getValue());
    return entity;
  }
  
  private Entity getValue(Double value) {
    entity.setValue(value);
    return entity;
  }
  
  private Entity getValue(String units) {
    this.entity.setUnits(units);
    return entity;
  }

  @Override
  public void setValue(Entity entity) {
    MeasurementEntity me = (MeasurementEntity) entity;
    if(entity.getValue() != null) {
      valuebox.setValue(this.entity.getValue());
      unitsbox.setValue(this.entity.getUnits());
    }
    this.entity = me;
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
  
  @Override
  public void disable() {
    valuebox.setEnabled(false);
    unitsbox.setEnabled(false);
  }
}
