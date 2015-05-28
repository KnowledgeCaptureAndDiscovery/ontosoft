package org.earthcube.geosoft.client.application.list;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.earthcube.geosoft.client.application.ParameterizedViewImpl;
import org.earthcube.geosoft.client.components.form.facet.FacetedSearchPanel;
import org.earthcube.geosoft.client.components.form.facet.events.FacetSelectionEvent;
import org.earthcube.geosoft.client.place.NameTokens;
import org.earthcube.geosoft.client.rest.SoftwareREST;
import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.SoftwareSummary;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
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

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;
import com.google.gwt.text.shared.SafeHtmlRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class SoftwareListView extends ParameterizedViewImpl 
  implements SoftwareListPresenter.MyView {
  
  boolean isadmin;
  String authparam = "";
  
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
  CellTable<SoftwareSummary> table = new CellTable<SoftwareSummary>(20);

  @UiField
  SimplePager pager;
  
  private List<SoftwareSummary> allSoftwareList;
  private HashMap<String, Boolean> filteredSoftwareIdMap =
      new HashMap<String, Boolean>();
  private ListDataProvider<SoftwareSummary> listProvider = 
      new ListDataProvider<SoftwareSummary>();

  Column<SoftwareSummary, String> deletecolumn;
  Column<SoftwareSummary, Boolean> checkboxcolumn;

  private List<SoftwareSummary> selections;
  
  private Comparator<SoftwareSummary> swcompare;
  
  interface Binder extends UiBinder<Widget, SoftwareListView> {
  }

  @Inject
  public SoftwareListView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    initTable();
    initList();
  }
  
  private void initVocabulary() {
    SoftwareREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
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
    SoftwareREST.getSoftwareList(new Callback<List<SoftwareSummary>, Throwable>() {
      @Override
      public void onSuccess(List<SoftwareSummary> list) {        
        Collections.sort(list, swcompare);
        allSoftwareList = list;
        for(SoftwareSummary sum : list)
          filteredSoftwareIdMap.put(sum.getId(), true);
        listProvider.getList().clear();
        listProvider.getList().addAll(list);
        listProvider.flush();

        initMaterial();
        loading.setVisible(false);
        content.setVisible(true);
        Window.scrollTo(0, 0);
      }
      @Override
      public void onFailure(Throwable reason) { }
    }, false);
  }
  
  @Override
  public void initializeParameters(String[] params) {
    // Check for admin token
    if(params.length > 0) {
      if(params[params.length - 1].equals("__admin")) {
        this.isadmin = true;
        this.authparam = "/__admin";
        params = Arrays.copyOf(params, params.length - 1);
      }
    } else {
      this.authparam = "";
      this.isadmin = false;
    }
    
    if(params.length == 0) {
      if(isadmin) {
        table.addStyleName("admin-table");
      }
      else {
        table.removeStyleName("admin-table");
      }
    }
  }
  
  private void initTable() {
    ListHandler<SoftwareSummary> sortHandler =
        new ListHandler<SoftwareSummary>(listProvider.getList());
    
    selections = new ArrayList<SoftwareSummary>();
    
    table.addColumnSortHandler(sortHandler);
    table.setEmptyTableWidget(new Label("No Software found.."));
    
    this.swcompare = new Comparator<SoftwareSummary>() {
      @Override
      public int compare(SoftwareSummary sw1, SoftwareSummary sw2) {
        if(sw1.getLabel() != null && sw2.getLabel() != null)
          return sw1.getLabel().compareToIgnoreCase(sw2.getLabel());
        return 0;
      }
    };
    
    SafeHtmlRenderer<String> anchorRenderer = new AbstractSafeHtmlRenderer<String>() {
      @Override
      public SafeHtml render(String object) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<a href=\"javascript:;\">").appendEscaped(object)
            .appendHtmlConstant("</a>");
        return sb.toSafeHtml();
      }
    };
    
    // Name Column
    // TODO: Add extra rows for associated Software Versions too (indented)
    Column<SoftwareSummary, String> namecol = 
        new Column<SoftwareSummary, String>(new ClickableTextCell(anchorRenderer)) {
      @Override
      public String getValue(SoftwareSummary summary) {
        return summary.getLabel();
      }
    };
    namecol.setFieldUpdater(new FieldUpdater<SoftwareSummary, String>() {
      @Override
      public void update(int index, SoftwareSummary summary, String value) {
        String swname = summary.getName();
        History.newItem(NameTokens.browse + "/" + swname);
      }
    });
    table.addColumn(namecol, "Name");
    namecol.setSortable(true);
    sortHandler.setComparator(namecol, this.swcompare);
    table.getColumnSortList().push(namecol);
    
    // Edit Button Column
    final Column<SoftwareSummary, String> editcol = 
        new Column<SoftwareSummary, String>(
            new ButtonCell(IconType.EDIT, ButtonType.INFO, 
                ButtonSize.EXTRA_SMALL)) {
        @Override
        public String getValue(SoftwareSummary details) {
          return "Edit";
        }
    };
    editcol.setFieldUpdater(new FieldUpdater<SoftwareSummary, String>() {
      @Override
      public void update(int index, SoftwareSummary summary, String value) {
        String swname = summary.getName();
        History.newItem(NameTokens.publish + "/" + swname);
      }
    });
    table.setColumnWidth(editcol, "50px");
    table.addColumn(editcol);    
    
    // Delete Button Column
    deletecolumn = new Column<SoftwareSummary, String>(
            new ButtonCell(IconType.REMOVE, ButtonType.DANGER, ButtonSize.EXTRA_SMALL)) {
      @Override
      public String getValue(SoftwareSummary details) {
        return "Delete";
      }
    };
    deletecolumn.setFieldUpdater(new FieldUpdater<SoftwareSummary, String>() {
      @Override
      public void update(int index, SoftwareSummary summary, String value) {
        deleteSoftware(summary);
      }
    });
    table.setColumnWidth(deletecolumn, "0px");
    deletecolumn.setCellStyleNames("admin-cell");
    table.addColumn(deletecolumn);
    
    // Checkbox, software selection column (to select software to compare)
    checkboxcolumn = new Column<SoftwareSummary, Boolean>(new CheckboxCell(true, true)) {
        @Override
        public Boolean getValue(SoftwareSummary summary) {
          return selections.contains(summary);
        }
    };
    checkboxcolumn.setFieldUpdater(new FieldUpdater<SoftwareSummary, Boolean>() {
        @Override
        public void update(int index, SoftwareSummary summary, Boolean value) {
          if(value) 
            selections.add(summary);
          else
            selections.remove(summary);
          table.redrawRow(index);
          updateCompareButton();
        }
    });
    
    table.setColumnWidth(checkboxcolumn, "0px");
    checkboxcolumn.setCellStyleNames("selection-cell");
    table.addColumn(checkboxcolumn);
    
    // Set row styles
    table.setRowStyles(new RowStyles<SoftwareSummary>() {
      @Override
      public String getStyleNames(SoftwareSummary summary, int rowIndex) {
       if(selections.contains(summary))
         return "selected-row";
       return "";
      }
    });
    
    
    // Bind list & pager to table
    pager.setDisplay(table);
    listProvider.addDataDisplay(table);
  }
  
  private void deleteSoftware(final SoftwareSummary sw) {
    if (Window.confirm("Are you sure you want to delete the software ?")) {
      SoftwareREST.deleteSoftware(sw.getName(),
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

  
  @UiHandler("publishbutton")
  void onPublishButtonClick(ClickEvent event) {
    String label = softwarelabel.getValue();
    softwarelabel.setValue(null);
    if(label != null && !label.equals("")) {
      Software tmpsw = new Software();
      tmpsw.setLabel(label);
      SoftwareREST.publishSoftware(tmpsw, new Callback<Software, Throwable>() {
        public void onSuccess(Software sw) {
          // Add item to list
          SoftwareSummary newsw = new SoftwareSummary(sw);
          addToList(newsw);
          updateList();

          // Go to the new item
          History.newItem(NameTokens.publish + "/" + sw.getName());
        }
        @Override
        public void onFailure(Throwable exception) { }
      });
    }
  }
  
  @UiHandler("cancelbutton")
  void onCancelPublish(ClickEvent event) {
    softwarelabel.setValue(null);
    publishdialog.hide();
  }
  
  private void addToList(SoftwareSummary summary) {
    boolean contains = false;
    for(SoftwareSummary sum : allSoftwareList) {
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
  
  private void removeFromList(SoftwareSummary summary) {
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
    for(SoftwareSummary summary : allSoftwareList) {
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
    SoftwareREST.getSoftwareListFaceted(facets.getFacets(),
        new Callback<List<SoftwareSummary>, Throwable>() {
      @Override
      public void onSuccess(List<SoftwareSummary> list) {
        filteredSoftwareIdMap.clear();
        for(SoftwareSummary flist: list)
          filteredSoftwareIdMap.put(flist.getId(), true);
        updateList();
      }
      @Override
      public void onFailure(Throwable reason) { }
    });    
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
      for(SoftwareSummary summary : selections) {
        if(i > 0) idtext += ",";
        idtext += summary.getName();
        i++;
      }
      History.newItem(NameTokens.compare + "/" + idtext);
    }
  }
}
