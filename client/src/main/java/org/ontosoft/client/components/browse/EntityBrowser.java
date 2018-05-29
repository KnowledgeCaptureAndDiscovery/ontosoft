package org.ontosoft.client.components.browse;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.ontosoft.client.place.NameTokens;
import org.ontosoft.shared.classes.entities.ComplexEntity;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.EnumerationEntity;
import org.ontosoft.shared.classes.entities.MeasurementEntity;
import org.ontosoft.shared.classes.entities.TextEntity;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

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
      prophtml = "<b>"+prop.getLabel()+":&nbsp;</b>";
    else
      prophtml = "<div style='white-space:normal;padding:0.2em' class='" + labelclass + "'>"
        +(prop.isRequired() ? "" : "[OPTIONAL] ")
        +(prop.getQuestion() != null ? prop.getQuestion() : prop.getLabel())
        +"</div>";
    return prophtml;
  }
  
  public String getFunctionHeaderHTML(ComplexEntity entity) {  
	Entity functionName = entity.getPropertyValue(KBConstants.ONTNS() + "hasFunctionName");
	Entity functionDescription = entity.getPropertyValue(KBConstants.ONTNS() + "hasFunctionDescription");
	Entity functionality = entity.getPropertyValue(KBConstants.ONTNS() + "hasFunctionality");
	Entity algorithm = entity.getPropertyValue(KBConstants.ONTNS() + "usesAlgorithm");

	String prophtml = "<dl class=\"method\">\n" + 
			"   <dt id=\"flopy.modflow.mfbas.ModflowBas.plot\">\n";
	
	if (functionName != null)
	{
		prophtml += "<code class=\"descname\">" + functionName.getValue() + "</code>\n";
	}
	prophtml +=  "   </dt>\n" + 
				"   <dd>\n";
	if (functionDescription != null)
	{
		prophtml += "      <p>" + functionDescription.getValue() + "\n" + 
			"      </p>\n";
	}
	if (algorithm != null)
	{
		prophtml += "      <p><b>Uses Algorithm:</b> " + algorithm.toString() + "\n" + 
			"      </p>\n";
	}
	if (functionality != null)
	{
		prophtml +=		"      <p><b>Functionality:</b> " + functionality.toString() + "\n" + 
			"      </p>\n"; 
	}
	prophtml +=		"      <table class=\"docutils field-list\" frame=\"void\" rules=\"none\">\n" + 
			"         <col class=\"field-name\" />\n" + 
			"         <col class=\"field-body\" />\n" + 
			"         <tbody valign=\"top\">";
	
    return prophtml;
  }
  
  public String getFunctionEntitiesHTML(ComplexEntity entity, List<MetadataProperty> props, boolean simple) {
  	  
    String html = "<ul style='padding:5px;padding-left:5px'>";
    
    for(MetadataProperty prop : props) {
    	if (prop != null)
        {
    		html += "<li>" + this.getFunctionEntityValuesHTML(prop, entity.getPropertyValues(prop.getId()), simple) + "</li>";
        }
    }
    html += "</ul>";
    
    return html;
  }
  
  public String getInputsHTML(List<Entity> entities)
  {
	  String html = "<tr class=\"field-odd field\">\n" + 
	  		"               <th class=\"field-name\">Parameters:</th>\n" + 
	  		"               <td class=\"field-body\">\n" + 
	  		"                  <blockquote>\n" + 
	  		"                     <div>\n" + 
	  		"                        <dl class=\"docutils\">";
	  
	  for (Entity entity : entities)
	  {
		  html += getInputHTML((ComplexEntity) entity);
	  }
	  
	  html += "</dl>\n" + 
	  		"                     </div>\n" + 
	  		"                  </blockquote>\n" + 
	  		"               </td>\n" + 
	  		"            </tr>";
	  
	  return html;
  }
  
  public String getInputHTML(ComplexEntity entity)
  {
	  String html = "";
	  
	  Entity inputName = null;
	  Entity inputDescription = null;
	  Entity inputDataFormat = null;
	  Entity inputDefaultValue = null;
	  Entity inputDataType = null;
	  
	  if (entity.getType() == KBConstants.ONTNS() + "InputFile")
	  {
		  inputName = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputFileName");
		  inputDescription = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputFileDescription");
		  inputDataFormat = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputFileDataFormat");
		  inputDefaultValue = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputFileDefaultValue");
		  inputDataType = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputFileDataType");
	  }
	  else if (entity.getType() == KBConstants.ONTNS() + "InputParameter")
	  {
		  inputName = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputParameterName");
		  inputDescription = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputParameterDescription");
		  inputDataFormat = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputParameterDataFormat");
		  inputDefaultValue = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputParameterDefaultValue");
		  inputDataType = entity.getPropertyValue(KBConstants.ONTNS() + "hasInputParameterDataType");
	  }
	  
	  String name = (inputName != null) ? inputName.toString() : "";
	  String description = (inputDescription != null) ? inputDescription.toString() : "";
	  String dataFormat = (inputDataFormat != null) ? inputDataFormat.toString() : "";
	  String defaultValue = (inputDefaultValue != null) ? inputDefaultValue.toString() : "";
	  String dataType = (inputDataType != null) ? inputDataType.toString() : "";
	  
	  html += "<dt>name: " + name + " <span class=\"classifier-delimiter\"></span> (type: <span class=\"classifier\"> " + dataType + "</span>) (format: " + dataFormat + ") (default value: " + defaultValue + ")</dt>\n" + 
	  		"                           <dd>\n" + 
	  		"                              <p class=\"first last\">" + description + "\n" + 
	  		"                              </p>\n" + 
	  		"                           </dd>";
	  
	  return html;
  }
  
  public String getOutputsHTML(List<Entity> entities)
  {
	  String html = "<tr class=\"field-odd field\">\n" + 
	  		"               <th class=\"field-name\">Returns:</th>\n" + 
	  		"               <td class=\"field-body\">\n" + 
	  		"                  <blockquote>\n" + 
	  		"                     <div>\n" + 
	  		"                        <dl class=\"docutils\">";
	  
	  for (Entity entity : entities)
	  {
		  html += getOutputHTML((ComplexEntity) entity);
	  }
	  
	  html += "</dl>\n" + 
	  		"                     </div>\n" + 
	  		"                  </blockquote>\n" + 
	  		"               </td>\n" + 
	  		"            </tr>";
	  
	  return html;
  }
  
  public String getOutputHTML(ComplexEntity entity)
  {
	  String html = "";
	  
	  Entity outputName = entity.getPropertyValue(KBConstants.ONTNS() + "hasOutputName");
	  Entity outputDescription = entity.getPropertyValue(KBConstants.ONTNS() + "hasOutputDescription");
	  Entity outputDataFormat = entity.getPropertyValue(KBConstants.ONTNS() + "hasOutputDataFormat");
	  Entity outputDataType = entity.getPropertyValue(KBConstants.ONTNS() + "hasOutputDataType");
	  
	  String name = (outputName != null) ? outputName.toString() : "";
	  String description = (outputDescription != null) ? outputDescription.toString() : "";
	  String dataFormat = (outputDataFormat != null) ? outputDataFormat.toString() : "";
	  String dataType = (outputDataType != null) ? outputDataType.toString() : "";
	  
	  html += "<dt>name: " + name + " <span class=\"classifier-delimiter\"></span> (type: <span class=\"classifier\"> " + dataType + "</span>) (format: " + dataFormat + ")</dt>\n" + 
	  		"                           <dd>\n" + 
	  		"                              <p class=\"first last\">" + description + "\n" + 
	  		"                              </p>\n" + 
	  		"                           </dd>";
	  
	  return html;
  }
    
  public String getEntityValuesHTML(MetadataProperty prop, List<Entity> entities, boolean simple) {
    MetadataType complexEntity = vocabulary.getType(KBConstants.ONTNS() + "ComplexEntity");
    MetadataType measurement = vocabulary.getType(KBConstants.ONTNS() + "MeasurementEntity");
    MetadataType location = vocabulary.getType(KBConstants.ONTNS() + "Location");
    MetadataType date = vocabulary.getType(KBConstants.ONTNS() + "DateEntity");
    MetadataType rangeEntity = vocabulary.getType(prop.getRange());
    
    String entitieshtml = "";
    if(!simple)
      entitieshtml += "<ul style='padding:20px;padding-top:10px;padding-bottom:10px'>";
    
    // TODO: This part should go into viewing adapters
    boolean isComplex = vocabulary.isA(rangeEntity, complexEntity);
    boolean isMeasurement = vocabulary.isA(rangeEntity, measurement);
    boolean isLocation = vocabulary.isA(rangeEntity, location);
    boolean isDate = vocabulary.isA(rangeEntity, date);

    for(Entity entity : entities) {
      if(!simple) 
        entitieshtml += "<li>";
      if(isComplex && prop.getId() != KBConstants.ONTNS() + "affectsSoftwareFunction") {
        ComplexEntity centity = (ComplexEntity) entity;
        List<MetadataProperty> subprops = new ArrayList<MetadataProperty>();
        for(String subpropid : centity.getValue().keySet())
          subprops.add(vocabulary.getProperty(subpropid));
        subprops = vocabulary.orderProperties(subprops);
        entitieshtml += this.getEntitiesHTML(centity, subprops, true);
      }
      else if (prop.getId() == KBConstants.ONTNS() + "affectsSoftwareFunction") {
        ComplexEntity centity = (ComplexEntity) entity;
        entitieshtml += centity.getLabel();
      }
      else if(isMeasurement) {
        MeasurementEntity me = (MeasurementEntity) entity;
        entitieshtml += me.getValue()+ " " + me.getUnits();
      }
      else if(isLocation) {
        entitieshtml += "<a class='wrap-long-words' href='"+entity.getValue()+"'>"+entity.getValue()+"</a>";
      }
      else if(isDate) {
    	DateTimeFormat fmt = DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT);
	    entitieshtml += fmt.format((Date)entity.getValue());
	  }
      else if(entity.getType() == KBConstants.ONTNS() + "SoftwareVersion") {
	    entitieshtml += "<a class='wrap-long-words' href='#" + NameTokens.version + "/" + entity.getId().split("/")[5] + ":" + entity.getId().split("/")[7] +"'>"+entity.getValue()+"</a>";
	  }
      else {
        entitieshtml += "<span class='wrap-pre wrap-long-words'>" + entity.toString() + "</span>";
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
  
  public String getFunctionEntityValuesHTML(MetadataProperty prop, List<Entity> entities, boolean simple) {
    MetadataType complexEntity = vocabulary.getType(KBConstants.ONTNS() + "ComplexEntity");
    MetadataType measurement = vocabulary.getType(KBConstants.ONTNS() + "MeasurementEntity");
    MetadataType location = vocabulary.getType(KBConstants.ONTNS() + "Location");
    MetadataType rangeEntity = vocabulary.getType(prop.getRange());
    
    String entitieshtml = "";
    if(prop.getId() == KBConstants.ONTNS() + "hasFunction")
      entitieshtml += "<ul style='padding:20px;padding-top:10px;padding-bottom:10px'>";
    
    // TODO: This part should go into viewing adapters
    boolean isComplex = vocabulary.isA(rangeEntity, complexEntity);
    boolean isMeasurement = vocabulary.isA(rangeEntity, measurement);
    boolean isLocation = vocabulary.isA(rangeEntity, location);

    for(Entity entity : entities) { 
      entitieshtml += "<li>";
      if(isComplex) {
        ComplexEntity centity = (ComplexEntity) entity;
        List<MetadataProperty> subprops = new ArrayList<MetadataProperty>();
        for(String subpropid : centity.getValue().keySet())
          subprops.add(vocabulary.getProperty(subpropid));
        subprops = vocabulary.orderProperties(subprops);
        entitieshtml += this.getFunctionHeaderHTML(centity);
        //entitieshtml += this.getFunctionEntitiesHTML(centity, subprops, true);
        List<Entity> inputEntities = centity.getPropertyValues(KBConstants.ONTNS() + "hasInputFile");
        List<Entity> inputPEntities = centity.getPropertyValues(KBConstants.ONTNS() + "hasInputParameter");
        if (inputPEntities != null) {
        	inputEntities.addAll(inputPEntities);
        }
        List<Entity> outputEntities = centity.getPropertyValues(KBConstants.ONTNS() + "hasOutput");
        if (inputEntities != null) {
          entitieshtml += this.getInputsHTML(inputEntities);
        }
        if (outputEntities != null) {
          entitieshtml += this.getOutputsHTML(outputEntities);
        }
        entitieshtml += "</tbody>\n" + 
        		"      </table>\n" + 
        		"      </div>\n" + 
        		"   </dd>\n" + 
        		"</dl>";
      }
      else if(isMeasurement) {
        MeasurementEntity me = (MeasurementEntity) entity;
        entitieshtml += me.getValue() + " " + me.getUnits();
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
    entitieshtml += "</ul>";
    
    return entitieshtml;
  }
  
  public String getPropertyValuesHTML(MetadataProperty prop, List<Entity> entities, boolean simple) {
    
    boolean empty = (entities.size() == 0);
    String entitieshtml = "";
    if (prop.getId() == KBConstants.ONTNS()+"hasFunction") {
    	entitieshtml = getFunctionEntityValuesHTML(prop, entities, simple);
	}
    else {
    	entitieshtml = getEntityValuesHTML(prop, entities, simple);
    }
    String prophtml = getPropertyHTML(prop, simple, empty);
    String propclass = empty ? "hide-this-in-html" : "";
    String html = "<li class='"+propclass+"'>"
        + prophtml
        + entitieshtml
        +"</li>";
    return html;
  } 
  
  public String getEntitiesHTML(ComplexEntity entity, List<MetadataProperty> props, boolean simple) {
    String html = "<ul style='padding:5px;padding-left:2px;list-style-type:none'>";
    
    for(MetadataProperty prop : props) {
    	if (prop != null)
        {
    		html += this.getPropertyValuesHTML(prop, entity.getPropertyValues(prop.getId()), simple);
        }
    }
    html += "</ul>";
    
    return html;
  }

}
