package org.earthcube.geosoft.client.components.form.formgroup.input;

import java.util.Date;

import org.earthcube.geosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.earthcube.geosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.gwtbootstrap3.extras.datepicker.client.ui.DatePicker;
import org.gwtbootstrap3.extras.datepicker.client.ui.base.constants.DatePickerPosition;
import org.gwtbootstrap3.extras.datepicker.client.ui.base.events.ChangeDateEvent;
import org.gwtbootstrap3.extras.datepicker.client.ui.base.events.ChangeDateHandler;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Widget;

public class DateEntityInput implements IEntityInput {
  private HandlerManager handlerManager;
  Entity entity;
  DatePicker dateinput;
  MetadataProperty property;
  
  public DateEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocab)  {
    this.property = prop;
    this.entity = e;
    dateinput = new DatePicker();
    dateinput.setPlaceholder(prop.getLabel());
    dateinput.setAutoClose(true);
    dateinput.setFormat("yyyy-mm-dd");
    dateinput.setPosition(DatePickerPosition.TOP_AUTO);
    dateinput.addChangeDateHandler(new ChangeDateHandler() {
      @Override
      public void onChangeDate(ChangeDateEvent event) {
        fireEvent(new EntityChangeEvent(getValue()));
      }
    });
    
    this.setValue(e);
  }
  
  @Override
  public Entity getValue() {
    if(dateinput.getValue() != null) {
      DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
      entity.setValue(fmt.format(dateinput.getValue()));
    }
    return entity;
  }
  
  @Override
  public void setValue(Entity entity) {
    if(entity.getValue() != null) {
      if(!(entity.getValue() instanceof Date)) {
        DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd");
        dateinput.setValue(fmt.parse(entity.getValue().toString()));
      }
      else
        dateinput.setValue((Date)entity.getValue());
    }
    this.entity = entity;
  }

  @Override
  public void clearValue() {
    if(dateinput.getValue() != null) {
      dateinput.setValue(null);
      fireEvent(new EntityChangeEvent(getValue()));
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    return true; // No validation for this ?
  }
  
  @Override
  public Widget asWidget() {
    return this.dateinput;
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
