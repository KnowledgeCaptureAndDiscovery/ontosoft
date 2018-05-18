package org.ontosoft.client.application.version.publish;

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
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.PageHeader;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.ontosoft.client.Config;
import org.ontosoft.client.application.ParameterizedViewImpl;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.chart.CategoryBarChart;
import org.ontosoft.client.components.chart.CategoryPieChart;
import org.ontosoft.client.components.chart.events.CategorySelectionEvent;
import org.ontosoft.client.components.form.SoftwareVersionForm;
import org.ontosoft.client.components.form.events.PluginResponseEvent;
import org.ontosoft.client.components.form.events.SoftwareVersionChangeEvent;
import org.ontosoft.client.components.form.notification.PluginNotifications;
import org.ontosoft.client.place.NameTokens;
import org.ontosoft.client.rest.AppNotification;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.client.rest.UserREST;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.entities.SoftwareVersion;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Agent;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.plugins.PluginResponse;
import org.ontosoft.shared.utils.PermUtils;

import com.github.gwtd3.api.D3;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class PublishVersionView extends ParameterizedViewImpl 
  implements PublishVersionPresenter.MyView {

  @UiField
  CategoryPieChart piechart;
  
  @UiField
  CategoryBarChart barchart;
  
  @UiField
  Column sidecolumn, piecolumn, barcolumn;
  
  @UiField
  Breadcrumbs breadcrumbs;

  @UiField
  SoftwareVersionForm softwareform;
  
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
  
  @UiField
  PageHeader title;
  
  SoftwareREST api = SoftwareREST.get(Config.getServerURL());

  Vocabulary vocabulary;
  String versionname;
  String softwarename;
  SoftwareVersion version;
  String loggedinuser;
  Software software;

  private String allusers = "All Users(*)";
  
  private ListDataProvider<Authorization> listProvider = 
	      new ListDataProvider<Authorization>();

  private Comparator<Authorization> metacompare;
  
  interface Binder extends UiBinder<Widget, PublishVersionView> { }

  @Inject
  public PublishVersionView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
    initVocabulary();
    initTable();
  }
  
  public static void setBrowserWindowTitle (String newTitle) {
    if (Document.get() != null) {
        Document.get().setTitle (newTitle + " - Admin - OntoSoft Portal");
    }
  }
  
  // If some parameters are passed in, initialize the software and interface
  public void initializeParameters(String[] params) {
    clear();
    UserSession session = SessionStorage.getSession();
    if(session == null) {
      loading.setVisible(false);
      //this.api.notifyFailure("You need to be logged in to edit software description");
      return;
    }
    
    if(session.getUsername() != null)
      if(!this.loggedinuser.equals(session.getUsername())) {
        this.loggedinuser = session.getUsername();
        this.api.clearSwCache();
      }
    
    // Parse tokens
    if(params.length > 0) {
      this.versionname = params[0];
      String pfx = KBConstants.CATNS();
      String piecat = params.length > 1 ? pfx+params[1] : null;
      String barcat = params.length > 2 ? pfx+params[2] : null;

      piechart.setActiveCategoryId(piecat, false);
      barchart.setActiveCategoryId(barcat, false);

      initSoftware(this.versionname);
    }
    else {
      version = null; 
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
        if(version.getPermission().ownernameExists(auth.getAgentName())) {
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
    ArrayList<Authorization> authorizations = new ArrayList<Authorization>(this.version.getPermission().getAuthorizations().values());
    ArrayList<Authorization> authlist = new ArrayList<Authorization>();
    HashSet<String> permusers = new HashSet<String>();
	
    for (Iterator<Authorization> iter = authorizations.listIterator(); iter.hasNext(); ) {
      Authorization auth = iter.next();
      if (!permusers.contains(auth.getAgentName()) && 
        auth.getAccessMode().getMode().equals("Write") &&
        auth.getAccessToObjId().equals(this.version.getId()) &&
        !auth.getAgentName().equals("*")) {
        authlist.add(auth);
        permusers.add(auth.getAgentName());
      }
    }
	
    for (Agent owner:version.getPermission().getOwners()) {
      if (!permusers.contains(owner.getName())) {
        permusers.add(owner.getName());
			
        Authorization auth = new Authorization();
        auth.setId("");
        auth.setAgentId("");
        auth.setAccessToObjId(version.getId());
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
    this.api.getVocabulary(new Callback<Vocabulary, Throwable>() {
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
    this.api.getPermissionFeatureEnabled(new Callback<Boolean, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        permbutton.setVisible(false);
      }

      @Override
      public void onSuccess(Boolean permEnabled) {
        UserSession session = SessionStorage.getSession();
    	      
    	if(permEnabled && 
          ((session != null && session.getRoles().contains("admin")) || 
    	  version.getPermission().ownernameExists(loggedinuser))) {
    	  permbutton.setVisible(true);
        }
      }
    });	  
  }
  
  private void initSoftware(String versionname, final boolean reload) {    
    if(!reload)
      loading.setVisible(true);
    else
      reloadbutton.setIconSpin(true);
    
    String[] swnames = versionname.split("\\s*:\\s*");
    softwarename = swnames[0];
    versionname = swnames[1];
    this.versionname = versionname;
    
    this.api.getSoftwareVersion(softwarename, versionname, 
        new Callback<SoftwareVersion, Throwable>() {
      @Override
      public void onSuccess(SoftwareVersion sw) {
        reloadbutton.setIconSpin(false);
        loading.setVisible(false);
        savebutton.setEnabled(sw.isDirty());
        
        version = sw;
        initialDraw();
        
        setPermButtonVisibility();
        
        notifications.showNotificationsForSoftwareVersion(version.getId());
        String swlabel = version.getLabel();
        permissiondialog.setTitle("Set Permissions for " + swlabel.substring(0, 1).toUpperCase() + swlabel.substring(1));
      }
      @Override
      public void onFailure(Throwable reason) {
        reloadbutton.setIconSpin(false);
        loading.setVisible(false);
      }
    }, reload);
    api.getSoftware(softwarename, new Callback<Software, Throwable>() {
        @Override
        public void onSuccess(Software sw) {
        	software = sw;
        }
        @Override
        public void onFailure(Throwable exception) {
          GWT.log("Error fetching Software", exception);
        }
      }, false);
  }
  
  private void setBrowsePermissionHeader() {
    String perm_header = "Default Permission: ";
    String mode = PermUtils.getAccessLevelForUser(this.version, "*", this.version.getId());
    if (mode.equals("Write"))
      perm_header += "Write";
    else
      perm_header += "Read";
    
    title.setSubText(perm_header);
  }
  
  private void initialDraw() {
    if(this.vocabulary == null || this.version == null)
      return;

    piechart.setVocabulary(vocabulary);
    barchart.setVocabulary(vocabulary);
    softwareform.setVocabulary(vocabulary);
    
    piechart.setSoftware(version);
    barchart.setSoftware(version);
    softwareform.setSoftwareVersion(version);
    
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
      History.replaceItem(NameTokens.publishversion + "/" + softwarename + ":" + versionname + "/" + pcat.getName(), false);
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
      History.replaceItem(NameTokens.publishversion + "/" + softwarename + ":" + versionname + "/"
            + pcat.getName() + "/" + bcat.getName() , false);
      barCategorySelected(bcat.getId());
    }
    setBreadCrumbs();
  }

  @UiHandler("savebutton")
  public void onSave(ClickEvent event) {
    final SoftwareVersion tmpsw = softwareform.getSoftwareVersion();
    tmpsw.setName(versionname);
    //savebutton.state().loading();
    
    this.api.updateSoftwareVersion(softwarename, tmpsw, new Callback<SoftwareVersion, Throwable>() {
      @Override
      public void onSuccess(SoftwareVersion sw) {
        version = sw;
        //softwarename = tmpsw.getName();
        piechart.setSoftware(version);
        barchart.setSoftware(version);
        softwareform.setSoftwareVersion(version);
        
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
    initSoftware(versionname, true);
    //History.replaceItem(History.getToken(), false);
  }
  
  @UiHandler("softwareform")
  void onSoftwareChange(SoftwareVersionChangeEvent event) {
    version = event.getSoftwareVersion();
    version.setDirty(true);
    savebutton.setEnabled(true);
    versionname = version.getName();
    piechart.setSoftware(version);
    barchart.setSoftware(version);
    softwareform.setSoftwareVersion(version);
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
    
    //SoftwareVersion version = piechart.getSoftware().get
    
    String swlabel = piechart.getSoftware().getLabel();
    setBrowserWindowTitle(swlabel);
    String swname = piechart.getSoftware().getSoftwareName();
    if (swname != null)
      swlabel = swname;
    else if (swlabel == null)
      swlabel = piechart.getSoftware().getName();
    
    
    
    AnchorListItem anchor1 = new AnchorListItem(software.getLabel());
    anchor1.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(NameTokens.publish + "/" + softwarename);
      }
    });
    anchor1.setStyleName("first-crumb");
    breadcrumbs.add(anchor1);
    
    AnchorListItem anchor = new AnchorListItem(swlabel);
    anchor.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        History.newItem(NameTokens.version + "/" + softwarename + ":" + versionname);
      }
    });
    //anchor.setStyleName("first-crumb");
    breadcrumbs.add(anchor);

    if(piechart != null && piechart.getSoftware() != null) {
      anchor = new AnchorListItem("Edit");
      anchor.addClickHandler(new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          History.replaceItem(NameTokens.publishversion + "/" + softwarename + ":" + versionname, false);
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
    
    setBrowsePermissionHeader();
    
    Window.scrollTo(0, 0);
  }
  
  @UiHandler("userlist")
  void onUserChangedEvent(ValueChangeEvent<String> event) {
    permlist.setEnabled(true);
    permlist.refresh();
    setpermbutton.setEnabled(true);
    String newuser = userlist.getValue();
    selectPermissionForUser(newuser);
    if(newuser.equals(allusers))
    	ownerrole.setEnabled(false);
    else
    	ownerrole.setEnabled(true);
  }
  
  private void selectAccessLevel(String accesslevel) {
    permlist.setValue(accesslevel);
  }
  
  private void selectPermissionForUser(String name) {	  
  	permlist.setEnabled(true);
  	ownerrole.setEnabled(true);
  	setpermbutton.setEnabled(true);
  	ownerrole.setValue(false);
  	
  	final String username = name.equals(allusers) ? "*" :name;
  	
    UserREST.getUserRoles(username, new Callback<List<String>, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        AppNotification.notifyFailure(reason.getMessage());
      }

      @Override
      public void onSuccess(List<String> roles) {
        if (roles!= null && roles.contains("admin")) {
          permlist.setEnabled(false);
          setpermbutton.setEnabled(false);
          ownerrole.setEnabled(false);
          ownerrole.setValue(true);
          selectAccessLevel("Write");
          permlist.refresh();
        } else if (version.getPermission().ownernameExists(username)) {
          ownerrole.setValue(true);
          selectAccessLevel("Write");
          permlist.setEnabled(false);
          permlist.refresh();
        } else {
          api.getSoftwareAccessLevelForUser(version.getName(), 
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
    	Option opt = new Option();
    	opt.setText(allusers);
    	userlist.add(opt);
    	
        for(String name : list) {
          opt = new Option();
          opt.setText(name);
          opt.setValue(name);
          userlist.add(opt);
        }
        userlist.refresh();
      }
    });
  }
  
  private void setPermissionList() {
    this.api.getPermissionTypes(new Callback<List<String>, Throwable>() {
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
    String name = userlist.getValue();
    final String username = name.equals(allusers) ? "*" :name;
    final String permtype = permlist.getValue();
    UserSession session = SessionStorage.getSession();

    if ((session != null && session.getRoles().contains("admin")) || 
      version.getPermission().ownernameExists(this.loggedinuser)) {
      if (ownerrole.getValue() == true) {
        this.api.addSoftwareOwner(version.getName(), username, 
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
            if (username.equals("*")) {
              version.getPermission().removeAllOwners();
            }
            version.getPermission().addOwnerid(username);
          }
        });
      } else {
        final Authorization authorization = new Authorization();
        authorization.setId("");
        authorization.setAgentId("");
        authorization.setAccessToObjId(version.getId());
        authorization.setAgentName(username);
        AccessMode mode = new AccessMode();
        mode.setMode(permtype);
        authorization.setAccessMode(mode);

        this.api.setSoftwarePermissionForUser(version.getName(), authorization, 
          new Callback<Boolean, Throwable>() {
            @Override
            public void onFailure(Throwable reason) {
              permissiondialog.hide();
              AppNotification.notifyFailure(reason.getMessage());
            }
            
            @Override
            public void onSuccess(Boolean success) {
              if (username.equals("*")) {
                version.getPermission().removeAuthsHavingTarget(authorization.getAccessToObjId());
              }
              version.getPermission().addOrUpdateAuth(authorization);
              
              api.removeSoftwareOwner(version.getName(), username, 
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
                 version.getPermission().removeOwnerid(username);
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
