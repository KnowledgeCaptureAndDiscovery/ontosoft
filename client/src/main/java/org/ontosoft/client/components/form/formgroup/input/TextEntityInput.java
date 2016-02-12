package org.ontosoft.client.components.form.formgroup.input;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.TextArea;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.TextEntity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

public class TextEntityInput extends Container implements IEntityInput {
  private HandlerManager handlerManager;

  TextEntity entity;
  MetadataProperty property;
  Vocabulary vocabulary;
  
  // Maximum height to autogrow to before scrollbar kicks
  private int maxHeight = 400;
  
  // Maximum number of characters allowed in a field
  private int maxLength = 5000;
  
  // A bit of padding for the height
  //private int padding = 5;
  
  private int lineHeight = 20;
  
  // A Div to mirror the textarea contents to calculate height
  private HTML mirrorDiv;
  
  private int rows = 1;
  
  // Custom TextArea
  private MyTextArea myinput;
  class MyTextArea extends TextArea {
    public MyTextArea() {
      super();
      sinkEvents(Event.ONPASTE);
    }
    @Override
    public void onBrowserEvent(Event event) {
      super.onBrowserEvent(event);
      switch (event.getTypeInt()) {
      case Event.ONPASTE:
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
          public void execute() {
            updateEditorDimensions();
          }
        });
        break;
      }
    }
  }

  public TextEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary)  {
    this.setFluid(true);
    this.addStyleName("no-pad-container");
    this.entity = (TextEntity) e;
    this.property = prop;
    this.vocabulary = vocabulary;
    
    // Create text area
    this.myinput = new MyTextArea();
    myinput.getElement().getStyle().setLineHeight(lineHeight, Unit.PX);
    myinput.setMaxLength(maxLength);
    myinput.addStyleName("grow-textarea");
    myinput.addValidator(Validators.NO_BLANK_STRINGS);  
    myinput.setVisibleLines(this.rows);
    myinput.setPlaceholder(prop.getLabel() + " (Text)");
    this.add(myinput);
    
    myinput.addValueChangeHandler(new ValueChangeHandler<String>() {
      @Override
      public void onValueChange(ValueChangeEvent<String> event) {
        entity.setValue(event.getValue());
        fireEvent(new EntityChangeEvent(entity));
      }
    });
    
    this.initializeAutoGrow();
    
    this.setValue(e);
  }

  private void initializeAutoGrow() {
    // Create a hidden mirror div
    mirrorDiv = new HTML();
    mirrorDiv.setVisible(false);
    mirrorDiv.getElement().setAttribute("aria-hidden", "true");
    mirrorDiv.setStyleName("mirror-text wrap-long-words");
    mirrorDiv.getElement().getStyle().setLineHeight(lineHeight, Unit.PX);
    this.add(mirrorDiv);

    // Add handlers to adjust textarea height according to mirrorDiv
    myinput.addKeyDownHandler(new KeyDownHandler() {
      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == 13) {
          myinput.setVisibleLines(++rows);
        }
        else {
          Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            public void execute() {
              updateEditorDimensions();
            }
          });
        }
      }
    });
    Window.addResizeHandler(new ResizeHandler() {
      public void onResize(ResizeEvent event) {
        updateEditorDimensions();
      }
    });
  }
  
  @Override
  public void onLoad() {
    updateEditorDimensions();
  }
  
  @Override
  public Entity getValue() {
    entity.setValue(myinput.getValue());
    return entity;
  }

  @Override
  public void setValue(Entity entity) {
    TextEntity te = (TextEntity) entity;
    if(te.getValue() != null) {
      myinput.setValue(te.getValue());
      this.updateEditorDimensions();
    }
    this.entity = te;
  }
  
  @Override
  public void clearValue() {
    if(myinput.getValue() != null) {
      myinput.setValue(null);
      this.updateEditorDimensions();
      fireEvent(new EntityChangeEvent(getValue()));
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    return myinput.validate(show);
  }

  private String valueForMirror(String value) {
    if (value == null || value.equals(""))
      return "&nbsp;";

    return value.replace("&", "&amp;").replace("\"", "&quot;")
        .replace("'", "&#39;").replace("<", "&lt;").replace(">", "&gt;")
        .replaceAll("\n$", "<br/>&nbsp;").replace("\n", "<br/>");
  }

  private void updateEditorDimensions() {
    mirrorDiv.setHTML(valueForMirror(myinput.getValue()));
    mirrorDiv.setVisible(true);
    mirrorDiv.getElement().getStyle().setVisibility(Visibility.VISIBLE);
    int height = this.mirrorDiv.getOffsetHeight();
    mirrorDiv.setVisible(false);

    if (height > this.maxHeight)
      height = this.maxHeight;
    
    if (height > 0) {
      this.rows = (int) Math.ceil(height/lineHeight);
      myinput.setVisibleLines(this.rows);
      //myinput.setHeight(height + padding + "px");
    }
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
  public void layout() {
    this.updateEditorDimensions();
  }
  
  @Override
  public void disable() {
    myinput.setEnabled(false);
  }
}
