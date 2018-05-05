package org.ontosoft.client.application.version.browse;

import java.util.List;

import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.DeviceSize;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.browse.EntityBrowser;
import org.ontosoft.client.components.chart.CategoryBarChart;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.SoftwareSummary;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

import com.github.gwtd3.api.D3;
import com.github.gwtd3.api.core.Value;
import com.github.gwtd3.api.functions.DatumFunction;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VersionBrowseView extends ParameterizedViewImpl 
  implements VersionBrowsePresenter.MyView {

  interface Binder extends UiBinder<Widget, VersionBrowseView> {}
  
  @UiField
  PageHeader softwareTitle;

  @UiField
  VerticalPanel softwareBody;

  @UiField
  VerticalPanel loading;
  
  @UiField
  Button publishbutton, cancelbutton, bigpublishbutton;
  
  @UiField
  Modal publishdialog;
  
  @UiField
  TextBox softwarelabel;
  
  @UiField
  Button htmlbutton, rdfbutton, jsonbutton, editbutton;
  
  @UiField
  CategoryPieChart piechart;
  
  SoftwareREST api = SoftwareREST.get(Config.getServerURL());

  SoftwareVersion software;
  String softwarename;
  String softwarerdf;
  String softwarehtml;

  Vocabulary vocabulary;
  
  public interface SoftwareCodec extends JsonEncoderDecoder<SoftwareVersion> {}
  SoftwareCodec codec;
  
  @Inject
  public VersionBrowseView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    
    this.codec = GWT.create(SoftwareCodec.class);
  }

  // If some parameters are passed in, initialize the software and interface
  public void initializeParameters(String[] params) {    
    clear();
    if(params.length > 0) {
      this.softwarename = params[0];
      initSoftware(this.softwarename);
    }
    UserSession session = SessionStorage.getSession();
    if(session != null && session.getUsername() != null)
      editbutton.setVisible(true);
  }
  
  private void initVocabulary() {
    this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        vocabulary = vocab;
        if(software != null)
          showSoftware(software);
      }
      @Override
      public void onFailure(Throwable reason) {
        GWT.log("Error fetching Vocabulary", reason);
      }
    }, false);
  }
  
  private void initSoftware(String softwarename) {
    loading.setVisible(true);
    
    String[] swnames = softwarename.split("\\s*:\\s*");

    this.api.getSoftwareVersion(swnames[0], swnames[1], new Callback<SoftwareVersion, Throwable>() {
      @Override
      public void onSuccess(SoftwareVersion sw) {
        software = sw;
        initSoftwareRDF();
        loading.setVisible(false);
        if(vocabulary != null)
          showSoftware(software);
      }
      @Override
      public void onFailure(Throwable exception) {
        GWT.log("Error fetching Software", exception);
      }
    }, false);
  }
  
  private void initSoftwareRDF() {
    this.api.getSoftwareRDF(software.getId(), new Callback<String, Throwable>() {
      @Override
      public void onSuccess(String rdf) {
        softwarerdf = rdf;
      }
      @Override
      public void onFailure(Throwable exception) {
        GWT.log("Error fetching Software RDF", exception);
      }
    });
  }
  
  private void clear() {
    htmlbutton.getParent().setVisible(false);
    editbutton.getParent().setVisible(false);
    editbutton.setVisible(false);
    piechart.setVisible(false);
    softwareBody.setVisible(false);
    softwareTitle.setVisible(false);
    softwareBody.clear();
    softwareTitle.setText(null);
    softwareTitle.setSubText(null);
    software = null;
  }

  private void initializePieChart() {
    piechart.setEventEnabled(false);
    piechart.setVisible(true);
    piechart.setVocabulary(vocabulary);
    piechart.setSoftware(software);
    if(!piechart.drawnCategories())
      piechart.drawCategories();
    piechart.fillCategories(true); 
    //piechart.setActiveCategoryId(null, false);
  }
  
  public void showSoftware(SoftwareVersion sw) {
    if(sw == null || vocabulary == null)
      return;

    initializePieChart();
    
    Entity swName = sw.getPropertyValue(KBConstants.ONTNS()+"hasName");
    if (swName != null)
    	softwareTitle.setText(swName.getValue().toString());
    else
    	softwareTitle.setText(sw.getLabel());
    
    softwareBody.clear();
    
    String topcatid = KBConstants.CATNS()+"MetadataCategory";
    MetadataCategory topcat = vocabulary.getCategory(topcatid);
    topcat = vocabulary.orderChildCategories(topcat);
    
    MetadataType swtype = vocabulary.getType(sw.getType());
    
    EntityBrowser browser = new EntityBrowser(vocabulary);
    
    for(String lvl1catid: topcat.getChildren()) {
      Row catrow = new Row();
      
      // Level 1 category
      MetadataCategory lvl1cat = vocabulary.getCategory(lvl1catid);
      
      // Main panel
      Column panelcol = new Column("SM_9");
      catrow.add(panelcol);
      Panel lvl1panel = new Panel(PanelType.INFO);
      panelcol.add(lvl1panel);
      PanelHeader header1 = new PanelHeader();
      Heading heading1 = new Heading(HeadingSize.valueOf("H2"));
      heading1.setText(lvl1cat.getLabel().toUpperCase());
      header1.add(heading1);
      lvl1panel.add(header1);
      PanelBody body1 = new PanelBody();
      lvl1panel.add(body1);
      
      // Side panel
      Column sidecol = new Column("SM_3");
      sidecol.setHiddenOn(DeviceSize.XS);
      catrow.add(sidecol);
      Panel sidepanel = new Panel(PanelType.INFO);
      sidecol.add(sidepanel);
      PanelHeader sideheader = new PanelHeader();
      Heading sideheading = new Heading(HeadingSize.valueOf("H2"));
      sideheader.add(sideheading);
      sidepanel.add(sideheader);
      PanelBody sidebody = new PanelBody();
      sidepanel.add(sidebody);
      
      CategoryPieChart pchart = new CategoryPieChart(lvl1cat.getName(), 250);
      pchart.setSoftware(sw);
      pchart.setEventEnabled(false);
      pchart.setVocabulary(vocabulary);
      pchart.drawCategories();
      pchart.fillCategories(false);
      pchart.setActiveCategoryId(lvl1catid, false);
      sidebody.add(pchart);
      
      Double done = pchart.getDonePercentage(lvl1catid);
      Double optdone = pchart.getDonePercentage(lvl1catid, true);
      sideheading.setText("Done: "+done.intValue()+"% ("+optdone.intValue()+"% optional)");
      
      CategoryBarChart chart = new CategoryBarChart(lvl1cat.getName(), 250, 250);
      chart.setSoftware(sw);
      chart.setEventEnabled(false);
      chart.setVocabulary(vocabulary);
      chart.drawCategories(lvl1catid);
      chart.fillCategories(false);
      sidebody.add(chart);
      
      lvl1cat = vocabulary.orderChildCategories(lvl1cat);
      boolean hasSomeValues = false;
      
      for(String lvl2catid: lvl1cat.getChildren()) {
        MetadataCategory lvl2cat = vocabulary.getCategory(lvl2catid);
        Heading heading2 = new Heading(HeadingSize.valueOf("H4"));
        heading2.setText(lvl2cat.getLabel());
        String sublabel = lvl2cat.getSublabel();
        if(sublabel != null) {
          sublabel = Character.toUpperCase(sublabel.charAt(0)) + sublabel.substring(1);
          heading2.setSubText(" - " + sublabel);
        }
        body1.add(heading2);

        List<MetadataProperty> props = vocabulary.getPropertiesForType(swtype);
        props.retainAll(vocabulary.getPropertiesInCategory(lvl2cat));
        props = vocabulary.orderProperties(props);
        
        String html = browser.getEntitiesHTML(sw, props, false);
        HTML lvl2html = new HTML(html);
        if(hasSomePropertyValues(props, sw)) {
          hasSomeValues = true;
        }
        else {
          heading2.addStyleName("hide-this-in-html");
          lvl2html.addStyleName("hide-this-in-html");
        }
        body1.add(lvl2html);
      }
      
      softwareBody.add(catrow);
      
      if(!hasSomeValues) {
        lvl1panel.addStyleName("hide-this-in-html");
      }
    }
    
    htmlbutton.getParent().setVisible(true);
    editbutton.getParent().setVisible(true);
    softwareBody.setVisible(true);
    softwareTitle.setVisible(true);
    easeIn(htmlbutton.getParent());
    easeIn(editbutton.getParent());
    easeIn(softwareBody);
    easeIn(softwareTitle);
    
    initMaterial();
    Window.scrollTo(0, 0);
    
    bigpublishbutton.getParent().setVisible(true);
  }
  
  
  private boolean hasSomePropertyValues(List<MetadataProperty> props, Software sw) {
    for(MetadataProperty prop : props) {
      if(sw.getPropertyValues(prop.getId()).size() > 0) {
        return true;
      }
    }
    return false;
  }

  @UiHandler("editbutton")
  void onEditButtonClick(ClickEvent event) {
	String[] swnames = softwarename.split("\\s*:\\s*");
    History.newItem(NameTokens.publishversion + "/" + swnames[0] + ":" + software.getName());
  }
  
  @UiHandler("rdfbutton")
  void onRDFButtonClick(ClickEvent event) {
    openWindow("text/plain", softwarerdf);
  }
  
  @UiHandler("jsonbutton")
  void onJsonButtonClick(ClickEvent event) {
    openWindow("application/json", codec.encode(software).toString());
  }
  
  @UiHandler("htmlbutton")
  void onHTMLButtonClick(ClickEvent event) {
    softwarehtml = softwareTitle.getElement().getInnerHTML();
    D3.selectAll(".col-sm-9").datum(new DatumFunction<Void>() {
      @Override
      public Void apply(Element context, Value d, int index) {
        softwarehtml += context.getInnerHTML();
        return null;
      }
    });
    String styles =  "<style>\n"+
        "* { font-family: Arial, Helvetica }"+
        ".browse-label {\n"+
        "   color: #5D7BA0 !important;\n"+
        "   background-color: rgba(93,123,160,0.05) !important;\n"+
        "   border: 1px solid rgba(93,123,160,0.08) !important;\n"+
        "   border-radius: 4px;\n"+
        "   padding-left: 4px;\n"+
        "   padding-right: 4px;\n"+
        "   margin-left: -3px;\n"+
        "}\n"+
        ".browse-label {\n"+
        "   font-weight: bold;\n"+
        "   font-size: 0.85em;\n"+
        "}\n"+
        ".browse-label.error-label {\n"+
        "   background-color: rgba(212, 32, 65,0.05) !important;\n"+
        "   border: 1px solid rgba(212, 32, 65,0.08) !important;\n" +
        "   color: #D42041 !important;\n"+
        "}\n"+
        ".browse-label.error-label.optional {\n"+
        "   color: #969696 !important;\n"+
        "   background-color: #F9F9F9 !important;\n"+
        "   border: 1px solid #F0F0F0 !important;\n"  +
        "}\n"+
        ".hide-this-in-html {\n"+
        "   display: none;\n"+
        "}\n"+
        ".wrap-long-words {\n" + 
        "  -ms-word-break: break-all;\n" + 
        "  word-break: break-all;\n" + 
        "  word-break: break-word;\n" + 
        "  -webkit-hyphens: auto;\n" + 
        "  -moz-hyphens: auto;\n" + 
        "  hyphens: auto;\n" + 
        "}\n" + 
        ".wrap-pre {\n" + 
        "  width: 100%;\n" + 
        "  white-space: pre-wrap;\n" + 
        "}\n"+
        "</style>\n";    
    String html = "<html><head>" + styles + 
        "</head><body>"+ softwarehtml +"</body></html>";
    openWindow("text/html", html);
  }
  
  native void openWindow(String mime, String content) /*-{
    window.open("data:"+mime+";base64,"+btoa(unescape(encodeURIComponent(content))));
  }-*/;
  
  private void easeIn(Widget w) {
    D3.select(w.getElement()).style("opacity", 0);
    D3.select(w.getElement()).transition().duration(400).style("opacity", 1);
  }
  
  @UiHandler("publishdialog")
  void onShowWindow(ModalShownEvent event) {
    softwarelabel.setFocus(true);
  }
  
  @UiHandler("bigpublishbutton")
  void onOpenPublishButtonClick(ClickEvent event) {
    publishdialog.show();
  }
  
  @UiHandler("publishbutton")
  void onPublishButtonClick(ClickEvent event) {
    submitPublishForm();
    event.stopPropagation();
  }
  
  @UiHandler("softwarelabel")
  void onSoftwareEnter(KeyPressEvent event) {
    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      submitPublishForm();
    }
  }
  
  @UiHandler("cancelbutton")
  void onCancelPublish(ClickEvent event) {
    softwarelabel.setValue(null);
  }
  
  private void submitPublishForm() {
    String label = softwarelabel.getValue();
    if(softwarelabel.validate(true)) {
      SoftwareVersion tmpsw = new SoftwareVersion();
      tmpsw.setLabel(label);
      this.api.publishSoftwareVersion(softwarename, tmpsw, new Callback<SoftwareVersion, Throwable>() {
        public void onSuccess(SoftwareVersion sw) {
          // Add item to list
          SoftwareSummary newsw = new SoftwareSummary(sw);
          newsw.setExternalRepositoryId(SoftwareREST.LOCAL);
          // TODO: do the same to versions
          //addToList(newsw);
          //updateList();
          
          // Go to the new item
          History.newItem(NameTokens.publish + "/" + sw.getName());
          
          publishdialog.hide();
          softwarelabel.setValue(null);
        }
        @Override
        public void onFailure(Throwable exception) { }
      });
    } 
  }

}
