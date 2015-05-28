package org.earthcube.geosoft.client.application.publish;

import org.earthcube.geosoft.client.application.ParameterizedViewImpl;
import org.earthcube.geosoft.client.components.chart.CategoryBarChart;
import org.earthcube.geosoft.client.components.chart.CategoryPieChart;
import org.earthcube.geosoft.client.components.chart.events.CategorySelectionEvent;
import org.earthcube.geosoft.client.components.form.SoftwareForm;
import org.earthcube.geosoft.client.components.form.events.PluginResponseEvent;
import org.earthcube.geosoft.client.components.form.events.SoftwareChangeEvent;
import org.earthcube.geosoft.client.components.form.notification.PluginNotifications;
import org.earthcube.geosoft.client.place.NameTokens;
import org.earthcube.geosoft.client.rest.SoftwareREST;
import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataCategory;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.earthcube.geosoft.shared.plugins.PluginResponse;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Heading;

import com.github.gwtd3.api.D3;
import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PublishView extends ParameterizedViewImpl 
  implements PublishPresenter.MyView {

  @UiField
  CategoryPieChart piechart;
  
  @UiField
  CategoryBarChart barchart;
  
  @UiField
  Column sidecolumn, piecolumn, barcolumn;
  
  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  SoftwareForm softwareform;
  
  @UiField
  ButtonGroup buttons;
  
  @UiField
  Button savebutton, reloadbutton;
  
  @UiField
  VerticalPanel loading;
  
  @UiField
  Heading heading;
  
  @UiField
  PluginNotifications notifications;
  
  Vocabulary vocabulary;
  String softwarename;
  Software software;

  
  interface Binder extends UiBinder<Widget, PublishView> { }

  @Inject
  public PublishView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
  }
  
  // If some parameters are passed in, initialize the software and interface
  public void initializeParameters(String[] params) {
    clear();
    
    // Parse other tokens
    if(params.length > 0) {
      this.softwarename = params[0];
      String pfx = KBConstants.CATNS();
      String piecat = params.length > 1 ? pfx+params[1] : null;
      String barcat = params.length > 2 ? pfx+params[2] : null;

      piechart.setActiveCategoryId(piecat, false);
      barchart.setActiveCategoryId(barcat, false);

      initSoftware(this.softwarename);
    }
    else {
      clear();
      software = null; 
    }
  }
  
  private void clear() {
    //notifications.setVisible(false);
    breadcrumbs.setVisible(false);
    heading.setVisible(false);
    piechart.setActiveCategoryId(null, false);
    barchart.setActiveCategoryId(null, false);
    softwareform.setVisible(false);
    piechart.setVisible(false);
    buttons.setVisible(false);
    barchart.setVisible(false);
    breadcrumbs.clear();  
  }
  
  private void initVocabulary() {
    SoftwareREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        vocabulary = vocab;
        initialDraw();
      }
      @Override
      public void onFailure(Throwable reason) { }
    }, false);
  }
  
  private void initSoftware(String softwarename) {
    initSoftware(softwarename, false);
  }
  
  private void initSoftware(String softwarename, final boolean reload) {    
    if(!reload)
      loading.setVisible(true);
    else
      reloadbutton.setIconSpin(true);
    
    SoftwareREST.getSoftware(softwarename, 
        new Callback<Software, Throwable>() {
      @Override
      public void onSuccess(Software sw) {
        reloadbutton.setIconSpin(false);
        loading.setVisible(false);
        savebutton.setEnabled(sw.isDirty());
        
        software = sw;
        initialDraw();
        
        notifications.showNotificationsForSoftware(software.getId());
        
        initMaterial();
        Window.scrollTo(0, 0);
      }
      @Override
      public void onFailure(Throwable reason) {
        reloadbutton.setIconSpin(false);
        loading.setVisible(false);
      }
    }, reload);
  }
  
  private void initialDraw() {
    if(this.vocabulary == null || this.software == null)
      return;

    piechart.setVocabulary(vocabulary);
    barchart.setVocabulary(vocabulary);
    softwareform.setVocabulary(vocabulary);
    
    piechart.setSoftware(software);
    barchart.setSoftware(software);
    softwareform.setSoftware(software);
    
    softwareform.createFormItems();
    initializePiechart();

    setBreadCrumbs();
    heading.setVisible(true);
    
    if(piechart.getActiveCategoryId() != null) {
      MetadataCategory mcat = 
          piechart.getVocabulary().getCategory(piechart.getActiveCategoryId());
      if(mcat != null) {
        piechart.setActiveCategoryId(mcat.getId(), false);
        pieCategorySelected(mcat.getId());
      }
    }
    else
      piechart.setActiveCategoryId(null, false);
    
    if(barchart.getActiveCategoryId() != null) {
      MetadataCategory mcat = 
          barchart.getVocabulary().getCategory(barchart.getActiveCategoryId());
      if(mcat != null) {
        barchart.setActiveCategoryId(mcat.getId(), false);
        barCategorySelected(barchart.getActiveCategoryId());
      }
    }
  }

  private void initializePiechart() {
    if(!piechart.drawnCategories())
      piechart.drawCategories();
    
    piechart.fillCategories(true);

    sidecolumn.setSize("XS_12");
    piecolumn.setSize("XS_10, SM_8, MD_6");
    piecolumn.setOffset("XS_1, SM_2, MD_3");
    
    easeIn(piechart);
    piechart.setVisible(true);
    buttons.setVisible(true);
    
    piechart.updateDimensions();
  }
  
  @UiHandler("piechart")
  void onPieSelect(CategorySelectionEvent event) {
    MetadataCategory pcat = vocabulary.getCategory(piechart.getActiveCategoryId());
    if(pcat != null) {
      History.replaceItem(NameTokens.publish + "/" + softwarename + "/" + pcat.getName(), false);
      barchart.setActiveCategoryId(null, false);
      pieCategorySelected(pcat.getId());
      setBreadCrumbs();
    }
  }
  
  @UiHandler("barchart")
  void onBarSelect(CategorySelectionEvent event) {
    MetadataCategory pcat = vocabulary.getCategory(piechart.getActiveCategoryId());
    MetadataCategory bcat = vocabulary.getCategory(barchart.getActiveCategoryId());
    if(bcat != null && pcat != null) {
      History.replaceItem(NameTokens.publish + "/" + softwarename + "/"
            + pcat.getName() + "/" + bcat.getName() , false);
      barCategorySelected(bcat.getId());
    }
    setBreadCrumbs();
  }

  @UiHandler("savebutton")
  public void onSave(ClickEvent event) {
    final Software tmpsw = softwareform.getSoftware();
    tmpsw.setName(softwarename);
    //savebutton.state().loading();
    SoftwareREST.updateSoftware(tmpsw, new Callback<Software, Throwable>() {
      @Override
      public void onSuccess(Software sw) {
        software = sw;
        softwarename = tmpsw.getName();
        piechart.setSoftware(software);
        barchart.setSoftware(software);
        softwareform.setSoftware(software);
        
        //savebutton.state().reset();
        savebutton.setEnabled(false);
        
        //TODO: Save should reset invalid entries ?
        //piechart.fillCategories(true);
        //piechart.setActiveCategoryId(piechart.getActiveCategoryId(), false);
      }
      @Override
      public void onFailure(Throwable exception) { 
        savebutton.state().reset();
      }
    });    
  }
  
  @UiHandler("reloadbutton")
  public void onReload(ClickEvent event) {
    initSoftware(softwarename, true);
    History.replaceItem(History.getToken(), false);
  }
  
  @UiHandler("softwareform")
  void onSoftwareChange(SoftwareChangeEvent event) {
    software = event.getSoftware();
    software.setDirty(true);
    savebutton.setEnabled(true);
    softwarename = software.getName();
    piechart.setSoftware(software);
    barchart.setSoftware(software);
    softwareform.setSoftware(software);
    piechart.fillCategories();
    barchart.fillCategories();
    piechart.setActiveCategoryId(piechart.getActiveCategoryId(), false);
  }
  
  @UiHandler("softwareform")
  void onPluginResponse(PluginResponseEvent event) {
    PluginResponse response = event.getPluginResponse();
    notifications.addPluginResponse(response, softwareform);
  }
  
  void pieCategorySelected(String categoryId) {
    // Show transition if bar and form aren't visible
    if(!barchart.isVisible() && !softwareform.isVisible()) {
      easeIn(piechart);
    }
    
    sidecolumn.setSize("XS_12");
    piecolumn.setSize("XS_5 SM_4 MD_3");
    piecolumn.setOffset("");
    barcolumn.setSize("XS_7 SM_6 MD_5 LG_4");
    
    barchart.drawCategories(categoryId);
    barchart.fillCategories(false);
    easeIn(barchart);
    barchart.setVisible(true);
    heading.setVisible(false);

    softwareform.setVisible(false);
    
    piechart.updateDimensions();
    barchart.updateDimensions();
  }
  
  void barCategorySelected(String categoryId) {
    sidecolumn.setSize("XS_12 SM_4 MD_3");
    piecolumn.setSize("XS_5 SM_12");
    piecolumn.setOffset("");
    barcolumn.setSize("XS_7 SM_12");
    
    softwareform.showCategoryItems(categoryId);
    easeIn(softwareform);
    softwareform.setVisible(true);
    
    barchart.updateDimensions();
  }
  
  private void setBreadCrumbs() {
    breadcrumbs.clear();
    breadcrumbs.setVisible(true);
    
    AnchorListItem anchor = null;
    if(piechart != null && piechart.getSoftware() != null) {
      String swlabel = piechart.getSoftware().getLabel();
      if(swlabel == null)
        swlabel = piechart.getSoftware().getName();
      anchor = new AnchorListItem(swlabel);
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          History.replaceItem(NameTokens.publish + "/" + softwarename, false);
          clear();
          initialDraw();
          //initSoftware(softwarename);
        }
      });
      anchor.setStyleName("first-crumb");
      breadcrumbs.add(anchor);
    }
    if(piechart != null) {
      final String catid = piechart.getActiveCategoryId();
      if(catid != null) {
        MetadataCategory mcat = this.vocabulary.getCategory(catid);
        anchor = new AnchorListItem(mcat.getLabel()); 
        anchor.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            barchart.setActiveCategoryId(null, false);
            onPieSelect(new CategorySelectionEvent(catid));
          }
        });
        anchor.setStyleName("");
        breadcrumbs.add(anchor);
      }
    }
    if(barchart != null) {
      final String catid = barchart.getActiveCategoryId();
      if(catid != null) {
        MetadataCategory mcat = this.vocabulary.getCategory(catid);
        anchor = new AnchorListItem(mcat.getLabel()); 
        anchor.addClickHandler(new ClickHandler() {
          @Override
          public void onClick(ClickEvent event) {
            onBarSelect(new CategorySelectionEvent(catid));
          }
        });
        anchor.setStyleName("");
        breadcrumbs.add(anchor);
      }
    }
    if(anchor != null)
      anchor.addStyleName("active-crumb");
  }
  
  private void easeIn(Widget w) {
    D3.select(w.getElement()).style("opacity", 0);
    D3.select(w.getElement()).transition().duration(400).style("opacity", 1);
  }
  
}
