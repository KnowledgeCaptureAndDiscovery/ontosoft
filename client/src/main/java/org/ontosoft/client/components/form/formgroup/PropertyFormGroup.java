package org.ontosoft.client.components.form.formgroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.FormLabel;
import org.gwtbootstrap3.client.ui.HelpBlock;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.ontosoft.client.components.form.SoftwareForm;
import org.ontosoft.client.components.form.events.HasPluginHandlers;
import org.ontosoft.client.components.form.events.PluginResponseEvent;
import org.ontosoft.client.components.form.events.PluginResponseHandler;
import org.ontosoft.client.components.form.events.SoftwareChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.EntityInputRegistrar;
import org.ontosoft.client.components.form.formgroup.input.IEntityInput;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.Plugin;
import org.ontosoft.shared.plugins.PluginResponse;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class PropertyFormGroup extends FormGroup implements HasPluginHandlers {
  private HandlerManager handlerManager;

  private MetadataProperty property;
  private Software software;
  private Vocabulary vocabulary;
  private List<Entity> entities;
  private SoftwareForm form;
  private List<IEntityInput> inputs;
  private Map<String, Button> pluginbuttons;
  private ButtonGroup pluginButtonGroup;
  
  public PropertyFormGroup(MetadataProperty property, SoftwareForm formview) {
    super();
    handlerManager = new HandlerManager(this);

    this.property = property;
    this.form = formview;
    
    this.software = formview.getSoftware();
    this.vocabulary = formview.getVocabulary();
    this.entities = new ArrayList<Entity>();
    this.inputs = new ArrayList<IEntityInput>();
    this.pluginbuttons = new HashMap<String, Button>();
    
    List<Entity> propEntities = software.getPropertyValues(property.getId());
    this.setValue(propEntities);
    this.addPluginButtons();
  }
  public void setRawValue(List<Object> objects) {
    List<Entity> propEntities = new ArrayList<Entity>();
    for(Object object: objects) {
      Entity entity = getNewEntity(object);
      propEntities.add(entity);
    }
    this.setValue(propEntities);
  }
    
  public List<Entity> getValue() {
    List<Entity> values = new ArrayList<Entity>();
    for (IEntityInput input : this.inputs) {
      if(input.validate(true))
        values.add(input.getValue());
    }
    return values;
  }

  public void setValue(List<Entity> entities) {
    final FormGroup thisfg = this;
    this.clear();

    FormLabel flabel = new FormLabel();
    String question = property.getQuestion();
    if(question == null)
      question = property.getLabel();
    flabel.setText(question);
    flabel.addStyleName("input-label");
    
    boolean empty = (entities == null || entities.size() == 0);
    
    final HelpBlock infoblock = new HelpBlock();
    infoblock.setIconType(IconType.EXCLAMATION_TRIANGLE);
    
    if(property.isMultiple()) {
      final Button btn = new Button();
      btn.addStyleName("btn-flat");
      btn.setTabIndex(-2);
      btn.setIcon(IconType.PLUS);
      btn.setType(ButtonType.SUCCESS);
      btn.setSize(ButtonSize.EXTRA_SMALL);
      btn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          Entity newEntity = getNewEntity(null);
          addEntityEditor(newEntity, infoblock);
          thisfg.add(infoblock);
          validate();
        }
      });
      
      InputGroup labelpanel = new InputGroup();
      InputGroupButton igbtn = new InputGroupButton();
      igbtn.add(btn);
      labelpanel.add(flabel);
      labelpanel.add(igbtn);
      this.add(labelpanel);
    }
    else {
      this.add(flabel);
    }
    if(!empty) {
      for(Entity ey : entities) {
        addEntityEditor(ey, infoblock);
      }
    } else {
      Entity newEntity = getNewEntity(null);
      addEntityEditor(newEntity, infoblock);
    }

    this.add(infoblock);

    if(empty)
      this.setValidationState(ValidationState.ERROR);    
  }
  
  private void toggleInfoBlock(final HelpBlock infoblock) {
    if(infoblock.getText().equals(""))
      infoblock.setVisible(false);
    else
      infoblock.setVisible(true);  
  }
  
  private void addEntityEditor(final Entity entity, final HelpBlock infoblock) {
    final FormGroup thisfg = this;
    
    try {
      final InputGroup ig = new InputGroup();
      final IEntityInput ip = EntityInputRegistrar.getInput(entity, property, vocabulary);
      ig.add(ip);
      
      ip.addEntityChangeHandler(new EntityChangeHandler() {
        @Override
        public void onEntityChange(EntityChangeEvent event) {
          Entity e = event.getEntity();
          if(e.getValue() != null && e.getValue().equals(""))
            e.setValue(null);
          if(!validate())
            e.setValue(null);
          
          software.updatePropertyValue(property.getId(), e);
          form.fireEvent(new SoftwareChangeEvent(software));
          //GWT.log(software.getPropertyValues(property.getId()).toString());
          
          toggleInfoBlock(infoblock);
          handleEntityPlugins(e);
        }
      });
      
      final Button btn = new Button();
      btn.addStyleName("btn-flat");
      btn.setIcon(IconType.REMOVE);
      btn.setColor("#5D7BA0");
      btn.setSize(ButtonSize.EXTRA_SMALL);
      btn.setTabIndex(-2);
      btn.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          if(property.isMultiple() && entities.size() > 1) {
            // Remove if multiple entities are there
            thisfg.remove(ig);
            entities.remove(entity);
            inputs.remove(ip);
            if(entity.getValue() != null) {
              entity.setValue(null);
              software.updatePropertyValue(property.getId(), entity);
              form.fireEvent(new SoftwareChangeEvent(software));
            }
            //GWT.log(software.getPropertyValues(property.getId()).toString());
          }
          else {
            // Clear contents (this will fire the entitychange event)
            if(entity.getValue() != null)
              entity.setValue(null);
            ip.clearValue();
          }
          validate();
          toggleInfoBlock(infoblock);
          handleEntityPlugins(entity);
        }
      });
      InputGroupButton igbtn = new InputGroupButton();
      igbtn.add(btn);
      ig.add(igbtn);
      
      this.add(ig);
      
      // Add for bookkeeping
      entities.add(entity);
      inputs.add(ip);
      
    } catch (Exception e) {
      GWT.log("Problem adding input widget for "+property.getRange(), e);
    }
  }
  
  private Entity getNewEntity(Object value) {
    return new Entity(KBConstants.randomEntityId(software.getId()), 
        value, property.getRange());
  }
  
  public boolean validate() {
    // Check all inputs in the property for validation
    boolean allok = true;
    for(IEntityInput myip: inputs)
      if(!myip.validate(true))
        allok = false;
    return allok;
  }
  
  private void handleEntityPlugins(Entity e) {
    if(property.getPlugins() == null)
      return;
    
    boolean havepluginbuttons = false; 
    for(Plugin plugin : property.getPlugins()) {
      if(!plugin.isAutomaticallyTriggered()) {
        // For Buttons
        boolean match = false;
        if(e.getValue() == null) {
          for(Entity entity : entities) {
            if(entity.getValue() != null && 
                entity.getValue().toString().matches(plugin.getValueMatchRegex())) {
              match = true;
            }
          }
        }
        else if(e.getValue() != null && 
            e.getValue().toString().matches(plugin.getValueMatchRegex())) {
          match = true;
        }
        pluginbuttons.get(plugin.getName()).setVisible(match);
        if(match)
          havepluginbuttons = true;
      }
      else {
        // For Triggers
        if(e.getValue() != null && 
          e.getValue().toString().matches(plugin.getValueMatchRegex())) {
          runPlugin(plugin.getName());
        }
      }
    }
    pluginButtonGroup.setVisible(havepluginbuttons);
  }
  
  private void addPluginButtons() {
    if(property.getPlugins() != null) {
      pluginButtonGroup = new ButtonGroup();
      pluginButtonGroup.addStyleName("btn-group-responsive has-bottom-margin");
      for(Plugin plugin : property.getPlugins()) {
        if(!plugin.isAutomaticallyTriggered()) {
          final Button btn = new Button(plugin.getLabel());
          btn.setSize(ButtonSize.EXTRA_SMALL);
          btn.setType(ButtonType.INFO);
          btn.setIcon(IconType.fromStyleName(plugin.getIcon()));
          pluginButtonGroup.add(btn);
          final String pluginname = plugin.getName();
          btn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
              btn.state().loading();
              runPlugin(pluginname);
            }
          });
          pluginbuttons.put(plugin.getName(), btn);
        }
      }
      if(pluginButtonGroup.getWidgetCount() > 0)
        this.add(pluginButtonGroup);
    }
    
    // Enable/Disable plugin buttons
    for(Entity entity : this.entities) {
        handleEntityPlugins(entity);
    }
  }
  
  private void runPlugin(final String pluginname) {
    SoftwareREST.runPlugin(pluginname, software, 
        new Callback<PluginResponse, Throwable>() {
      @Override
      public void onSuccess(PluginResponse response) {
        if(pluginbuttons.containsKey(pluginname))
          pluginbuttons.get(pluginname).state().reset();
        fireEvent(new PluginResponseEvent(response));         
      }
      @Override
      public void onFailure(Throwable reason) {
        if(pluginbuttons.containsKey(pluginname))
          pluginbuttons.get(pluginname).state().reset();
      }
    });    
  }

  @Override
  public HandlerRegistration addPluginResponseHandler(
      PluginResponseHandler handler) {
    return handlerManager.addHandler(PluginResponseEvent.TYPE, handler);
  }
  
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }
  
  public void layout() {
    if(this.isVisible())
      for(IEntityInput ip : inputs)
        ip.layout(); 
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if(visible)
      this.layout();
  }
}