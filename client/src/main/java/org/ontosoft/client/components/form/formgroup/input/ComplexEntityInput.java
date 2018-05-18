package org.ontosoft.client.components.form.formgroup.input;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.FieldSet;
import org.gwtbootstrap3.client.ui.InputGroup;
import org.gwtbootstrap3.client.ui.InputGroupButton;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Trigger;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeEvent;
import org.ontosoft.client.components.form.formgroup.input.events.EntityChangeHandler;
import org.ontosoft.shared.classes.entities.ComplexEntity;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.provenance.Activity;
import org.ontosoft.shared.classes.provenance.Agent;
import org.ontosoft.shared.classes.provenance.ProvEntity;
import org.ontosoft.shared.classes.provenance.Provenance;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataClass;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.ui.Label;

public class ComplexEntityInput extends FieldSet implements IEntityInput {
  private List<Entity> entities;
  private HandlerManager handlerManager;
  SoftwareVersion version;
  ComplexEntity entity;
  MetadataProperty property;
  Vocabulary vocabulary;
  
  HashMap<String, List<IEntityInput>> inputs;
  
  public ComplexEntityInput() {
    handlerManager = new HandlerManager(this);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary) {
	this.entities = new ArrayList<Entity>();
    this.inputs = new HashMap<String, List<IEntityInput>>();
    this.property = prop;
    this.vocabulary = vocabulary;
    this.addStyleName("bordered-fieldset");
    
    MetadataType type = vocabulary.getType(this.property.getRange());
    
    List<MetadataProperty> subprops = vocabulary.getPropertiesForType(type);
	subprops = vocabulary.orderProperties(subprops);
    
    for(MetadataProperty subprop : subprops) {
      String subentityid = e.getId() + "-" + GUID.get();
      Entity subentity = null;
      try {
        subentity = EntityRegistrar.getEntity(subentityid, null, subprop.getRange());
      } catch (Exception ex) {
        GWT.log("Could not get a new entity", ex);
        continue;
      }
      try {
        subprop.setRequired(prop.isRequired());
        String tip = subprop.getQuestion();
        if(tip == null)
          tip = subprop.getLabel();
        Tooltip tooltip = new Tooltip(tip);
        tooltip.setPlacement(Placement.BOTTOM);
        tooltip.setTrigger(Trigger.FOCUS);
        IEntityInput ip = EntityRegistrar.getInput(subentity, subprop, vocabulary);
        tooltip.add(ip.asWidget());
        this.add(tooltip);
        
        ip.addEntityChangeHandler(new EntityChangeHandler() {
	        @Override
	        public void onEntityChange(EntityChangeEvent event) {
	          fireEvent(new EntityChangeEvent(getValue()));
	        }
	      });
        
        List<IEntityInput> entInputs = inputs.get(subprop.getId());
        if (entInputs == null)
        {
        	entInputs = new ArrayList<IEntityInput>();
        }
        entInputs.add(ip);
        inputs.put(subprop.getId(), entInputs);
      }
      catch (Exception exception) {
        GWT.log("Problem adding sub widget: "+subprop.getId()+" for "+prop.getId(), exception);
      }
    }
    this.entity = (ComplexEntity) e;
    this.setValue(e);
  }
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocabulary, final SoftwareVersion version) {
    final FieldSet thisfs = this;
	this.version = version;
	this.inputs = new HashMap<String, List<IEntityInput>>();
    this.property = prop;
    this.vocabulary = vocabulary;
    this.addStyleName("bordered-fieldset");
    
    MetadataType type = vocabulary.getType(this.property.getRange());
    
    List<MetadataProperty> subprops = vocabulary.getPropertiesForType(type);
	subprops = vocabulary.orderProperties(subprops);
    
    for(final MetadataProperty subprop : subprops) {
      List<Entity> propEntities = ((ComplexEntity) e).getPropertyValues(subprop.getId());
      InputGroup labelpanel = new InputGroup();
      InputGroupButton igbtn = new InputGroupButton();
      
      if(subprop.isMultiple()) {
        final Button btn = new Button();
        btn.addStyleName("btn-flat");
        btn.setTabIndex(-2);
        btn.setIcon(IconType.PLUS);
        btn.setType(ButtonType.SUCCESS);
        btn.setSize(ButtonSize.EXTRA_SMALL);
        
        igbtn.add(btn);
        labelpanel.add(igbtn);
        this.add(labelpanel);
        final int index = this.getWidgetIndex(labelpanel);
        
        btn.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
	        	Entity newEntity = getNewEntity(null, subprop);
	            addEntityEditor(newEntity, subprop, index);
          }
        });
        
        
        //Entity newEntity = getNewEntity(null);
        //addEntityEditor(e, subprop, version, form);
      }
      
      int size = (propEntities != null)?propEntities.size():1;
      
      for (int i = 0; i < size; i++)
      {
	      String subentityid = e.getId() + "-" + GUID.get();
	      Entity subentity = null;
	      try {
	        subentity = EntityRegistrar.getEntity(subentityid, null, subprop.getRange());
	      } catch (Exception ex) {
	        GWT.log("Could not get a new entity", ex);
	        continue;
	      }
	      try {
	        subprop.setRequired(prop.isRequired());
	        String tip = subprop.getQuestion();
	        if(tip == null)
	          tip = subprop.getLabel();
	        final Tooltip tooltip = new Tooltip(tip);
	        tooltip.setPlacement(Placement.BOTTOM);
	        tooltip.setTrigger(Trigger.FOCUS);
	        final IEntityInput ip = EntityRegistrar.getInput(subentity, subprop, vocabulary);
	        tooltip.add(ip.asWidget());
	        
	        if (subprop.isMultiple()) {
		        final InputGroup ig = new InputGroup();
		        final Button btn = new Button();
		        btn.addStyleName("btn-flat");
		        btn.setIcon(IconType.REMOVE);
		        btn.setColor("#5D7BA0");
		        btn.setSize(ButtonSize.EXTRA_SMALL);
		        btn.setTabIndex(-2);
		        btn.addClickHandler(new ClickHandler() {
		          @Override
		          public void onClick(ClickEvent event) {
		            if(subprop.isMultiple()) {
		              // Remove if multiple entities are there
		              thisfs.remove(ig);
		              ip.clearValue();
		              //entities.remove(entity);
		              inputs.remove(ip);
		              if(entity.getValue() != null) {
		            	  GWT.log("remove!");
		                entity.setValue(null);
		              }
		              GWT.log("remove!");
		            }
		            else {
		              // Clear contents (this will fire the entitychange event)
		              if(entity.getValue() != null)
		                entity.setValue(null);
		              ip.clearValue();
		            }
		          }
		        });
		        
		        InputGroupButton igbtn2 = new InputGroupButton();
		        igbtn2.add(btn);
		        ig.add(tooltip);
		        ig.add(igbtn2);
		        
		        this.add(ig);
	        }
	        else {
	        	this.add(tooltip);
	        }
	
	        ip.addEntityChangeHandler(new EntityChangeHandler() {
	          @Override
	          public void onEntityChange(EntityChangeEvent event) {
	            fireEvent(new EntityChangeEvent(getValue()));
	          }
	        });
	        List<IEntityInput> entInputs = inputs.get(subprop.getId());
	        if (entInputs == null)
	        {
	        	entInputs = new ArrayList<IEntityInput>();
	        }
	        entInputs.add(ip);
	        inputs.put(subprop.getId(), entInputs);
	      }
	      catch (Exception exception) {
	        GWT.log("Problem adding sub widget: "+subprop.getId()+" for "+prop.getId(), exception);
	      }
      }
    }
    this.entity = (ComplexEntity) e;
    this.setValue(e);
  }
  
  /***
   * Assumption: Multiple entries for each subproperty aren't allowed
   **/
  @Override
  public Entity getValue() {
    HashMap<String, List<Entity>> subentities = 
        new HashMap<String, List<Entity>>();
    for(String propid: inputs.keySet()) {
    	List<Entity> entities = new ArrayList<Entity>();
    	for (IEntityInput input : inputs.get(propid)) {
	      Entity subentity = input.getValue();
	      if(subentity != null)
	        entities.add(subentity);
	      subentities.put(propid, entities);
    	}
    }
    this.entity.setValue(subentities);;
    return this.entity;
  }


  @Override
  public void setValue(Entity e) {
    ComplexEntity ce = (ComplexEntity) e;
    if(ce.getValue() != null) {
      for(String propid: inputs.keySet()) {
        List<Entity> subentities = ce.getPropertyValues(propid);
        if(subentities != null && subentities.size() > 0) {
        	int i = 0;
	    	for (IEntityInput input : inputs.get(propid)) {
	    		if (subentities.size() >= i)
	    			input.setValue(subentities.get(i++));
	        }
    	}
      }
    }
    this.entity = ce;
  }

  @Override
  public void clearValue() {
    for(String propid: inputs.keySet()) {
    	for (IEntityInput input : inputs.get(propid)) {
    		input.clearValue();
    	}
    }
  }
  
  @Override
  public boolean validate(boolean show) {
    for(String propid: inputs.keySet()) {
    	for (IEntityInput input : inputs.get(propid)) {
	      if(!input.validate(show))
	        return false;
    	}
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
    	for (IEntityInput input : inputs.get(propid)) {
    		input.layout();
    	}
    }    
  }
  
  @Override
  public void disable() {
    for(String propid: inputs.keySet()) {
    	for (IEntityInput input : inputs.get(propid)) {
    		input.disable();
    	}
    }
  }
  
  private Entity getNewEntity(Object value, MetadataProperty subprop) {
    String proptype = subprop.getRange();
    String id = GUID.randomEntityId(version.getId(), proptype);
    
    // Convert software entities to enumerations
    MetadataClass topclass = vocabulary.getType(KBConstants.ONTNS()+"SoftwareVersion");
    MetadataClass eclass = vocabulary.getType(proptype);
    if(vocabulary.isA(eclass, topclass)) {
      proptype = KBConstants.ONTNS()+"EnumerationEntity";
      id = id.replace(version.getName()+"#", "");
    }
    
    try {
      Entity entity = EntityRegistrar.getEntity(id, value, proptype);
      entity.setType(subprop.getRange());
      return entity;
    } catch (Exception e) {
      GWT.log("Coult not get a new entity", e);
      return null;
    }
  }
  
  private void addEntityEditor(final Entity entity, final MetadataProperty subprop, int index) {
    final FieldSet thisfs = this;
    
    try {
      final InputGroup ig = new InputGroup();
      final IEntityInput ip = EntityRegistrar.getInput(entity, subprop, vocabulary);
      ig.add(ip);
      
      ip.addEntityChangeHandler(new EntityChangeHandler() {
          @Override
          public void onEntityChange(EntityChangeEvent event) {
            fireEvent(new EntityChangeEvent(getValue()));
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
          if(property.isMultiple()) {
            // Remove if multiple entities are there
            thisfs.remove(ig);
            //entities.remove(entity);
            inputs.remove(ip);
            if(entity.getValue() != null) {
              entity.setValue(null);

            }
            //GWT.log(software.getPropertyValues(property.getId()).toString());
          }
          else {
            // Clear contents (this will fire the entitychange event)
            if(entity.getValue() != null)
              entity.setValue(null);
            ip.clearValue();
          }
        }
      });
      
      InputGroupButton igbtn = new InputGroupButton();
      igbtn.add(btn);
      ig.add(igbtn);
      
      this.insert(ig, index+1);
      
      Provenance prov = this.version.getProvenance();
      ProvEntity pentity = prov.getEntity(entity.getId());
      if(pentity != null) {
        Activity activity = prov.getActivity(pentity.getGeneratedBy());
        if(activity != null) {
          Agent agent = prov.getAgent(activity.getAgentId());
          if(agent != null) {
            Label flabel = new Label();
            flabel.addStyleName("provenance-label");
            DateTimeFormat fmt = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
            String datestr = fmt.format(activity.getTime());
            flabel.setText("Last edited by " + agent.getName() + " at "+datestr);
            this.add(flabel);
          }
        }
      }
      
      // Add for bookkeeping
      //entities.add(entity);
      List<IEntityInput> entInputs = inputs.get(subprop.getId());
      if (entInputs == null)
      {
      	entInputs = new ArrayList<IEntityInput>();
      }
      entInputs.add(ip);
      inputs.put(subprop.getId(), entInputs);
     
      
    } catch (Exception e) {
      GWT.log("Problem adding input widget for "+property.getName(), e);
    }
  }
}