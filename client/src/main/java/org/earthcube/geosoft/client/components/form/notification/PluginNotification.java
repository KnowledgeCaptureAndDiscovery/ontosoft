package org.earthcube.geosoft.client.components.form.notification;

import java.util.List;

import org.earthcube.geosoft.client.components.form.SoftwareForm;
import org.earthcube.geosoft.shared.classes.util.GUID;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.plugins.Plugin;
import org.earthcube.geosoft.shared.plugins.PluginResponse;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Panel;
import org.gwtbootstrap3.client.ui.PanelBody;
import org.gwtbootstrap3.client.ui.PanelCollapse;
import org.gwtbootstrap3.client.ui.PanelHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.PanelType;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.html.Paragraph;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.view.client.ListDataProvider;

public class PluginNotification extends Panel {
  PluginResponse response;
  String target;
  String parentid;
  SoftwareForm form;
  
  public PluginNotification(PluginResponse response, 
      String parentid, SoftwareForm form) {
    this.response = response;
    this.parentid = parentid;
    this.form = form;
    this.setType(PanelType.INFO);
    this.target = "collapse-"+GUID.get(8);
    this.add(getPanelHeader());
    this.add(getMainPanel());
  }
  
  class ResponseDetails {
    MetadataProperty property;
    List<Object> propertyValues;
    public ResponseDetails(MetadataProperty prop, List<Object> values) {
      this.property = prop;
      this.propertyValues = values;
    }
    public MetadataProperty getProperty() {
      return property;
    }
    public void setProperty(MetadataProperty property) {
      this.property = property;
    }
    public List<Object> getPropertyValues() {
      return propertyValues;
    }
    public void setPropertyValues(List<Object> propertyValues) {
      this.propertyValues = propertyValues;
    }
  }
  
  public PluginResponse getPluginResponse() {
    return response;
  }

  public void setPluginResponse(PluginResponse response) {
    this.response = response;
  }

  private PanelHeader getPanelHeader() {
    final PluginNotification me = this;
    PanelHeader header = new PanelHeader();
    header.addStyleName("small-header");
    header.setDataTarget("#" + this.target);
    header.setDataToggle(Toggle.COLLAPSE);
    header.setDataParent("#" + this.parentid);
    
    Anchor anchor = new Anchor();
    Plugin plugin = response.getPlugin();
    
//    int numsug = response.getSuggestedMetadata().keySet().size();
//    String summary = (numsug > 0 ) ? numsug + " suggestions" :
//      ( response.getMessage() != null ? "a message" : " some data");
    
    anchor.setIcon(IconType.fromStyleName(plugin.getIcon()));
    anchor.setColor("white");
    anchor.setText("Found some information from "+plugin.getName()); 
    anchor.setIconSize(IconSize.LARGE);
    anchor.setDataToggle(Toggle.COLLAPSE);
    anchor.setDataTarget("#" + this.target);
    anchor.setDataParent("#" + this.parentid);
    
    Anchor delanchor = new Anchor();
    delanchor.setIcon(IconType.REMOVE);
    delanchor.setPull(Pull.RIGHT);
    delanchor.setColor("white");
    delanchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        me.removeFromParent();
      }
    });
    
    header.add(anchor);
    header.add(delanchor);
    return header;
  }
  
  private PanelCollapse getMainPanel() {
    PanelCollapse collapse = new PanelCollapse();
    collapse.setId(this.target);
    collapse.add(this.getContentPanel());;
    return collapse;
  }
  
  private PanelBody getContentPanel() {
    PanelBody body = new PanelBody();
    body.addStyleName("tight-body");
    if(response.getMessage() != null)
      body.add(new Paragraph(response.getMessage()));

    addTableToBody(body);
    return body;
  }
  
  private void addTableToBody(PanelBody body) {
    final PluginNotification me = this;
    
    // Initialize data grid items    
    final ListDataProvider<ResponseDetails> dataProvider = 
        new ListDataProvider<ResponseDetails>();
    
    final CellTable<ResponseDetails> table = new CellTable<ResponseDetails>(10);
    table.setWidth("100%");
    table.setBordered(true);
    table.setStriped(true);
    
    ScrollPanel scrollPanel = new ScrollPanel();
    scrollPanel.setWidth("100%");
    scrollPanel.add(table);
    body.add(scrollPanel);
   
    // Initialize Grid columns
    final Column<ResponseDetails, String> okbutton = 
        new Column<ResponseDetails, String>(
            new ButtonCell(IconType.CHECK, ButtonType.INFO, 
                ButtonSize.SMALL)) {
        @Override
        public String getValue(ResponseDetails details) {
          return "Accept";
        }
    };
    okbutton.setCellStyleNames("button-cell");
    table.setColumnWidth(okbutton, "30px");
    okbutton.setFieldUpdater(new FieldUpdater<ResponseDetails, String>() {
        @Override
        public void update(int index, ResponseDetails details, String value) {
          form.setRawPropertyValues(details.getProperty().getId(), 
              details.getPropertyValues());
          dataProvider.getList().remove(details);
          if(dataProvider.getList().size() == 0)
            me.removeFromParent();
        }
    });
    table.addColumn(okbutton);
    
    /*final Column<ResponseDetails, String> nobutton = 
        new Column<ResponseDetails, String>(
            new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.SMALL)) {
        @Override
        public String getValue(ResponseDetails details) {
          return "";
        }
    };
    nobutton.setCellStyleNames("button-cell");
    nobutton.setFieldUpdater(new FieldUpdater<ResponseDetails, String>() {
        @Override
        public void update(int index, ResponseDetails details, String value) {
          dataProvider.getList().remove(details);
          if(dataProvider.getList().size() == 0)
            me.removeFromParent();
        }
    });
    table.addColumn(nobutton);*/
    
    table.addColumn(new TextColumn<ResponseDetails>() {
      @Override
      public String getValue(ResponseDetails details) {
        return String.valueOf(details.getProperty().getLabel());
      }
    });
    table.addColumn(new TextColumn<ResponseDetails>() {
      @Override
      public String getValue(ResponseDetails details) {
        return String.valueOf(details.getPropertyValues());
      }
    });
    
    dataProvider.addDataDisplay(table);
    
    // Initialize Grid data
    for(String propid : response.getSuggestedMetadata().keySet()) {
      List<Object> values = response.getSuggestedMetadata(propid);
      MetadataProperty prop = form.getVocabulary().getProperty(propid);
      ResponseDetails rd = new ResponseDetails(prop, values);
      dataProvider.getList().add(rd);
    }
    dataProvider.flush();

  }
}
