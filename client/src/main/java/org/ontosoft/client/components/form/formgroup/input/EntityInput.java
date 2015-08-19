package org.ontosoft.client.components.form.formgroup.input;

import org.gwtbootstrap3.client.ui.Input;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class EntityInput implements IEntityInput {
  Entity entity;
  Vocabulary vocabulary;
  private HandlerManager handlerManager;
  
  Input input;
  MetadataProperty property;
  
  public EntityInput() {
    handlerManager = new HandlerManager(this);
  }

  @Override
  public Input asWidget() {
    return input;
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary)  {
    this.entity = e;
    init(prop, vocabulary);
    input = new Input();
    input.setPlaceholder(property.getLabel());
    input.addValidator(Validators.NO_BLANK_STRINGS);    
    //input.setValidateOnBlur(true);
    input.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        entity.setValue(event.getValue());
        fireEvent(new EntityChangeEvent(entity));
      }
    });
    this.setValue(e);
  }
  
  protected void init(MetadataProperty prop, Vocabulary vocabulary) {
    this.property = prop;
    this.vocabulary = vocabulary;
  }
  
  @Override
  public Entity getValue() {
    entity.setValue(input.getValue());
    return entity;
  }

  @Override
  public void setValue(Entity entity) {
    if(entity.getValue() != null)
      input.setValue(entity.getValue().toString());
    this.entity = entity;
  }
  
  @Override
  public void clearValue() {
    if(input.getValue() != null) {
      input.setValue(null);
      fireEvent(new EntityChangeEvent(getValue()));
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    return input.validate(show);
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
}
