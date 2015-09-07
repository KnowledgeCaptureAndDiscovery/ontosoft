package org.ontosoft.client.components.chart;

import java.util.List;

import org.ontosoft.client.Config;
import org.ontosoft.client.components.chart.events.CategorySelectionEvent;
import org.ontosoft.client.components.chart.events.CategorySelectionHandler;
import org.ontosoft.client.components.chart.events.HasCategoryChartHandlers;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.Software;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.github.gwtd3.api.core.Selection;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.SimplePanel;

public class CategoryChartBase extends SimplePanel
    implements HasCategoryChartHandlers {
  private HandlerManager handlerManager;

  Selection svg;
  Selection chart;

  Vocabulary vocabulary;
  Software software;
  String activeCategoryId;
  boolean drawncategories = false;
  
  double width, height;
  
  // Graph Look & Feel
  String font = "Roboto Condensed";
  double strokewidth = 1;
  int fontsizecutoff = 6;
  double padding = 1;
  double cornerRadius = 0;
  final int induration = 200;
  final int outduration = 100;
  
  String bgcolor = "#F5F5F5";
  String textcolor = "#555555";
  String strokecolor = "#EEEEEE";
  
  String donecolor = Config.getOKColor();
  String notdonecolor = Config.getErrorColor();
  String midcolor = "#EEA6B3"; // #EA90A0
  
  String optcolor = "#CCCCCC";
  String midoptcolor = "#BBBBBB";
  
  boolean eventEnabled = true;
  
  public CategoryChartBase() {
    handlerManager = new HandlerManager(this);
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  public HandlerRegistration addCategorySelectionHandler(
      CategorySelectionHandler handler) {
    return handlerManager.addHandler(CategorySelectionEvent.TYPE, handler);
  }
  
  public void setVocabulary(Vocabulary data) {
    this.vocabulary = data;
  }
  
  public Vocabulary getVocabulary() {
    return this.vocabulary;
  }
  
  public void setSoftware(Software software) {
    this.software = software;
  }
  
  public Software getSoftware() {
    return this.software;
  }
  
  public Selection getSVG() {
    return this.svg;
  }
  
  public void setActiveCategoryId(String activeCategoryId, boolean fireEvent) {
    this.activeCategoryId = activeCategoryId;
    if(activeCategoryId != null && fireEvent)
      this.fireEvent(new CategorySelectionEvent(activeCategoryId));
  }
  
  public String getActiveCategoryId() {
    return activeCategoryId;
  }
  
  public boolean isEventEnabled() {
    return eventEnabled;
  }

  public void setEventEnabled(boolean eventEnabled) {
    this.eventEnabled = eventEnabled;
  }

  public void drawCategories() { 
    this.drawCategories(KBConstants.CATNS()+"MetadataCategory");
  }
  
  public void drawCategories(String categoryId) { }

  public void fillCategories() { 
    this.fillCategories(false, false);
  }

  public void fillCategories(boolean animate) { 
    this.fillCategories(animate, false);
  }

  public void fillCategories(boolean animate, boolean quickanimation) { }
  
  public boolean drawnCategories() { return this.drawncategories; }

  protected void changeTextColor(Selection group, String color, boolean animate) {
    if(animate) {
      group.selectAll("text.maintext").transition().duration(induration).style("fill", color);
    }
    else {
      group.selectAll("text.maintext").style("fill", color);
    }
  }
  
  protected void fadeOut(Selection item, boolean animate) {
    if(animate) {
      item.transition().duration(outduration)
        .style("fill-opacity", 0.3);
    }
    else {
      item.style("fill-opacity", 0.3);
    }
  }
  
  protected void fadeIn(Selection item, boolean animate) {
    if(animate) {
      item.transition().duration(induration/2)
        .style("fill-opacity", 1);       
    }
    else {
      item.style("fill-opacity", 1);
    }
  }
  
  public double getDonePercentage(String categoryId) {
    return getDonePercentage(categoryId, false);
  }
  
  public double getDonePercentage(String categoryId, boolean optional) {
    if(this.software == null || this.vocabulary == null)
      return 0;
    
    MetadataType mtype = this.vocabulary.getType(software.getType());
    MetadataCategory mcat = this.vocabulary.getCategory(categoryId);
    if(mtype == null || mcat == null)
      return 0;

    List<MetadataProperty> props = this.vocabulary.getPropertiesForType(mtype);
    List<MetadataProperty> catprops = this.vocabulary.getPropertiesInCategory(mcat);
    props.retainAll(catprops);
    
    int total = 0;
    int filled = 0;
    for(MetadataProperty typeprop : props) {
      if(typeprop.getUiConfig() != null && typeprop.getUiConfig().isUneditable())
        continue;
      if((!optional && typeprop.isRequired()) || (optional && !typeprop.isRequired())) {
        total++;
        if(catprops.contains(typeprop)) {
          List<Entity> list = this.software.getPropertyValues().get(typeprop.getId());
          if(list != null && list.size() > 0)
            filled++;
        }
      }
    }
    if(total == 0)
      return 100;
    
    return 100.0*filled/total;
  }
  
  protected Selection setGradient(Selection grad, double percentage) {
    double gradoffset = percentage < 80 ? 15 : (percentage < 95 ? 10 : 
      (percentage < 100 ? 5 : 0));
    if(percentage > 0) {
      grad.append("stop").attr("offset", (percentage-gradoffset) + "%")
      .style("stop-color", donecolor);
      grad.append("stop").attr("offset", percentage + "%")
      .style("stop-color", midcolor);
    }
    grad.append("stop").attr("offset", (percentage+gradoffset) + "%")
      .style("stop-color", notdonecolor);  
    return grad;
  }
  
  protected Selection setOptGradient(Selection grad, double percentage, double start) {
    double gradoffset = percentage < 80 ? 15 : (percentage < 95 ? 10 : 
      (percentage < 100 ? 5 : 0));
    //gradoffset = 8;
    double diff = 1-start/100;
    if(percentage > 0) {
      grad.append("stop").attr("offset", start + diff*(percentage-gradoffset) + "%")
      .style("stop-color", donecolor);
      grad.append("stop").attr("offset", start + diff*percentage + "%")
        .style("stop-color", midoptcolor);
    }
    grad.append("stop").attr("offset", start + diff*(percentage+gradoffset) + "%")
      .style("stop-color", optcolor);
    return grad;
  }
  
  protected String addGlowFilter(Selection defs) {
    Selection glow = defs.append("filter").attr("id", "glow").attr("x", "-5%").attr("y", "-5%")
        .attr("width", "110%").attr("height", "110%");
    glow.append("feGaussianBlur").attr("stdDeviation", "1 1").attr("result", "glow");
    Selection merge = glow.append("feMerge");
    merge.append("feMergeNode").attr("in", "glow");
    merge.append("feMergeNode").attr("in", "glow");
    merge.append("feMergeNode").attr("in", "glow");
    return "url(#glow)";
  }
  
  protected String addShadowFilter(Selection defs) {
    Selection shadow = defs.append("filter").attr("id", "shadow").attr("x", "-3%").attr("y", "-3%")
        .attr("width", "106%").attr("height", "106%");    
    shadow.append("feGaussianBlur").attr("stdDeviation", "1 1").attr("result", "shadow");
    shadow.append("feOffset").attr("dx", "0.5").attr("dy", "0.5");
    return "url(#shadow)";
  }
  
  protected String addInnerShadowFilter(Selection defs) {
    Selection ishadow = defs.append("filter").attr("id", "innerShadow").attr("x", "-10%").attr("y", "-10%")
        .attr("width", "120%").attr("height", "120%");    
    ishadow.append("feGaussianBlur").attr("in", "SourceGraphic").attr("stdDeviation", "3").attr("result", "blur");
    ishadow.append("feOffset").attr("in", "blur").attr("dx", "0.5").attr("dy", "0.5");
    return "url(#innerShadow)";
  }
  
  protected String addGreyscaleFilter(Selection defs) {
    Selection greyscale = defs.append("filter").attr("id", "greyscale");
    greyscale.append("feColorMatrix").attr("type", "matrix")
        .attr("values", "0.3333 0.3333 0.3333 0 0\n0.3333 0.3333 0.3333 0 0"
            + "\n0.3333 0.3333 0.3333 0 0\n0 0 0 1 0");
    return "url(#greyscale)";
  }
  
  public void onLoad() {
    super.onLoad();
    this.updateDimensions();
  }
  
  // To fix bugs with older iOS safari and IE
  // Since percentage widths and heights don't work in some browsers
  public void updateDimensions() { }
  
  
  public static native String getUserAgent() /*-{
    return navigator.userAgent.toLowerCase();
  }-*/;
}
