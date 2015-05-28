package org.earthcube.geosoft.client.application.browse;

import java.util.List;

import org.earthcube.geosoft.client.application.ParameterizedViewImpl;
import org.earthcube.geosoft.client.components.browse.EntityBrowser;
import org.earthcube.geosoft.client.components.chart.CategoryPieChart;
import org.earthcube.geosoft.client.place.NameTokens;
import org.earthcube.geosoft.client.rest.SoftwareREST;
import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataCategory;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataType;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.Well;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;

import com.github.gwtd3.api.D3;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class BrowseView extends ParameterizedViewImpl 
  implements BrowsePresenter.MyView {

  interface Binder extends UiBinder<Widget, BrowseView> {}
  
  @UiField
  PageHeader softwareTitle;

  @UiField
  VerticalPanel softwareBody;

  @UiField
  VerticalPanel loading;
  
  @UiField
  Button htmlbutton, rdfbutton, jsonbutton, editbutton;
  
  @UiField
  CategoryPieChart piechart;
  
  Software software;
  String softwarename;
  String softwarerdf;
  
  Vocabulary vocabulary;
  
  public interface SoftwareCodec extends JsonEncoderDecoder<Software> {}
  SoftwareCodec codec;
  
  @Inject
  public BrowseView(Binder binder) {
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
  }
  
  private void initVocabulary() {
    SoftwareREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
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
    SoftwareREST.getSoftware(softwarename, new Callback<Software, Throwable>() {
      @Override
      public void onSuccess(Software sw) {
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
    SoftwareREST.getSoftwareRDF(software.getId(), new Callback<String, Throwable>() {
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
  
  public void showSoftware(Software sw) {
    if(sw == null || vocabulary == null)
      return;

    initializePieChart();
    softwareTitle.setText(sw.getLabel());
    
    List<Entity> authorlist = sw.getPropertyValues(KBConstants.ONTNS()+"hasCreator");
    String authors = authorlist.toString();
    if(authorlist.isEmpty())
      authors = "[No author listed]";
    softwareTitle.setSubText(authors);
    softwareBody.clear();
    
    String topcatid = KBConstants.CATNS()+"MetadataCategory";
    MetadataCategory topcat = vocabulary.getCategory(topcatid);
    topcat = vocabulary.orderChildCategories(topcat);
    
    MetadataType swtype = vocabulary.getType(sw.getType());
    
    EntityBrowser browser = new EntityBrowser(vocabulary);
    
    for(String lvl1catid: topcat.getChildren()) {
      // Level 1 properties
      Well lvl1well = new Well(); 
      
      MetadataCategory lvl1cat = vocabulary.getCategory(lvl1catid);
      Heading heading1 = new Heading(HeadingSize.valueOf("H2"));
      heading1.setText(lvl1cat.getLabel());
      heading1.addStyleName("catheading");
      lvl1well.add(heading1);
      
      lvl1cat = vocabulary.orderChildCategories(lvl1cat);
      boolean hasSomeValues = false;
      
      for(String lvl2catid: lvl1cat.getChildren()) {
        MetadataCategory lvl2cat = vocabulary.getCategory(lvl2catid);
        Heading heading2 = new Heading(HeadingSize.valueOf("H3"));
        heading2.setText(lvl2cat.getLabel());
        heading2.addStyleName("catheading");
        String sublabel = lvl2cat.getSublabel();
        if(sublabel != null) {
          sublabel = Character.toUpperCase(sublabel.charAt(0)) + sublabel.substring(1);
          heading2.setSubText(" - " + sublabel);
        }
        lvl1well.add(heading2);
        
        List<MetadataProperty> props = vocabulary.getPropertiesForType(swtype);
        props.retainAll(vocabulary.getPropertiesInCategory(lvl2cat));
        props = vocabulary.orderProperties(props);
        
        String html = browser.getEntitiesHTML(sw.getPropertyValues(), props, false);
        HTML lvl2html = new HTML(html);
        if(hasSomePropertyValues(props, sw)) {
          hasSomeValues = true;
        }
        else {
          heading2.addStyleName("hide-this-in-html");
          lvl2html.addStyleName("hide-this-in-html");
        }
        
        lvl1well.add(lvl2html);
      }
      softwareBody.add(lvl1well);
      
      if(!hasSomeValues)
        lvl1well.addStyleName("hide-this-in-html");
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
    History.newItem(NameTokens.publish + "/" + software.getName());
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
    String contenthtml = softwareTitle.getElement().getInnerHTML()
        + softwareBody.getElement().getInnerHTML();
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
        "</head><body>"+ contenthtml +"</body></html>";
    openWindow("text/html", html);
  }
  
  native void openWindow(String mime, String content) /*-{
    window.open("data:"+mime+";base64,"+btoa(unescape(encodeURIComponent(content))));
  }-*/;
  
  private void easeIn(Widget w) {
    D3.select(w.getElement()).style("opacity", 0);
    D3.select(w.getElement()).transition().duration(400).style("opacity", 1);
  }

}
