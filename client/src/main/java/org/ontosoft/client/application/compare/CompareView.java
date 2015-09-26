package org.ontosoft.client.application.compare;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.components.browse.EntityBrowser;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class CompareView extends ParameterizedViewImpl 
  implements ComparePresenter.MyView {

  @UiField
  FlexTable table;
  
  @UiField
  PageHeader heading;
  
  @UiField
  Panel matrixpanel;
  
  @UiField
  VerticalPanel loading;
  
  Vocabulary vocabulary;
  List<Software> softwares;
  boolean swloaded;
  
  interface Binder extends UiBinder<Widget, CompareView> {
  }

  @Inject
  public CompareView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    softwares = new ArrayList<Software>();
  }

  @Override
  public void initializeParameters(String[] params) {
    reset();
    if(params.length > 0) {
      String[] swnames = params[0].split("\\s*,\\s*");
      initSoftwares(swnames);
    }
  }
  
  private void reset() {
    swloaded = false;
    softwares.clear();
    table.removeAllRows();
  }
  
  private void initVocabulary() {
    SoftwareREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        vocabulary = vocab;
        if(swloaded)
          showComparisonMatrix();
      }
      @Override
      public void onFailure(Throwable reason) {
        GWT.log("Error fetching Vocabulary", reason);
      }
    }, false);
  }
  
  private void initSoftwares(final String[] swnames) {
    loading.setVisible(true);
    matrixpanel.setVisible(false);
    for(int i=0; i<swnames.length; i++) {
      String swname = swnames[i];
      SoftwareREST.getSoftware(swname, new Callback<Software, Throwable>() {
        @Override
        public void onSuccess(Software sw) {
          softwares.add(sw);
          if(softwares.size() == swnames.length) {
            swloaded = true;
            if(vocabulary != null)
              showComparisonMatrix();
          }
        }
        @Override
        public void onFailure(Throwable exception) {
          GWT.log("Error fetching Software", exception);
        }
      }, false);
    }
  }
  
  private void showComparisonMatrix() {
    if(softwares == null || softwares.size() < 2)
      return;
    
    Collections.sort(softwares, new Comparator<Software>() {
      @Override
      public int compare(Software sw1, Software sw2) {
        return sw1.getLabel().compareToIgnoreCase(sw2.getLabel());
      }
    });
    
    String txt = "";
    int i=0;
    for(Software sw : softwares) {
      if(i > 0) txt += ", ";
      txt += sw.getLabel();
      i++;
    }
    heading.setSubText(txt);
    
    addHeading();
    addPieCharts();
    
    EntityBrowser browser = new EntityBrowser(vocabulary);
    MetadataType type = vocabulary.getType(softwares.get(0).getType());
    List<MetadataProperty> swprops = vocabulary.getPropertiesForType(type);
    
    MetadataCategory topcat  = vocabulary.getCategory(KBConstants.CATNS()+"MetadataCategory");
    topcat = vocabulary.orderChildCategories(topcat);
    for(String lvl1catid : topcat.getChildren()) {
      MetadataCategory lvl1cat = vocabulary.getCategory(lvl1catid);
      lvl1cat = vocabulary.orderChildCategories(lvl1cat);
      for(String lvl2catid : lvl1cat.getChildren()) {
        MetadataCategory lvl2cat = vocabulary.getCategory(lvl2catid);
        List<MetadataProperty> catprops = vocabulary.getPropertiesInCategory(lvl2cat);
        catprops.retainAll(swprops);
        catprops = vocabulary.orderProperties(catprops);
        // First add required properties
        for(MetadataProperty prop : catprops) {
          if(prop.isRequired())
            addRow(prop, browser);
        }
        // Then add not optional properties
        for(MetadataProperty prop : catprops) {
          if(!prop.isRequired())
            addRow(prop, browser);
        }        
      }
    }
    
    loading.setVisible(false);
    matrixpanel.setVisible(true);
  }
  
  private void addHeading() {
    int i=0;
    FlexCellFormatter cellFormatter = table.getFlexCellFormatter();    
    for(Software sw : softwares) {
      cellFormatter.addStyleName(0, i, "header-cell wrap-long-words");
      cellFormatter.setWidth(0, i, 100/softwares.size()+"%");
      table.setWidget(0, i++, new Heading(HeadingSize.H4, sw.getLabel()));
    }
    cellFormatter.addStyleName(0, i-1, "no-border-cell");    
  }
  
  private void addPieCharts() {
    int i=0;
    FlexCellFormatter cellFormatter = table.getFlexCellFormatter();    
    for(Software sw : softwares) {
      CategoryPieChart piechart = new CategoryPieChart(sw.getName(), 200);
      piechart.setVocabulary(vocabulary);
      piechart.setSoftware(sw);
      piechart.setEventEnabled(false);;
      cellFormatter.addStyleName(1, i, "chart-cell");
      cellFormatter.setWidth(1, i, 100/softwares.size()+"%");
      table.setWidget(1, i, piechart);
      if(!piechart.drawnCategories())
        piechart.drawCategories();
      piechart.fillCategories(false); 
      //piechart.setActiveCategoryId(null, false);
      i++;
    }
    cellFormatter.addStyleName(1, i-1, "no-border-cell");    
  }
  
  private void addRow(MetadataProperty prop, EntityBrowser browser) {
    int numRows = table.getRowCount();
    boolean novalue = true;
    for(Software sw : softwares) {
      if(sw.getPropertyValues(prop.getId()).size() > 0) {
        novalue = false;
        break;
      }
    }
    if(novalue)
      return;
      
    FlexCellFormatter cellFormatter = table.getFlexCellFormatter();
    int i=0;
    String prophtml = browser.getPropertyHTML(prop, false, false);
    table.setHTML(numRows, 0, prophtml);
    cellFormatter.addStyleName(numRows, 0, "no-border-cell no-padding-cell");
    cellFormatter.setColSpan(numRows, 0, softwares.size());
    for(Software sw : softwares) {
      if(sw.getPropertyValues(prop.getId()).size() == 0)
        cellFormatter.addStyleName(numRows+1, i, "empty-cell");
      String html = browser.getEntityValuesHTML(prop, sw.getPropertyValues(prop.getId()), true);
      table.setHTML(numRows+1, i++, html);
    }
    cellFormatter.addStyleName(numRows+1, i-1, "no-border-cell");
  }
  
}
