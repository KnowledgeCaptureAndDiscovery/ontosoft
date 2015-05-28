package org.earthcube.geosoft.client.components.form.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.earthcube.geosoft.client.components.form.facet.events.FacetSelectionEvent;
import org.earthcube.geosoft.client.components.form.facet.events.FacetSelectionHandler;
import org.earthcube.geosoft.client.components.form.facet.events.HasFacetHandlers;
import org.earthcube.geosoft.shared.classes.util.GUID;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataCategory;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataType;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.earthcube.geosoft.shared.search.EnumerationFacet;
import org.gwtbootstrap3.client.ui.PanelGroup;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;

public class FacetedSearchPanel extends PanelGroup
    implements HasFacetHandlers {
  private HandlerManager handlerManager;

  String id;
  Map<String, EnumerationFacet> facets;
  List<FacetSelector> selectors;
  
  public FacetedSearchPanel() {
    this.id = "faceted-search-"+GUID.get(8);
    this.setId(this.id);
    facets = new HashMap<String, EnumerationFacet>();
    selectors = new ArrayList<FacetSelector>();
    handlerManager = new HandlerManager(this);
  }
  
  public void showFacetGroups(Vocabulary vocabulary) {
    MetadataType enumtype = vocabulary.getType(KBConstants.ONTNS()+"EnumerationEntity");
    MetadataCategory topcat = vocabulary.getCategory(KBConstants.CATNS()+"MetadataCategory");
    List<MetadataProperty> swprops = vocabulary.getPropertiesInCategory(topcat);
    swprops = vocabulary.orderProperties(swprops);
    
    // Create faceted property groups
    Map<String, List<MetadataProperty>> facetProperties = 
        new TreeMap<String, List<MetadataProperty>>();
    for(MetadataProperty prop : swprops) {
      if(prop.getSearchConfig() != null
          && prop.getSearchConfig().getFacet() != null) {
        String facetId = prop.getSearchConfig().getFacet(); 
        if(!facetProperties.containsKey(facetId))
          facetProperties.put(facetId, new ArrayList<MetadataProperty>());
        facetProperties.get(facetId).add(prop);
      }
    }
    
    
    for(String facetId : facetProperties.keySet()) {
      List<MetadataProperty> props = facetProperties.get(facetId);
      MetadataType type = vocabulary.getType(props.get(0).getRange());
      if(type == null || type.getName().equals("Software")
          || (type.getName().equals("SoftwareVersion")))
        continue;

      if(vocabulary.isA(type, enumtype)) { //TODO: && type.getEnumerations().size() > 0) {
        FacetSelector facetselector = new FacetSelector(vocabulary, 
            facetId, props, type, this.id);
        this.add(facetselector);
        selectors.add(facetselector);

        facetselector.addFacetSelectionHandler(new FacetSelectionHandler() {
          @Override
          public void onFacetSelection(FacetSelectionEvent event) {
            EnumerationFacet facet = event.getFacet();
            facets.put(facet.getFacetId(), facet);
            fireEvent(event);
          }
        });
      }
    }
    
    fetchFacetEnumerations();
  }

  public void fetchFacetEnumerations() {
    Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
      int index = 0;
      @Override
      public boolean execute() {
        selectors.get(index).fetchEnumerations();
        index++;
        if(selectors.size() == index)
          return false;
        return true;
      }
    });
  }
  
  public List<EnumerationFacet> getFacets() {
    return new ArrayList<EnumerationFacet>(facets.values());
  }

  public void setFacets(Map<String, EnumerationFacet> facets) {
    this.facets = facets;
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
