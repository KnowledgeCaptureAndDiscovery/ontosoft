package org.ontosoft.client.components.form.facet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.ontosoft.client.Config;
import org.ontosoft.client.components.form.facet.events.FacetSelectionEvent;
import org.ontosoft.client.components.form.facet.events.FacetSelectionHandler;
import org.ontosoft.client.components.form.facet.events.HasFacetHandlers;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.search.EnumerationFacet;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class FacetSelector extends Panel 
    implements HasFacetHandlers {
  private HandlerManager handlerManager;

  MetadataType type;
  String facetId;
  List<MetadataProperty> properties;
  List<MetadataEnumeration> enumerations;
  String target;
  String parentid;
  MultipleSelect selectbox;
  Anchor anchor;

  SoftwareREST api;
  Map<String, SoftwareREST> apis;

  public FacetSelector(Vocabulary vocabulary, String facetId,
      List<MetadataProperty> props, 
      MetadataType type, String parentid) {
    this.facetId = facetId;
    this.properties = props;
    this.type = type;
    this.parentid = parentid;
    this.target = "facet-collapse-"+GUID.get(8);
    
    this.addStyleName("no-shadow");
    
    //this.setType(PanelType.INFO);
    this.add(getPanelHeader());
    this.add(getMainPanel());    
    
    handlerManager = new HandlerManager(this);
    
    this.initAPIs();
  }
  
  private PanelHeader getPanelHeader() {
    PanelHeader header = new PanelHeader();
    header.addStyleName("small-header");
    header.setDataTarget("#" + this.target);
    header.setDataToggle(Toggle.COLLAPSE);
    header.setDataParent("#" + this.parentid);
    
    anchor = new Anchor();
    anchor.setIcon(IconType.FILTER);
    anchor.setText(facetId); 
    anchor.setIconSize(IconSize.LARGE);
    anchor.setDataToggle(Toggle.COLLAPSE);
    anchor.setDataTarget("#" + this.target);
    anchor.setDataParent("#" + this.parentid);
    
    header.add(anchor);
    return header;
  }
  
  private PanelCollapse getMainPanel() {
    PanelCollapse collapse = new PanelCollapse();
    collapse.setId(this.target);
    collapse.add(this.getContentPanel());;
    return collapse;
  }
  
  private PanelBody getContentPanel() {
    final PanelBody body = new PanelBody();
    body.addStyleName("tightish-body");
    
    HorizontalPanel panel = new HorizontalPanel();
    panel.addStyleName("fixed-layout-table");
    panel.setWidth("100%");
    panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
    body.add(panel);
    
    selectbox = new MultipleSelect();
    selectbox.setCountSelectedTextFormat(2);
    selectbox.setFixedMenuSize(8);
    selectbox.setLiveSearch(true);
    selectbox.setTitle("Select one or more");
    selectbox.addValueChangeHandler(new ValueChangeHandler<List<String>>() {
      @Override
      public void onValueChange(ValueChangeEvent<List<String>> event) {
        handleEnumerationText();
      }
    });
    panel.add(selectbox);
    
    Button btn = new Button();
    btn.addStyleName("btn-flat");
    btn.setIcon(IconType.REMOVE);
    btn.setColor("#5D7BA0");
    btn.setSize(ButtonSize.EXTRA_SMALL);
    btn.setTabIndex(-2);
    btn.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        selectbox.setValue(Arrays.asList(""));
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
          public void execute() {
            handleEnumerationText();
          }
        });
        event.stopPropagation();
      }
    });

    panel.add(btn);
    panel.setCellWidth(btn, "24px");
    
    return body;
  }
  
  private void initAPIs() {
    this.api = SoftwareREST.get(Config.getServerURL());
    
    this.apis = new HashMap<String, SoftwareREST>();
    this.apis.put(SoftwareREST.LOCAL, this.api);
    
    final List<Map<String, String>> xservers = Config.getExternalServers();
    for(Map<String, String> xserver : xservers) {
      String xname = xserver.get("name");
      String xurl = xserver.get("server");
      SoftwareREST xapi = SoftwareREST.get(xurl);
      this.apis.put(xname, xapi);
    }    
  }
  
  public void fetchEnumerations() {
    final List<String> loaded = new ArrayList<String>();
    this.enumerations = new ArrayList<MetadataEnumeration>();
    final HashSet<MetadataEnumeration> uniques = new HashSet<MetadataEnumeration>();
    for(final String sname : this.apis.keySet()) {
      SoftwareREST sapi = this.apis.get(sname);
      sapi.getEnumerationsForType(type.getId(), 
          new Callback<List<MetadataEnumeration>, Throwable>() {
            @Override
            public void onSuccess(List<MetadataEnumeration> enumlist) {
              loaded.add(sname);
              uniques.addAll(enumlist);
              if(loaded.size() == apis.keySet().size()) {
                enumerations.addAll(uniques);
                Collections.sort(enumerations, new Comparator<MetadataEnumeration>() {
                  @Override
                  public int compare(MetadataEnumeration enum1, 
                      MetadataEnumeration enum2) {
                    if(enum1.getLabel() != null && enum2.getLabel() != null)
                      return enum1.getLabel().compareToIgnoreCase(enum2.getLabel());
                    return 0;
                  }
                });
                for(MetadataEnumeration menum : enumerations) {
                  Option opt = new Option();
                  opt.setText(menum.getLabel());
                  opt.setValue(menum.getId());
                  selectbox.add(opt);
                }
                selectbox.refresh();
              }
            }
            public void onFailure(Throwable reason) {
              GWT.log("WARNING !! could not load enumerations for "+type.getId(), reason);
            }
        }
      );
    }
  }
  
  private void handleEnumerationText() {
    List<String> enumerationIds = selectbox.getValue();
    EnumerationFacet facet = new EnumerationFacet();
    facet.setFacetId(facetId);
    facet.setEnumerationIds(enumerationIds);
    List<String> propertyIds = new ArrayList<String>();
    for(MetadataProperty prop : this.properties)
      propertyIds.add(prop.getId());
    facet.setPropertyIds(propertyIds);
    
    String enumtext = "";
    int i=0;
    for(MetadataEnumeration menum : enumerations) {
      if(facet.getEnumerationIds().contains(menum.getId())) {
        if(i > 0)
          enumtext += " OR ";
        enumtext += menum.getLabel();
        i++;
      }
    }
    
    if(enumerationIds.size() > 0) {
      anchor.setText(facetId + ": " + enumtext);
      anchor.setColor("white");
      this.setType(PanelType.INFO);
    }
    else {
      anchor.setText(facetId);
      anchor.setColor(null);
      this.setType(PanelType.DEFAULT);
    }    

    fireEvent(new FacetSelectionEvent(facet));    
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }
  
  @Override
  public HandlerRegistration addFacetSelectionHandler(
      FacetSelectionHandler handler) {
    return handlerManager.addHandler(FacetSelectionEvent.TYPE, handler);
  }
}
