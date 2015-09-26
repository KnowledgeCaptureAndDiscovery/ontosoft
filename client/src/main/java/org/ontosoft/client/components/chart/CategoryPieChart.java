package org.ontosoft.client.components.chart;

import java.util.HashMap;

import org.ontosoft.shared.classes.vocabulary.MetadataCategory;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.Interpolators;
import com.github.gwtd3.api.core.Selection;
import com.github.gwtd3.api.core.Transition;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.github.gwtd3.api.interpolators.CallableInterpolator;
import com.github.gwtd3.api.interpolators.Interpolator;
import com.github.gwtd3.api.svg.Arc;
import com.github.gwtd3.api.tweens.TweenFunction;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.user.client.ui.SimplePanel;

public class CategoryPieChart extends CategoryChartBase {
  Selection defs;
  double radius, innerradius;
  double optradius, optinnerradius;
  double offset;

  HashMap<String, SliceDetails> categorySlices;
  class SliceDetails {
    Selection categorySlice;
    Selection backgroundSlice;
    Selection fillSlice;
    Selection optFillSlice;
    Arc sliceArc;
    Arc optSliceArc;
    double dx, dy;
    
    public SliceDetails(Selection categorySlice, Selection backgroundSlice, 
        Selection fillSlice, Selection optFillSlice, 
        Arc sliceArc, Arc optSliceArc,         
        double dx, double dy) {
      this.categorySlice = categorySlice;
      this.backgroundSlice = backgroundSlice;
      this.fillSlice = fillSlice;
      this.sliceArc = sliceArc;
      this.optFillSlice = optFillSlice;
      this.optSliceArc = optSliceArc;      
      this.dx = dx;
      this.dy = dy;
    }
  }
  
  @UiConstructor
  public CategoryPieChart(String name, double size) {
    super(name);
    this.width = size;
    categorySlices = new HashMap<String, SliceDetails>();
  }
  
  private void initDrawingSurface(double width) {
    this.width = width;
    this.height = width;
    if(svg == null)
      svg = D3.select(this)
          .append("svg")
          .attr("width", "100%")
          .attr("height", "100%")
          .attr("preserveAspectRatio", "xMinYMin");
    
    svg.attr("viewBox", "0 0 "+this.width + " "+this.height);
    //svg.attr("width", width);
    //svg.attr("height", height);
    
    if(this.chart != null)
      this.chart.remove();
    
    this.chart = svg.append("g")
        .attr("transform",
            "translate(" + (width / 2) + "," + (height / 2) + ")");
  }
  
  @Override
  public void drawCategories(String categoryid) {
    if(this.vocabulary == null)
      return;
    
    initDrawingSurface(this.width);
    final CategoryChartBase me = this;
    final Selection level = chart;

    MetadataCategory topcat = this.vocabulary.getCategory(categoryid);
    categorySlices.clear();
    
    int num = topcat.getChildren().size();
    this.offset = width*0.025;
    double dwidth = width*0.06; // donut width
    optradius = width/2.0 - padding - offset - dwidth;
    optinnerradius = optradius - dwidth;
    
    final double PI = Math.PI;
    double fontsize = 2*PI*optinnerradius/(6.8*num);
    
    this.radius = optinnerradius - strokewidth/4; //width/2.0 - padding - fontsize*1.5 - offset;
    this.innerradius = 0;

    this.defs = level.append("defs");
    
    int i = 0;
    double startangle = -PI/num;
    double angle = 2*PI/num;
    
    topcat = vocabulary.orderChildCategories(topcat);
    for(final String catid : topcat.getChildren()) {
      MetadataCategory category = this.vocabulary.getCategory(catid);
      
      double endangle = startangle + angle;
      
      double txtangle = startangle + angle/2 - PI/2;
      double dx = Math.cos(txtangle);
      double dy = Math.sin(txtangle);
      
      double sa = startangle, ea = endangle, fdy = -fontsize/2;
      if(dy > 0) {
        sa = endangle;
        ea = startangle;
        fdy = fontsize;
      }
      
      final Arc arc = D3.svg().arc()
          .startAngle(sa).endAngle(ea)
          .innerRadius(innerradius)
          .setOrInvokeSetter("cornerRadius", cornerRadius);
            
      final Arc optarc = D3.svg().arc()
          .startAngle(sa).endAngle(ea).
          innerRadius(optinnerradius)
          .setOrInvokeSetter("cornerRadius", cornerRadius);
      
      String slicetransform = "translate(0,0)";
      if(catid.equals(activeCategoryId))
        slicetransform = "translate("+dx*offset+","+dy*offset+")";

      final Selection slice = level.append("g")
          .attr("transform", slicetransform)
          .attr("class", "slicegroup");
      
      String sliceid = "slice_"+this.getName()+i;
      final Arc bgslicejson = Arc.constantArc().outerRadius(radius);
      final Selection bgslice = 
          slice.append("path")
          .datum(bgslicejson)
          .style("fill", bgcolor)
          .attr("d", arc)          
          .attr("id", sliceid);
      
      if(this.isEventEnabled())
        bgslice.style("cursor", "pointer");

      final Arc optdoneslicejson = Arc.constantArc().outerRadius(optinnerradius);
      final Selection optdoneslice = 
          slice.append("path")
          .datum(optdoneslicejson)
          .style("fill", "none")
          .style("stroke", strokecolor)
          .attr("d", optarc)
          .attr("pointer-events", "none");  
      
      
      final Arc doneslicejson = Arc.constantArc().outerRadius(innerradius);
      final Selection doneslice = 
          slice.append("path")
          .datum(doneslicejson)
          .style("fill", "none")
          .attr("d", arc)
          .attr("pointer-events", "none");    
            
      categorySlices.put(catid, new SliceDetails(slice, bgslice,
          doneslice, optdoneslice,
          arc, optarc, 
          dx, dy));
      
      // Outline
      slice.append("path")
          .datum(bgslicejson)
          .style("fill", "none")
          .attr("stroke", strokecolor)
          .attr("stroke-width", strokewidth)
          .attr("pointer-events", "none")          
          .attr("d", arc);
      
      if(fontsize > fontsizecutoff) {
        double txtoffset = 100.0*(PI/num)/(2*PI/num + 2.0);

        Selection slicetxt = slice.append("text")
          .attr("dy", fdy)
          .attr("pointer-events", "none")
          .style("font-size", fontsize+"px")
          .style("font-weight", "bold")
          .style("font-family", font)
          .attr("class", "maintext")
          .style("fill", textcolor)
          .attr("text-anchor", "middle");

        slicetxt.append("textPath")
          .attr("startOffset", txtoffset+"%")
          .attr("xlink:href", "#"+sliceid)
          .text(category.getLabel());
      }
      
      if(catid.equals(this.activeCategoryId)) {
        fadeIn(doneslice, false);
        fadeIn(optdoneslice, false);
        moveSliceOut(catid, false);
      }

      if(this.isEventEnabled()) {
        bgslice.on(BrowserEvents.MOUSEOVER, new DatumFunction<Void>() {
          @Override
          public Void apply(Element context, Value d, int index) {
            if(!catid.equals(activeCategoryId)) {
              if(activeCategoryId != null) {
                fadeIn(doneslice, true);
                fadeIn(optdoneslice, false);
              }
              moveSliceOut(catid, true);
            }
            return null;
          }
        });
        bgslice.on(BrowserEvents.MOUSEOUT, new DatumFunction<Void>() {
          @Override
          public Void apply(Element context, Value d, int index) {
            if(!catid.equals(activeCategoryId)) {
              if(activeCategoryId != null) {
                fadeOut(doneslice, true);
                fadeOut(optdoneslice, false);
              }
              moveSliceIn(catid, true);
            }
            return null;
          }
        });
  
        bgslice.on(BrowserEvents.CLICK, new DatumFunction<Void>() {
          @Override
          public Void apply(Element context, Value d, int index) {
            me.setActiveCategoryId(catid, true);
            return null;
          }
        });
      }
      
      // If events disabled and no active category, show all bars full
      if(!this.isEventEnabled() && activeCategoryId == null) {
        optdoneslice.datum(Arc.constantArc().outerRadius(optradius));
        slice.selectAll("text")
          .attr("transform", "translate("+dx*dwidth+","+dy*dwidth+")");  
      }

      startangle = endangle;
      i++;
    }
    this.drawncategories = true;
  }
  
  private void moveSliceIn(String categoryid, boolean animate) {
    SliceDetails sd = this.categorySlices.get(categoryid);
    
    if(animate) {
      sd.categorySlice.transition().duration(outduration)
        .attr("transform", "translate(0,0)");
      radiusTransition(sd.optSliceArc, 
          sd.optFillSlice.transition().duration(outduration), optinnerradius);
      sd.categorySlice.selectAll("text").transition().duration(outduration)
        .attr("transform", "translate(0,0)");
    }
    else {
      sd.categorySlice.attr("transform", "translate(0,0)");
      sd.optFillSlice.datum(Arc.constantArc().outerRadius(optinnerradius));
      sd.categorySlice.selectAll("text").attr("transform", "translate(0,0)");      
    }  
  }
  
  private void moveSliceOut(String categoryid, boolean animate) {
    SliceDetails sd = this.categorySlices.get(categoryid);
    double dwidth = optradius - optinnerradius;
    
    if(animate) {
      sd.categorySlice.transition().duration(induration)
        .attr("transform", "translate("+sd.dx*offset+","+sd.dy*offset+")");
      radiusTransition(sd.optSliceArc, 
          sd.optFillSlice.transition().duration(induration), optradius);
      sd.categorySlice.selectAll("text").transition().duration(induration)
        .attr("transform", "translate("+sd.dx*dwidth+","+sd.dy*dwidth+")");
    }
    else {
      sd.categorySlice.attr("transform", "translate("+sd.dx*offset+","+sd.dy*offset+")");
      sd.optFillSlice.datum(Arc.constantArc().outerRadius(optradius));
      sd.categorySlice.selectAll("text")
        .attr("transform", "translate("+sd.dx*dwidth+","+sd.dy*dwidth+")");      
    }
  }
  
  @Override
  public void fillCategories(boolean animate, boolean quickanimation) {
    if(this.software == null)
      return;
    
    this.defs.remove();
    this.defs = this.chart.append("defs");

    double optstart = optinnerradius*100/optradius;
    
    int i=0;
    for(String catId : this.categorySlices.keySet()) {
      SliceDetails sd = this.categorySlices.get(catId);
      
      // Convert percentage to area-based percentage for a circular chart
      double percentage = 100*Math.sqrt(this.getDonePercentage(catId)/100);
      double optpercentage = 100*Math.sqrt(this.getDonePercentage(catId, true)/100);
      
      //GWT.log(catId+" :"+percentage);
      String sfx = this.getName() + "_" + i;
      Selection grad = this.defs.append("radialGradient").attr("id", "innerGrad" + sfx)
            .attr("gradientUnits", "userSpaceOnUse")
            .attr("cx", 0).attr("cy", 0).attr("r", radius);
      this.setGradient(grad, percentage);
      
      Selection optgrad = this.defs.append("radialGradient").attr("id", "optGrad" + sfx)
          .attr("gradientUnits", "userSpaceOnUse")
          .attr("cx", 0).attr("cy", 0).attr("r", optradius);      
      this.setOptGradient(optgrad, optpercentage, optstart);
      
      //if(animate) {
        int duration = induration*2;
        if(quickanimation) 
          duration = induration;
        if(!animate)
          duration = 0;
        final Arc doneslicejson = Arc.constantArc().outerRadius(innerradius);
        final Arc optdoneslicejson = Arc.constantArc().outerRadius(optinnerradius);
        sd.fillSlice.datum(doneslicejson).style("fill", "url(#innerGrad" + sfx +")");        
        sd.optFillSlice.datum(optdoneslicejson).style("fill", "url(#optGrad" + sfx +")");

        Transition transition = sd.fillSlice.transition().duration(duration);
        radiusTransition(sd.sliceArc, transition, radius);
        
        Transition opttransition = sd.optFillSlice.transition().duration(duration);
        radiusTransition(sd.optSliceArc, opttransition, optradius);
      /*}
      else {
        final Arc doneslicejson = Arc.constantArc().outerRadius(radius);
        final Arc optdoneslicejson = Arc.constantArc().outerRadius(optradius);
        sd.fillSlice.datum(doneslicejson).style("fill", "url(#innerGrad" + sfx +")");
        sd.optFillSlice.datum(optdoneslicejson).style("fill", "url(#optGrad" + sfx +")");
      }*/
      i++;
    }
  }
  
  @Override
  public void setActiveCategoryId(String activeCategoryId, boolean fireEvent) {
    // Reset everyone to their original states
    for(String catid : this.categorySlices.keySet()) {
      SliceDetails sd = this.categorySlices.get(catid);
      if(catid.equals(activeCategoryId)) {
        // The category that is going to be newly selected
        // This should be moved out and made opaque
        if(!activeCategoryId.equals(this.activeCategoryId)) {
          // Only do this if the category isn't already selected
          fadeIn(sd.fillSlice, false);
          fadeIn(sd.optFillSlice, false);
          moveSliceOut(catid, true);
        }
      }
      else {
        // If there is no slice going to be selected, then make all opaque
        if(activeCategoryId == null) {
          fadeIn(sd.fillSlice, false);
          fadeIn(sd.optFillSlice, false);
        }
        else {
          fadeOut(sd.fillSlice, false);
          fadeOut(sd.optFillSlice, false);
        }
        // The rest of the categories should have their slices moved in
        moveSliceIn(catid, true);
      }
    }
    super.setActiveCategoryId(activeCategoryId, fireEvent);
  }
  
  protected void radiusTransition(final Arc arc,
      final Transition transition,
      final double newRadius) {

    transition.attrTween("d", new TweenFunction<String>() {
      @Override
      public Interpolator<String> apply(final Element context,
          final Value datum, final int index,
          final Value currentAttributeValue) {
        try {
          final Arc arcDatum = datum.as();
          final double outerRadius = arcDatum.outerRadius();
          return new CallableInterpolator<String>() {
            private final Interpolator<Double> interpolator = Interpolators
                .interpolateNumber(outerRadius, newRadius);

            @Override
            public String interpolate(final double t) {
              double interpolated = interpolator.interpolate(t);
              arcDatum.outerRadius(interpolated);
              return arc.generate(arcDatum);
            }
          };
        } catch (Exception e) {
          throw new IllegalStateException("Error during transition",e);
        }
      }
    });
  }
  
  protected void angleTransition(final Arc arc,
      final Transition transition,
      final double newAngle) {

    transition.attrTween("d", new TweenFunction<String>() {
      @Override
      public Interpolator<String> apply(final Element context,
          final Value datum, final int index,
          final Value currentAttributeValue) {
        try {
          final Arc arcDatum = datum.as();
          final double endAngle = arcDatum.endAngle();
          return new CallableInterpolator<String>() {
            private final Interpolator<Double> interpolator = Interpolators
                .interpolateNumber(endAngle, newAngle);

            @Override
            public String interpolate(final double t) {
              double interpolated = interpolator.interpolate(t);
              arcDatum.endAngle(interpolated);
              return arc.generate(arcDatum);
            }
          };
        } catch (Exception e) {
          throw new IllegalStateException("Error during transition",e);
        }
      }
    });
  }
  
  public void updateDimensions() {
    String useragent = getUserAgent();
    // Fix some IE/iOS 7.1 bugs
    if(useragent.contains("msie") ||
        useragent.matches(".*(ipad|iphone);.*cpu.*os 7_\\d.*")) {
      final SimplePanel panel = this;
      Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
        @Override
        public void execute() {
          int width = panel.getOffsetWidth();
          //GWT.log(width+"px");
          if(width > 0){
            panel.getElement().getStyle().setHeight(width, Unit.PX);
          }
        } 
      });
    }
  }

}