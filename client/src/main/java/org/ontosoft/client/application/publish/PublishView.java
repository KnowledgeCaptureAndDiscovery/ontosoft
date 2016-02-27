package org.ontosoft.client.application.publish;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Breadcrumbs;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Label;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.chart.CategoryBarChart;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.components.chart.events.CategorySelectionEvent;
import org.ontosoft.client.components.form.SoftwareForm;
import org.ontosoft.client.components.form.events.PluginResponseEvent;
import org.ontosoft.client.components.form.events.SoftwareChangeEvent;
import org.ontosoft.client.components.form.notification.PluginNotifications;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.AppNotification;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.client.rest.UserREST;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Agent;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;

import com.github.gwtd3.api.D3;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
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
  Button savebutton, reloadbutton, permbutton;
  
  @UiField
  Button setpermbutton;
  
  @UiField
  VerticalPanel loading;
  
  @UiField
  Heading heading;
  
  @UiField
  PluginNotifications notifications;
  
  @UiField
  Select userlist, permlist;
  
  @UiField
  Modal permissiondialog;
  
  @UiField
  CheckBox ownerrole;
  
  @UiField(provided = true)
  CellTable<Authorization> table = new CellTable<Authorization>(40);

  @UiField
  SimplePager pager;
  
  Vocabulary vocabulary;
  String softwarename;
  Software software;
  String loggedinuser;

  private ListDataProvider<Authorization> listProvider = 
	      new ListDataProvider<Authorization>();

  private Comparator<Authorization> metacompare;
  
  interface Binder extends UiBinder<Widget, PublishView> { }

  @Inject
  public PublishView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    initTable();
  }
  
  // If some parameters are passed in, initialize the software and interface
  public void initializeParameters(String[] params) {
    clear();
    UserSession session = SessionStorage.getSession();
    if(session == null) {
      loading.setVisible(false);
      //SoftwareREST.notifyFailure("You need to be logged in to edit software description");
      return;
    }
    
    if(session.getUsername() != null)
      if(!this.loggedinuser.equals(session.getUsername())) {
        this.loggedinuser = session.getUsername();
        SoftwareREST.clearSwCache();
      }
    
    // Parse tokens
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
      software = null; 
    }
  }
  
  private void initTable() {
    ListHandler<Authorization> sortHandler =
      new ListHandler<Authorization>(listProvider.getList());	  
    table.addColumnSortHandler(sortHandler);
    table.setEmptyTableWidget(new Label("No Permissions found.."));
 
    this.metacompare = new Comparator<Authorization>() {
      @Override
      public int compare(Authorization auth1, Authorization auth2) {
        if(auth1.getAgentName() != null && auth2.getAgentName() != null)
          return auth1.getAgentName().compareToIgnoreCase(auth2.getAgentName());
        return 0;
      }
    };
      
    // Name Column
    TextColumn<Authorization> namecol = 
      new TextColumn<Authorization>() {
        @Override
        public String getValue(Authorization menum) {
          return menum.getAgentName();
      }
    };
    table.addColumn(namecol, "Username");
    namecol.setSortable(true);
    sortHandler.setComparator(namecol, this.metacompare);
    table.getColumnSortList().push(namecol);    

    TextColumn<Authorization> permcolumn = 
      new TextColumn<Authorization>() {
      @Override
      public String getValue(Authorization auth) {
        return "";
      }
    	      
      @Override
      public void render(Cell.Context context, Authorization auth, SafeHtmlBuilder sb) {
        if(auth.getAccessMode().getMode().equals("Write")) {
          sb.appendHtmlConstant("<i class=\"fa fa-check\"></i>");
        } else {
          sb.appendHtmlConstant("<i class=\"fa fa-times\"></i>");
        }
      }
    };
    
    permcolumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    table.addColumn(permcolumn, "Write");

    TextColumn<Authorization> isownercolumn = 
      new TextColumn<Authorization>() {
      @Override
      public String getValue(Authorization auth) {
        return "";
      }
	      
      @Override
      public void render(Cell.Context context, Authorization auth, SafeHtmlBuilder sb) {
        if(software.getPermission().ownernameExists(auth.getAgentName())) {
          sb.appendHtmlConstant("<i class=\"fa fa-check fa-1\"></i>");
        } else {
          sb.appendHtmlConstant("<i class=\"fa fa-times fa-1\"></i>");
        }
      }
    };
    	    
    isownercolumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
    table.addColumn(isownercolumn, "Owner");
    
    table.setWidth("100%", true);
    table.setColumnWidth(permcolumn, 10.0, Unit.PCT);
    table.setColumnWidth(isownercolumn, 10.0, Unit.PCT);
      
    listProvider.addDataDisplay(table);
    pager.setDisplay(table);
  }
  
  private void initAgents() {
    ArrayList<Authorization> authorizations = new ArrayList<Authorization>(this.software.getPermission().getAuthorizations().values());
    ArrayList<Authorization> authlist = new ArrayList<Authorization>();
    HashSet<String> permusers = new HashSet<String>();
	
    for (Iterator<Authorization> iter = authorizations.listIterator(); iter.hasNext(); ) {
      Authorization auth = iter.next();
      if (!permusers.contains(auth.getAgentName()) && 
        auth.getAccessMode().getMode().equals("Write") &&
        auth.getAccessToObjId().equals(this.software.getId())) {
        authlist.add(auth);
        permusers.add(auth.getAgentName());
      }
    }
	
    for (Agent owner:software.getPermission().getOwners()) {
      if (!permusers.contains(owner.getName())) {
        permusers.add(owner.getName());
			
        Authorization auth = new Authorization();
        auth.setId("");
        auth.setAgentId("");
        auth.setAccessToObjId(software.getId());
        auth.setAgentName(owner.getName());
        AccessMode mode = new AccessMode();
        mode.setMode("Write");
        auth.setAccessMode(mode);

        authlist.add(auth);
      }
    }
	
    Collections.sort(authlist, metacompare);
    listProvider.getList().clear();
    listProvider.getList().addAll(authlist);
    listProvider.flush();

    initMaterial();
    Window.scrollTo(0, 0);        
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
    permbutton.setVisible(false);
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
  
  private void setPermButtonVisibility() {
    UserSession session = SessionStorage.getSession();
      
    if ((session != null && session.getRoles().contains("admin")) || 
      this.software.getPermission().ownernameExists(this.loggedinuser)) {
      permbutton.setVisible(true);
    }	  
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
        
        setPermButtonVisibility();
        
        notifications.showNotificationsForSoftware(software.getId());
        String swlabel = software.getLabel();
        permissiondialog.setTitle("Set Permissions for " + swlabel.substring(0, 1).toUpperCase() + swlabel.substring(1));
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
    
    setPermButtonVisibility();
    
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
    //History.replaceItem(History.getToken(), false);
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
    
    String swlabel = piechart.getSoftware().getLabel();
    if (swlabel == null)
      swlabel = piechart.getSoftware().getName();
    AnchorListItem anchor = new AnchorListItem(swlabel);
    anchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(NameTokens.browse + "/" + softwarename);
      }
    });
    anchor.setStyleName("first-crumb");
    breadcrumbs.add(anchor);

    if(piechart != null && piechart.getSoftware() != null) {
      anchor = new AnchorListItem("Edit");
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          History.replaceItem(NameTokens.publish + "/" + softwarename, false);
          clear();
          initialDraw();
          //initSoftware(softwarename);
        }
      });
      anchor.setStyleName("");
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
  
  @UiHandler("permbutton")
  void onPermButtonClick(ClickEvent event) {
    permissiondialog.show();
    
    userlist.setVisible(true);
    permlist.setVisible(true);
    
    if (userlist.getItemCount() == 1)
      setUserList();
    if (permlist.getItemCount() == 1)
      setPermissionList();
    
    initAgents();
    initMaterial();
    
    Window.scrollTo(0, 0);
  }
  
  @UiHandler("userlist")
  void onUserChangedEvent(ChangeEvent event) {
    permlist.setEnabled(true);
    permlist.refresh();
    setpermbutton.setEnabled(true);
    String newuser = userlist.getSelectedValue();
    selectPermissionForUser(newuser);
  }
  
  private void selectAccessLevel(String accesslevel) {
    for (int i = 1; i < permlist.getItemCount(); i++) {
      if (permlist.getValue(i).equals(accesslevel)) {
        permlist.setValue(accesslevel);
        break;
      }
    }
  }
  
  private void selectPermissionForUser(final String username) {	  
	permlist.setEnabled(true);
	ownerrole.setEnabled(true);
	setpermbutton.setEnabled(true);
	ownerrole.setValue(false);
	
    UserREST.getUserRoles(username, new Callback<List<String>, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure(reason.getMessage());
      }

      @Override
      public void onSuccess(List<String> roles) {
        if (roles.contains("admin")) {
          permlist.setEnabled(false);
          setpermbutton.setEnabled(false);
          ownerrole.setEnabled(false);
          ownerrole.setValue(true);
          selectAccessLevel("Write");
          permlist.refresh();
        } else if (software.getPermission().ownernameExists(username)) {
          ownerrole.setValue(true);
          selectAccessLevel("Write");
          permlist.setEnabled(false);
          permlist.refresh();
        } else {
          SoftwareREST.getSoftwareAccessLevelForUser(software.getName(), 
            username, new Callback<AccessMode, Throwable>() {
              @Override
              public void onFailure(Throwable reason) {
                AppNotification.notifyFailure(reason.getMessage());
              }

              @Override
              public void onSuccess(AccessMode accessmode) {
                selectAccessLevel(accessmode.getMode());
              }
          });		  
        }	        	
      }
    }); 
    permlist.refresh();
  }
  
  private void setUserList() {
    UserREST.getUsers(new Callback<List<String>, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure(reason.getMessage());
      }

      @Override
      public void onSuccess(List<String> list) {
        for(String name : list) {
          Option opt = new Option();
          opt.setText(name);
          opt.setValue(name);
          userlist.add(opt);
        }
        userlist.refresh();
      }
    });
  }
  
  private void setPermissionList() {
    SoftwareREST.getPermissionTypes(new Callback<List<String>, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure(reason.getMessage());
      }

      @Override
      public void onSuccess(List<String> list) {
        for(String name : list) {
          Option opt = new Option();
          opt.setText(name);
          opt.setValue(name);
          permlist.add(opt);
        }
        permlist.refresh();
      }
    });	  
  }
  
  @UiHandler("setpermbutton")
  void onSetPermissionButtonClick(ClickEvent event) {
    submitPermissionForm();
    event.stopPropagation();
  }
  
  @UiHandler("ownerrole")
  public void onSetOwnerClicked(ValueChangeEvent<Boolean> ev) {
	  boolean state = ev.getValue();
	  if (state) {
		selectAccessLevel("Write");
        permlist.setEnabled(false);
	  } else {
        permlist.setEnabled(true);
	  }
	  permlist.refresh();
  }
  
  private void submitPermissionForm() {
    final String username = userlist.getSelectedValue();
    final String permtype = permlist.getSelectedValue();
    UserSession session = SessionStorage.getSession();

    if ((session != null && session.getRoles().contains("admin")) || 
      software.getPermission().ownernameExists(this.loggedinuser)) {
      if (ownerrole.getValue() == true) {
        SoftwareREST.addSoftwareOwner(software.getName(), username, 
          new Callback<Boolean, Throwable>() {
          @Override
          public void onFailure(Throwable reason) {
            permissiondialog.hide();
            AppNotification.notifyFailure(reason.getMessage());
          }
                  
          @Override
          public void onSuccess(Boolean success) {
            permissiondialog.hide();
            AppNotification.notifySuccess("Owner Added!", 2000);
            software.getPermission().addOwnerid(username);
          }
        });
      } else {
        final Authorization authorization = new Authorization();
        authorization.setId("");
        authorization.setAgentId("");
        authorization.setAccessToObjId(software.getId());
        authorization.setAgentName(username);
        AccessMode mode = new AccessMode();
        mode.setMode(permtype);
        authorization.setAccessMode(mode);

        SoftwareREST.setSoftwarePermissionForUser(software.getName(), authorization, 
          new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
              permissiondialog.hide();
              AppNotification.notifyFailure(reason.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean success) {
              software.getPermission().addOrUpdateAuth(authorization);
              
              SoftwareREST.removeSoftwareOwner(software.getName(), username, 
                new Callback<Boolean, Throwable>() {
               @Override
               public void onFailure(Throwable reason) {
                 permissiondialog.hide();
                 AppNotification.notifyFailure(reason.getMessage());
               }
                
               @Override
               public void onSuccess(Boolean success) {
                 permissiondialog.hide();
                 AppNotification.notifySuccess("Permission updated!", 2000);
                 software.getPermission().removeOwnerid(username);
               }
             });
           }
        });  
      }
    } else {
      AppNotification.notifyFailure("Not Allowed!");
    }
  }
  
  @UiHandler("cancelbutton")
  void onCancelButtonClick(ClickEvent event) {
    permissiondialog.hide();
    event.stopPropagation();
  }
}
