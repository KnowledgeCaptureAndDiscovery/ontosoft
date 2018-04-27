package org.ontosoft.client.application.version.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.Row;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.ButtonCell;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.form.facet.FacetedSearchPanel;
import org.ontosoft.client.components.form.facet.events.FacetSelectionEvent;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.SoftwareVersionSummary;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.utils.PermUtils;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class SoftwareVersionListView extends ParameterizedViewImpl 
  implements SoftwareVersionListPresenter.MyView {
  
  boolean isreguser, isadmin;
  
  @UiField
  Button publishbutton, cancelbutton, bigpublishbutton, clearsearch;

  @UiField
  Modal publishdialog;
  
  @UiField
  Button comparebutton, selectionswitch;

  boolean selectionmode = false;

  @UiField
  VerticalPanel loading;
  
  @UiField
  Row content;
  
  @UiField
  TextBox softwarelabel, searchbox;
  
  @UiField
  FacetedSearchPanel facets;
  
  @UiField(provided = true)
  CellTable<SoftwareVersionSummary> table = new CellTable<SoftwareVersionSummary>(40);

  @UiField
  SimplePager pager;
  
  SoftwareREST api;
  Map<String, SoftwareREST> apis;
  Map<String, String> clientUrls;
  
  private List<SoftwareVersionSummary> allSoftwareList;
  private HashMap<String, Boolean> filteredSoftwareIdMap =
      new HashMap<String, Boolean>();
  private ListDataProvider<SoftwareVersionSummary> listProvider = 
      new ListDataProvider<SoftwareVersionSummary>();

  private List<SoftwareVersionSummary> selections;
  
  private Comparator<SoftwareVersionSummary> swcompare;
  
  interface Binder extends UiBinder<Widget, SoftwareVersionListView> {
  }

  @Inject
  public SoftwareVersionListView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initAPIs();
    initVocabulary();
    initTable();
    initList();
  }
  
  private void initAPIs() {
    this.api = SoftwareREST.get(Config.getServerURL());
    
    this.apis = new HashMap<String, SoftwareREST>();
    this.apis.put(SoftwareREST.LOCAL, this.api);
    this.clientUrls = new HashMap<String, String>();
    
    final List<Map<String, String>> xservers = Config.getExternalServers();
    for(Map<String, String> xserver : xservers) {
      String xname = xserver.get("name");
      String xurl = xserver.get("server");
      String xclient = xserver.get("client");
      SoftwareREST xapi = SoftwareREST.get(xurl);
      this.apis.put(xname, xapi);
      clientUrls.put(xname, xclient);
    }    
  }
  
  private void initVocabulary() {
    this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        facets.showFacetGroups(vocab);
      }
      @Override
      public void onFailure(Throwable reason) {
        GWT.log("Error fetching Vocabulary", reason);
      }
    }, false);
  }

  public void initList() {
    final List<String> loaded = new ArrayList<String>();
    
    for(final String sname : apis.keySet()) {
      SoftwareREST sapi = apis.get(sname);
      allSoftwareList = new ArrayList<SoftwareVersionSummary>();
      sapi.getSoftwareVersionList(new Callback<List<SoftwareVersionSummary>, Throwable>() {
        @Override
        public void onSuccess(List<SoftwareVersionSummary> list) {
          for(SoftwareVersionSummary sum : list) {
            sum.setExternalRepositoryId(sname);
            sum.setExternalRepositoryUrl(clientUrls.get(sname));
          }
          allSoftwareList.addAll(list);
          Collections.sort(allSoftwareList, swcompare);
          for(SoftwareVersionSummary sum : list)
            filteredSoftwareIdMap.put(sum.getId(), true);
          
          loaded.add(sname);
          if(loaded.size() == apis.size()) {
            listProvider.getList().clear();
            listProvider.getList().addAll(allSoftwareList);
            listProvider.flush();
            initMaterial();
            loading.setVisible(false);
            content.setVisible(true);
            Window.scrollTo(0, 0);              
          }
        }
        @Override
        public void onFailure(Throwable reason) { }
      }, false);       
    }   
  }

  
  @Override
  public void initializeParameters(String[] params) {
    UserSession session = SessionStorage.getSession();
    this.isreguser = false;
    this.isadmin = false;
    if( session != null) {
      this.isreguser = true;
      if (session.getRoles().contains("admin"))
        this.isadmin = true;
    }
    this.updateList();
    this.redrawControls();
  }
  
  public void redrawControls() {
    table.removeStyleName("edit-col");
    table.removeStyleName("delete-col");
	
    bigpublishbutton.getParent().setVisible(false);
    
    if(this.isadmin || this.isreguser) {
      bigpublishbutton.getParent().setVisible(true);
      table.addStyleName("edit-col");
      table.addStyleName("delete-col");
      table.redraw();
    }
  }
  
  private void initTable() {
    ListHandler<SoftwareVersionSummary> sortHandler =
        new ListHandler<SoftwareVersionSummary>(listProvider.getList());
    
    selections = new ArrayList<SoftwareVersionSummary>();
    
    table.addColumnSortHandler(sortHandler);
    table.setEmptyTableWidget(new Label("No Software found.."));
    
    this.swcompare = new Comparator<SoftwareVersionSummary>() {
      @Override
      public int compare(SoftwareVersionSummary sw1, SoftwareVersionSummary sw2) {
        if(sw1.getLabel() != null && sw2.getLabel() != null)
          return sw1.getLabel().compareToIgnoreCase(sw2.getLabel());
        return 0;
      }
    };
    
    // Name Column
    // TODO: Add extra rows for associated Software Versions too (indented)
    final SafeHtmlCell progressCell = new SafeHtmlCell();
    Column<SoftwareVersionSummary, SafeHtml> namecol = new Column<SoftwareVersionSummary, SafeHtml>(progressCell) {
        @Override
        public SafeHtml getValue(SoftwareVersionSummary summary) {
            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            
            String link = "#" + NameTokens.version + "/" + summary.getSoftwareSummary().getName() + ":" + summary.getName();
            String extralabel = "";
            
            if(!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL)) {
              link = summary.getExternalRepositoryUrl() + link;
              extralabel = "<div class='external-repo'>" 
                  + summary.getExternalRepositoryId()+"</div>";
            }
            
            sb.appendHtmlConstant("<div class='software-list-item'>");
            sb.appendHtmlConstant("<div class='software-name'>");
            sb.appendHtmlConstant(extralabel);
            if (summary.getSoftwareName() != null)
              sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getSoftwareName() + "</a>");
            else
              sb.appendHtmlConstant("<a href='" + link + "'>" + summary.getLabel() + "</a>");
            sb.appendHtmlConstant("</div>");
            
            if(summary.getDescription() != null)
              sb.appendHtmlConstant("<div class='wrap-pre wrap-long-words software-description'>" + 
                  summary.getDescription() + "</div>");
            
            if(summary.getAuthors() != null) {
              if(summary.getAuthors().size() == 1) {
                sb.appendHtmlConstant("<div class='software-meta'>Author: " +
                    summary.getAuthors().get(0)+" </div>");
              }
              else {
                String authors = "";
                for(int i=0; i<summary.getAuthors().size(); i++) {
                  if(i > 0) authors += ", ";
                  authors += summary.getAuthors().get(i);
                }
                sb.appendHtmlConstant("<div class='software-meta'>Authors: " +
                    authors+" </div>");
              }
            }
            
            if(summary.getUser() != null) {
              String poststring = "<div class='software-meta'>Posted by: " + summary.getUser();
              if(summary.getTime() > 0) {
                DateTimeFormat fmt = DateTimeFormat.getFormat(PredefinedFormat.DATE_TIME_SHORT);
                String datestr = fmt.format(new Date(summary.getTime()));
                poststring += " at " + datestr;
              }
              poststring += "</div>";
              sb.appendHtmlConstant(poststring);
            }
            sb.appendHtmlConstant("</div>");
            return sb.toSafeHtml();
        }
    };

    table.addColumn(namecol);
    namecol.setSortable(true);
    sortHandler.setComparator(namecol, this.swcompare);
    table.getColumnSortList().push(namecol);
    
    // Delete Button Column
    Column<SoftwareVersionSummary, String> deletecolumn = 
        new Column<SoftwareVersionSummary, String>(
            new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL)) {
      @Override
      public String getValue(SoftwareVersionSummary details) {
        return "Delete";
      }
      @Override
      public String getCellStyleNames(Context context,
          SoftwareVersionSummary summary) {
        UserSession session = SessionStorage.getSession();
        String style = "hidden-cell";
        if (isadmin || 
            (isreguser && 
                PermUtils.hasOwnerAccess(summary.getPermission(), session.getUsername()))) {
          if(summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
            style = "delete-cell";
        }
        return style;
      }
    };
    deletecolumn.setFieldUpdater(new FieldUpdater<SoftwareVersionSummary, String>() {
      @Override
      public void update(int index, SoftwareVersionSummary summary, String value) {
        deleteSoftware(summary);
      }
    });
    table.addColumn(deletecolumn);

    // Edit Button Column
    final Column<SoftwareVersionSummary, String> editcol = 
        new Column<SoftwareVersionSummary, String>(
            new ButtonCell(IconType.EDIT, ButtonType.INFO, ButtonSize.EXTRA_SMALL)) {
        @Override
        public String getValue(SoftwareVersionSummary details) {
          return "Edit";
        }
        @Override
        public String getCellStyleNames(Context context,
            SoftwareVersionSummary summary) {
          UserSession session = SessionStorage.getSession();
          String style = "hidden-cell";
          if (isadmin ||
              (isreguser && 
                  (PermUtils.getAccessLevelForUser(
                  summary.getPermission(), session.getUsername(), summary.getId()) 
                  .equals("Write"))))
            if(summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
              style = "edit-cell";
          return style;
        }     
    };
    
    editcol.setFieldUpdater(new FieldUpdater<SoftwareVersionSummary, String>() {
      @Override
      public void update(int index, SoftwareVersionSummary summary, String value) {
        String swname = summary.getName();
        History.newItem(NameTokens.publish + "/" + swname);
      }
    });
    table.addColumn(editcol);
    
    // Checkbox, software selection column (to select software to compare)
    Column<SoftwareVersionSummary, Boolean> checkboxcolumn = 
        new Column<SoftwareVersionSummary, Boolean>(new CheckboxCell(true, true)) {
        @Override
        public Boolean getValue(SoftwareVersionSummary summary) {
          return selections.contains(summary);
        }
    };
    checkboxcolumn.setFieldUpdater(new FieldUpdater<SoftwareVersionSummary, Boolean>() {
        @Override
        public void update(int index, SoftwareVersionSummary summary, Boolean value) {
          if(value) 
            selections.add(summary);
          else
            selections.remove(summary);
          table.redrawRow(index);
          updateCompareButton();
        }
    });
    
    checkboxcolumn.setCellStyleNames("selection-cell");
    table.addColumn(checkboxcolumn);
    
    // Set row styles
    table.setRowStyles(new RowStyles<SoftwareVersionSummary>() {
      @Override
      public String getStyleNames(SoftwareVersionSummary summary, int rowIndex) {
       if(selections.contains(summary))
         return "selected-row";
       return "";
      }
    });
    
    
    // Bind list & pager to table
    pager.setDisplay(table);
    listProvider.addDataDisplay(table);
  }
  
  private void deleteSoftware(final SoftwareVersionSummary sw) {
    if (Window.confirm("Are you sure you want to delete the software ?")) {
      this.api.deleteSoftware(sw.getName(),
          new Callback<Void, Throwable>() {
            @Override
            public void onSuccess(Void v) {
              removeFromList(sw);
              updateList();
            }
            @Override
            public void onFailure(Throwable exception) { }
          }
      );
    }
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
      // TODO: provide Software name, instead of an empty string
      this.api.publishSoftwareVersion("", tmpsw, new Callback<SoftwareVersion, Throwable>() {
        public void onSuccess(SoftwareVersion sw) {
          // Add item to list
          SoftwareVersionSummary newsw = new SoftwareVersionSummary(sw);
          newsw.setExternalRepositoryId(SoftwareREST.LOCAL);
          addToList(newsw);
          updateList();
          
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
  
  private void addToList(SoftwareVersionSummary summary) {
    boolean contains = false;
    for(SoftwareVersionSummary sum : allSoftwareList) {
      if(sum.getId().equals(summary.getId())) {
        contains = true;
        break;
      }
    }
    if(!contains)
      allSoftwareList.add(summary);
    filteredSoftwareIdMap.put(summary.getId(), true);
    Collections.sort(allSoftwareList, swcompare);    
  }
  
  private void removeFromList(SoftwareVersionSummary summary) {
    filteredSoftwareIdMap.remove(summary);
    allSoftwareList.remove(summary);
  }
  
  @UiHandler("searchbox")
  void onSearch(KeyUpEvent event) {
    updateList();
  }
  
  @UiHandler("clearsearch")
  void onClearSearch(ClickEvent event) {
    searchbox.setValue("");
    updateList();
  }
  
  void updateList() {
    listProvider.getList().clear();
    String value = searchbox.getValue();
    for(SoftwareVersionSummary summary : allSoftwareList) {
      if(filteredSoftwareIdMap.containsKey(summary.getId())) {
        if(value == null || value.equals("") ||
            summary.getLabel().toLowerCase().contains(value.toLowerCase()))
        listProvider.getList().add(summary);
      }
    }
    this.listProvider.flush();    
  }
  
  void updateCompareButton() {
    String text = "Compare";
    if(selections.size() > 0)
      text += " ("+selections.size()+")";
    comparebutton.setText(text);
  }
  
  @UiHandler("facets")
  void onFacetSelection(FacetSelectionEvent event) {
    final List<String> loaded = new ArrayList<String>();
    
    final List<SoftwareVersionSummary> facetList = new ArrayList<SoftwareVersionSummary>();
    for(final String sname : apis.keySet()) {
      SoftwareREST sapi = apis.get(sname);
      sapi.getSoftwareVersionListFaceted(facets.getFacets(),
          new Callback<List<SoftwareVersionSummary>, Throwable>() {
        @Override
        public void onSuccess(List<SoftwareVersionSummary> list) {
          facetList.addAll(list);
          loaded.add(sname);
          if(loaded.size() == apis.size()) {
            filteredSoftwareIdMap.clear();
            for(SoftwareVersionSummary flist: facetList)
              filteredSoftwareIdMap.put(flist.getId(), true);
            updateList();
          }
        }
        @Override
        public void onFailure(Throwable reason) { }
      });
    }
  }
  
  @UiHandler("selectionswitch")
  void onSelectionModeToggle(ClickEvent event) {
    if(!selectionmode) {
      selectionmode = true;
      selectionswitch.setIcon(IconType.CHECK_SQUARE_O);
      table.addStyleName("selection-table");
    }
    else {
      selectionmode = false;
      selectionswitch.setIcon(IconType.SQUARE_O);
      table.removeStyleName("selection-table");
      
      // Remove all selections
      selections.clear();
      table.redraw();
      updateCompareButton();
    }
  }  
  
  @UiHandler("comparebutton")
  void onCompareButtonClick(ClickEvent event) {
    String idtext = "";
    if(selections.size() < 2)
      Window.alert("Select atleast 2 software to compare.\n\n"
          + "Click on the checkbox next to the compare button.\n"
          + "Then select software using checkboxes in each row");
    else if(selections.size() > 10)
      Window.alert("Cannot compare more than 10 at a time");    
    else {
      int i=0;
      for(SoftwareVersionSummary summary : selections) {
        if(i > 0) idtext += ",";
        if(!summary.getExternalRepositoryId().equals(SoftwareREST.LOCAL))
          idtext += summary.getExternalRepositoryId()+":";
        idtext += summary.getName();
        i++;
      }
      History.newItem(NameTokens.compare + "/" + idtext);
    }
  }

}
