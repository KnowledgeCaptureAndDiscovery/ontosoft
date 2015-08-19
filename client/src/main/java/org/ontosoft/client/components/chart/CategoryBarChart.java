package org.ontosoft.client.components.chart;

import java.util.HashMap;

import org.ontosoft.shared.classes.vocabulary.MetadataCategory;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiConstructor;

public class CategoryBarChart extends CategoryChartBase {
  Selection defs;
  double categoryWidth;
  double categoryHeight;
  double categoryBarHeight;
  double optBarWidth, mainBarWidth;
  
  HashMap<String, BarDetails> categoryBars;
  class BarDetails {
    Selection groupBar;
    Selection backgroundBar;
    Selection fillBar;
    Selection fillOptBar;
    public BarDetails(Selection categoryBar, Selection backgroundBar, 
        Selection fillBar, Selection fillOptBar) {
      this.groupBar = categoryBar;
      this.backgroundBar = backgroundBar;
      this.fillBar = fillBar;
      this.fillOptBar = fillOptBar;
    }
  }
  
  @UiConstructor
  public CategoryBarChart(double width, double height) {
    super();
    this.categoryBars = new HashMap<String, BarDetails>();
    this.width = width;
    this.height = height;
  }
  
  private void initDrawingSurface(double width, double height, double barheight) {
    this.height = barheight;
    
    if(svg == null)
      svg = D3.select(this)
          .append("svg")
          .attr("width", "100%")
          .attr("height", "100%")          
          .attr("preserveAspectRatio", "xMinYMin");
    
    svg.attr("viewBox", "0 0 "+this.width + " "+this.height);
    
    if(this.chart != null)
      this.chart.remove();
    
    this.chart = svg.append("g");
  }

  @Override
  public void drawCategories(String categoryid) {
    if(this.vocabulary == null)
      return;
    
    this.categoryBars.clear();
    this.categoryWidth = this.width - padding*2;
    
    MetadataCategory topcat = this.vocabulary.getCategory(categoryid);
    
    int num = topcat.getChildren().size();
    //double size = this.width;
    
    final double cath = 9; //size/20 < 20 ? size/20 : 20;
    final double catdy = 3; //size/20 < 20 ? size/20 : 20;
    double startx = padding;
    double fontsize = 11;
    double caty = 0;

    categoryHeight = 2*cath + fontsize*2.7; 
    categoryBarHeight = cath;
    
    double totalcath = categoryHeight + catdy;
    double barheight = totalcath * num - catdy;
    double optgap = strokewidth/2;
    
    optBarWidth = categoryWidth*0.05;
    mainBarWidth = categoryWidth - optBarWidth - optgap;
    double optbarx = startx + (categoryWidth - optBarWidth);
    
    if(this.chart != null)
      this.chart.remove();

    initDrawingSurface(width, height, barheight);
    
    final CategoryBarChart me = this;
    final Selection level = chart;
    
    defs = level.append("defs");
    
    //String glowfilter = this.addGlowFilter(defs);
    
    topcat = vocabulary.orderChildCategories(topcat);
    for(final String catid : topcat.getChildren()) {
      MetadataCategory category = this.vocabulary.getCategory(catid);
      String label = category.getLabel();
      String sublabel = category.getSublabel();
      
      double catlabely = caty + fontsize + cath;      
      double catsublabely = catlabely + fontsize*1.3;
      double catbary = catsublabely + fontsize*0.4;

      final Selection bar = level.append("g")
          .attr("id", catid);
      
      final Selection bgbar = bar.append("rect")
          .attr("rx", cornerRadius)
          .attr("ry", cornerRadius)
          .attr("x", startx)
          .attr("y", caty)
          .attr("width", mainBarWidth)
          .attr("height", catbary - caty + cath)
          .style("cursor", "pointer")
          .attr("fill", bgcolor);

      final Selection donebar = bar.append("rect")
        .attr("pointer-events", "none")
        .attr("rx", cornerRadius)
        .attr("ry", cornerRadius)
        .attr("x", startx)
        .attr("y", catbary)
        .attr("width", 0)
        .attr("stroke-width", 1)
        .attr("stroke", bgcolor)
        .attr("height", cath)
        .attr("fill", "none");
      
      final Selection doneoptbar = bar.append("rect")
          .attr("pointer-events", "none")
          .attr("rx", cornerRadius)
          .attr("ry", cornerRadius)
          .attr("x", optbarx)
          .attr("y", catbary)
          .attr("opacity", 0)
          .attr("width", 0)
          .attr("stroke-width", 1)
          .attr("stroke", "#f5f5f5")
          .attr("height", cath)
          .attr("fill", "done");      
      
      this.categoryBars.put(catid, new BarDetails(bar, bgbar, donebar, doneoptbar));
      
      bar.append("text")
        .attr("pointer-events", "none")
        .style("font-size", 1.5*fontsize+"px")
        .style("font-family", font)
        .style("font-weight", "bold")
        .style("fill", textcolor)
        .attr("class", "maintext")        
        .attr("x", startx + 4)
        .attr("y", catlabely)
        .text(label);   

      bar.append("text")
        .attr("pointer-events", "none")
        .style("font-size", fontsize+"px")
        .style("font-family", font)
        .style("fill", textcolor)
        .attr("class", "maintext")
        .attr("x", startx + 4)
        .attr("y", catsublabely)
        .text(sublabel);
    
      if(catid.equals(this.activeCategoryId)) {
        fadeIn(donebar, false);
        fadeIn(doneoptbar, false);
        doneoptbar.attr("opacity", 1);
        makeBarBigger(donebar, false);
        makeBarBigger(doneoptbar, false);
        changeTextColor(bar, strokecolor, false);
      }
    
      bgbar.on(BrowserEvents.MOUSEOVER, new DatumFunction<Void>() {
        @Override
        public Void apply(Element context, Value d, int index) {
          if(!catid.equals(activeCategoryId)) {
            doneoptbar.attr("opacity", 1);
            if(activeCategoryId != null) {
              fadeIn(donebar, false);
              fadeIn(doneoptbar, false);
            }
            makeBarBigger(donebar, true);
            makeBarBigger(doneoptbar, true);
            changeTextColor(bar, strokecolor, true);
          }
          return null;
        }
      });
      bgbar.on(BrowserEvents.MOUSEOUT, new DatumFunction<Void>() {
        @Override
        public Void apply(Element context, Value d, int index) {
          if(!catid.equals(activeCategoryId)) {
            doneoptbar.attr("opacity", 0);
            if(activeCategoryId != null) {
              fadeOut(donebar, false);
              fadeOut(doneoptbar, false);
            }
            makeBarSmaller(donebar, true);
            makeBarSmaller(doneoptbar, true);
            changeTextColor(bar, textcolor, true);
          }
          return null;
        }
      });

      bgbar.on(BrowserEvents.CLICK, new DatumFunction<Void>() {
        @Override
        public Void apply(Element context, Value d, int index) {
          me.setActiveCategoryId(catid, true);
          return null;
        }
      });

      caty += categoryHeight + catdy;
    }
  }
  
  private void makeBarBigger(Selection bar, boolean animate) {
    if(animate) {
      bar.transition().duration(induration)
        .attr("height", categoryHeight)
        .attr("transform", 
            "translate(0," + (categoryBarHeight - categoryHeight) + ")");
    }
    else {
      bar.attr("height", categoryHeight)
        .attr("transform", 
          "translate(0," + (categoryBarHeight - categoryHeight) + ")");
    }
  }
  
  private void makeBarSmaller (Selection bar, boolean animate) {
    if(animate) {
      bar.transition().duration(outduration)
        .attr("height", categoryBarHeight)
        .attr("transform", "translate(0,0)");
    }
    else {
      bar.attr("height", categoryBarHeight)
        .attr("transform", "translate(0,0)");
    }
  }
  
  @Override
  public void fillCategories(boolean animate, boolean quickanimation) {
    int i=0;
    this.defs.remove();
    this.defs = this.chart.append("defs");
    //this.addGlowFilter(defs);
    
    for(String catId : this.categoryBars.keySet()) {
      BarDetails bd = this.categoryBars.get(catId);

      double percentage = this.getDonePercentage(catId);
      double optpercentage = this.getDonePercentage(catId, true);
      
      Selection grad = defs.append("linearGradient").attr("id", "barGrad" + i);
      this.setGradient(grad, percentage);

      Selection optgrad = defs.append("linearGradient").attr("id", "optbarGrad" + i);
      this.setOptGradient(optgrad, optpercentage, 0);
      
      bd.fillBar.attr("fill", "url(#barGrad"+i+")");
      bd.fillOptBar.attr("fill", "url(#optbarGrad"+i+")");
      if(animate) {
        int duration = induration*2;
        if(quickanimation) 
          duration = induration;
        bd.fillBar.attr("width", 0);        
        bd.fillBar.transition().duration(duration)
          .attr("width", mainBarWidth);
        
        bd.fillOptBar.attr("width", 0);  
        bd.fillOptBar.transition().duration(duration)
          .attr("width", optBarWidth);
      }
      else {
        bd.fillBar.attr("width", mainBarWidth);
        bd.fillOptBar.attr("width", optBarWidth);
      }
      
      i++;
    }
  }
  
  @Override
  public void setActiveCategoryId(String activeCategoryId, boolean fireEvent) {
    // Reset everyone to their original states
    for(String catid : this.categoryBars.keySet()) {
      BarDetails bd = this.categoryBars.get(catid);
      if(catid.equals(activeCategoryId)) {
        // The category that is going to be newly selected
        // This should be made bigger and made opaque
        if(!activeCategoryId.equals(this.activeCategoryId)) {
          // Only do this if the category isn't already selected
          bd.fillOptBar.attr("opacity", 1);
          fadeIn(bd.fillBar, false);
          fadeIn(bd.fillOptBar, false);
          makeBarBigger(bd.fillBar, true);
          makeBarBigger(bd.fillOptBar, true);
          changeTextColor(bd.groupBar, strokecolor, true);
        }
      }
      else {
        // If there is no bar going to be selected, then make all opaque
        if(activeCategoryId == null) {
          bd.fillOptBar.attr("opacity", 1);
          fadeIn(bd.fillBar, false);
          fadeIn(bd.fillOptBar, false);
        }
        else {
          bd.fillOptBar.attr("opacity", 0);
          fadeOut(bd.fillBar, false);
          fadeOut(bd.fillOptBar, false);
        }
        // The rest of the categories should have their bars made smaller
        makeBarSmaller(bd.fillBar, true);
        makeBarSmaller(bd.fillOptBar, true);
        changeTextColor(bd.groupBar, textcolor, true);
      }
    }    
    super.setActiveCategoryId(activeCategoryId, fireEvent);
  }  
  
  // Fix an iOS7 Bug
  public void updateDimensions() { 
    String useragent = getUserAgent();
    if(useragent.matches(".*(ipad|iphone);.*cpu.*os 7_\\d.*")) {
      //GWT.log(useragent);
      final CategoryChartBase panel = this;
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          int width = panel.getOffsetWidth();
          if(width > 0) {
            //double height = panel.height * width / panel.width;
            //GWT.log(width+", "+ height);
            panel.getElement().getStyle().setHeight(panel.height, Unit.PX);          
          }
        }
      });
    }
  }
}