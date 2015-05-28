package org.earthcube.geosoft.client.components.browse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataType;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;

public class EntityBrowser {

  Vocabulary vocabulary;
  
  public EntityBrowser(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
  }

  public String getPropertyHTML(MetadataProperty prop, boolean simple, boolean empty) {
    String labelclass = "browse-label" + 
        (empty ? " error-label" : "") +
        (!prop.isRequired() ? " optional" : "");
    
    String prophtml = "";
    if(simple)
      prophtml = "<div style='float:left'><b>"+prop.getLabel()+":&nbsp;</b></div>";
    else
      prophtml = "<div style='white-space:normal;padding:0.2em' class='" + labelclass + "'>"
        +(prop.isRequired() ? "" : "[OPTIONAL] ")
        +(prop.getQuestion() != null ? prop.getQuestion() : prop.getLabel())
        +"</div>";
    return prophtml;
  }
    
  @SuppressWarnings("unchecked")
  public String getEntityValuesHTML(MetadataProperty prop, List<Entity> entities, boolean simple) {
    MetadataType complexEntity = vocabulary.getType(KBConstants.ONTNS() + "ComplexEntity");
    MetadataType measurement = vocabulary.getType(KBConstants.ONTNS() + "MeasurementEntity");
    MetadataType location = vocabulary.getType(KBConstants.ONTNS() + "Location");
    MetadataType rangeEntity = vocabulary.getType(prop.getRange());
    
    String entitieshtml = "";
    if(!simple)
      entitieshtml += "<ul style='padding:20px;padding-top:10px;padding-bottom:10px'>";
    
    // TODO: This part should go into viewing adapters
    boolean isComplex = vocabulary.isA(rangeEntity, complexEntity);
    boolean isMeasurement = vocabulary.isA(rangeEntity, measurement);
    boolean isLocation = vocabulary.isA(rangeEntity, location);

    for(Entity entity : entities) {
      if(!simple) 
        entitieshtml += "<li>";
      if(isComplex) {
        HashMap<String, List<Entity>> subentities = new HashMap<String, List<Entity>>();
        HashMap<String, List<HashMap<String, Object>>> valuemap =
            (HashMap<String, List<HashMap<String, Object>>>)entity.getValue();
        
        List<MetadataProperty> subprops = new ArrayList<MetadataProperty>();
        for(String subpropid : valuemap.keySet()) {
          List<Entity> sublist = new ArrayList<Entity>();
          List<HashMap<String, Object>> subentityhashes = valuemap.get(subpropid);
          if(subentityhashes != null) {
            for(HashMap<String, Object> subentityhash: subentityhashes) {
              Entity subentity = new Entity(
                  (String)subentityhash.get("id"), 
                  subentityhash.get("value"),
                  (String)subentityhash.get("type"));            
              sublist.add(subentity);
            }
          }
          subentities.put(subpropid, sublist);
          subprops.add(vocabulary.getProperty(subpropid));
        }
        entitieshtml += this.getEntitiesHTML(subentities, subprops, true);
      }
      else if(isMeasurement) {
        HashMap<String, Object> measure = (HashMap<String, Object>)entity.getValue(); 
        entitieshtml += measure.get("value") + " " + measure.get("units");
      }
      else if(isLocation) {
        entitieshtml += "<a class='wrap-long-words' href='"+entity.getValue()+"'>"+entity.getValue()+"</a>";
      }
      else {
        entitieshtml += "<div class='wrap-pre wrap-long-words'>" + entity.toString() + "</div>";
      }
      if(!simple)
        entitieshtml += "</li>";
    }
    if(entities.size() > 0)
      entitieshtml += " ";
    
    if(!simple)
      entitieshtml += "</ul>";

    return entitieshtml;
  }
  
  public String getPropertyValuesHTML(MetadataProperty prop, List<Entity> entities, boolean simple) {
    String entitieshtml = getEntityValuesHTML(prop, entities, simple);
    boolean empty = (entities.size() == 0);
    String prophtml = getPropertyHTML(prop, simple, empty);
    String propclass = empty ? "hide-this-in-html" : "";
    String html = "<li class='"+propclass+"'>"
        + prophtml
        + entitieshtml
        +"</li>";
    return html;
  }
    
  public String getEntitiesHTML(
      Map<String, List<Entity>> pvals, List<MetadataProperty> props, boolean simple) {
    String html = "<ul style='padding:5px;padding-left:2px;list-style-type:none'>";
    for(MetadataProperty prop : props) {
      html += this.getPropertyValuesHTML(prop, pvals.get(prop.getId()), simple);
    }
    html += "</ul>";
    return html;
  }
}
