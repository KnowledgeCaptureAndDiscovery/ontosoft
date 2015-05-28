package org.earthcube.geosoft.client.application.community;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.earthcube.geosoft.client.application.ParameterizedViewImpl;
import org.earthcube.geosoft.client.rest.SoftwareREST;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataEnumeration;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.gwt.CellTable;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class CommunityView extends ParameterizedViewImpl 
  implements CommunityPresenter.MyView {

  Vocabulary vocabulary;
  
  @UiField(provided = true)
  CellTable<MetadataEnumeration> table = new CellTable<MetadataEnumeration>(40);

  @UiField
  SimplePager pager;
  
  private ListDataProvider<MetadataEnumeration> listProvider = 
      new ListDataProvider<MetadataEnumeration>();
  
  private Comparator<MetadataEnumeration> metacompare;
  
  interface Binder extends UiBinder<Widget, CommunityView> {
  }

  @Inject
  public CommunityView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    initTable();
    initAgents();
  }

  @Override
  public void initializeParameters(String[] parameters) {
  }
  
  private void initVocabulary() {
    SoftwareREST.getVocabulary(new Callback<Vocabulary, Throwable>() {
      @Override
      public void onSuccess(Vocabulary vocab) {
        vocabulary = vocab;
      }
      @Override
      public void onFailure(Throwable reason) {
        GWT.log("Error fetching Vocabulary", reason);
      }
    }, false);
  }

  private void initTable() {
      ListHandler<MetadataEnumeration> sortHandler =
          new ListHandler<MetadataEnumeration>(listProvider.getList());

      table.addColumnSortHandler(sortHandler);
      table.setEmptyTableWidget(new Label("No Authors found.."));
      
      this.metacompare = new Comparator<MetadataEnumeration>() {
        @Override
        public int compare(MetadataEnumeration e1, MetadataEnumeration e2) {
          if(e1.getLabel() != null && e2.getLabel() != null)
            return e1.getLabel().compareToIgnoreCase(e2.getLabel());
          return 0;
        }
      };
      
      // Name Column
      TextColumn<MetadataEnumeration> namecol = 
          new TextColumn<MetadataEnumeration>() {
        @Override
        public String getValue(MetadataEnumeration menum) {
          return menum.getLabel();
        }
      };
      table.addColumn(namecol, "Author Name");
      namecol.setSortable(true);
      sortHandler.setComparator(namecol, this.metacompare);
      table.getColumnSortList().push(namecol);    
      
      listProvider.addDataDisplay(table);
      pager.setDisplay(table);
  }
    
  private void initAgents() {
    SoftwareREST.getEnumerationsForType(KBConstants.PROVNS() + "Agent", 
        new Callback<List<MetadataEnumeration>, Throwable>() {
      @Override
      public void onSuccess(List<MetadataEnumeration> list) {
        Collections.sort(list, metacompare);
        listProvider.getList().clear();
        listProvider.getList().addAll(list);
        listProvider.flush();

        initMaterial();
        Window.scrollTo(0, 0);        
      }
      @Override
      public void onFailure(Throwable reason) { }
    });
  }
}
