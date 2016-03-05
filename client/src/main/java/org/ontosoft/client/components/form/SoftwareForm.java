package org.ontosoft.client.components.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.TabPane;
import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.components.form.events.HasPluginHandlers;
import org.ontosoft.client.components.form.events.HasSoftwareHandlers;
import org.ontosoft.client.components.form.events.PluginResponseEvent;
import org.ontosoft.client.components.form.events.PluginResponseHandler;
import org.ontosoft.client.components.form.events.SoftwareChangeEvent;
import org.ontosoft.client.components.form.events.SoftwareChangeHandler;
import org.ontosoft.client.components.form.events.SoftwareSaveEvent;
import org.ontosoft.client.components.form.events.SoftwareSaveHandler;
import org.ontosoft.client.components.form.formgroup.PropertyFormGroup;
import org.ontosoft.client.components.form.formgroup.input.EntityInput;
import org.ontosoft.client.components.form.formgroup.input.EntityRegistrar;
import org.ontosoft.client.rest.AppNotification;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.client.rest.UserREST;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.permission.AccessMode;
import org.ontosoft.shared.classes.permission.Agent;
import org.ontosoft.shared.classes.permission.Authorization;
import org.ontosoft.shared.classes.users.UserSession;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataCategory;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.MetadataType;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;
import org.ontosoft.shared.utils.PermUtils;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class SoftwareForm extends Composite 
implements HasSoftwareHandlers, HasPluginHandlers {
  private HandlerManager handlerManager;

  @UiField
  Form form;
  
  @UiField
  TabPane requiredtab, optionaltab;
  
  @UiField
  TabListItem requiredtabitem, optionaltabitem;
  
  @UiField
  Button setpermbutton;
  
  @UiField
  Select userlist, permlist;
  
  @UiField
  Modal permissiondialog;
  
  @UiField(provided = true)
  CellTable<Authorization> table = new CellTable<Authorization>(40);

  @UiField
  SimplePager pager;
  
  Vocabulary vocabulary;
  Software software;
  String propidselected;
  
  Map<String, PropertyFormGroup> propeditors;
  
  interface Binder extends UiBinder<Widget, SoftwareForm> { };
  private static Binder uiBinder = 
      GWT.create(Binder.class);
  private ListDataProvider<Authorization> listProvider = 
	      new ListDataProvider<Authorization>();

  private Comparator<Authorization> metacompare;
  
  public SoftwareForm() {
    initWidget(uiBinder.createAndBindUi(this));
    propeditors = new HashMap<String, PropertyFormGroup>();
    handlerManager = new HandlerManager(this);
    initTable();
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
          
    table.setWidth("100%", true);
    table.setColumnWidth(permcolumn, 10.0, Unit.PCT);
      
    listProvider.addDataDisplay(table);
    pager.setDisplay(table);
  }
  
  @Override
  public void fireEvent(GwtEvent<?> event) {
    handlerManager.fireEvent(event);
  }

  @Override
  public HandlerRegistration addSoftwareSaveHandler(
      SoftwareSaveHandler handler) {
    return handlerManager.addHandler(SoftwareSaveEvent.TYPE, handler);
  }
  
  @Override
  public HandlerRegistration addSoftwareChangeHandler(
      SoftwareChangeHandler handler) {
    return handlerManager.addHandler(SoftwareChangeEvent.TYPE, handler);
  }
  
  @Override
  public HandlerRegistration addPluginResponseHandler(
      PluginResponseHandler handler) {
    return handlerManager.addHandler(PluginResponseEvent.TYPE, handler);
  }
  
  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public void setVocabulary(Vocabulary vocabulary) {
    this.vocabulary = vocabulary;
    if(this.vocabulary != null) {
      registerEntities();
      registerEntityInputs();
    }
  }

  public Software getSoftware() {
    return software;
  }

  public void setSoftware(Software software) {
    this.software = software;
  }
  
  public void setRawPropertyValues(String propid, List<Object> values) {
    PropertyFormGroup ed = propeditors.get(propid);
    ed.setRawValue(values);
    software.addPropertyValues(propid, ed.getValue());
    fireEvent(new SoftwareChangeEvent(software));    
  }
  
  public void setPropertyValues(String propid, List<Entity> entities) {
    PropertyFormGroup ed = propeditors.get(propid);
    ed.setValue(entities);
    software.addPropertyValues(propid, ed.getValue());
    fireEvent(new SoftwareChangeEvent(software));    
  }
  
  private void registerEntityInputs() {
    final String pkg = EntityInput.class.getName().replaceAll("(.*\\.).*?$", "$1");
    for(MetadataType type : vocabulary.getTypes().values()) {
      ArrayList<String> queue = new ArrayList<String>();
      //GWT.log("-------- "+type.getName());
      queue.add(type.getId());
      boolean registered = false;
      while(!queue.isEmpty()) {
        MetadataType qtype = vocabulary.getType(queue.remove(0));
        if(qtype != null) {
          String qInputWidgetClass = pkg + qtype.getName() + "Input";
          //GWT.log("Checking for "+qtype.getName()+ " = "+qInputWidgetClass);
          if(EntityRegistrar.registerInputClass(type.getId(), qInputWidgetClass)) {
            registered = true;
            break;
          }
          queue.add(qtype.getParent());
        }
      }
      if(!registered)
        GWT.log("** Cannot find adapter for "+type.getId());
    }
  }
  
  private void registerEntities() {
    final String pkg = Entity.class.getName().replaceAll("(.*\\.).*?$", "$1");
    for(MetadataType type : vocabulary.getTypes().values()) {
      ArrayList<String> queue = new ArrayList<String>();
      //GWT.log("-------- "+type.getName());
      queue.add(type.getId());
      boolean registered = false;
      while(!queue.isEmpty()) {
        MetadataType qtype = vocabulary.getType(queue.remove(0));
        if(qtype != null) {
          String qInputWidgetClass = pkg + qtype.getName();
          //GWT.log("Checking for "+qtype.getName()+ " = "+qInputWidgetClass);
          if(EntityRegistrar.registerEntityClass(type.getId(), qInputWidgetClass)) {
            registered = true;
            break;
          }
          queue.add(qtype.getParent());
        }
      }
      if(!registered)
        GWT.log("** Cannot find adapter for "+type.getId());
    }
  }

  public void showCategoryItems(String categoryId) {
    MetadataCategory mcat = vocabulary.getCategory(categoryId);
    if(mcat == null)
      return;
    
    for(PropertyFormGroup editor : propeditors.values())
      editor.setVisible(false);

    boolean hasOptional = false;
    boolean hasRequired = false;
    for(MetadataProperty prop: vocabulary.getPropertiesInCategory(mcat)) {
      if(propeditors.containsKey(prop.getId())) {
        if(!prop.isRequired())
          hasOptional=true;
        else
          hasRequired=true;        
        propeditors.get(prop.getId()).setVisible(true);
      }
    }
    
    optionaltab.setVisible(hasOptional);
    optionaltabitem.setVisible(hasOptional);
    
    requiredtab.setVisible(hasRequired);
    requiredtabitem.setVisible(hasRequired);
    
    requiredtab.setActive(hasRequired);
    requiredtabitem.setActive(hasRequired);
    optionaltab.setActive(!hasRequired && hasOptional);
    optionaltabitem.setActive(!hasRequired && hasOptional);
    
  }
  
  public void createFormItems() {
    if(this.vocabulary == null)
      return;
    
    propeditors.clear();
    requiredtab.clear();
    optionaltab.clear();
    
    MetadataType swtype = vocabulary.getType(software.getType());
    List<MetadataProperty> swprops = vocabulary.getPropertiesForType(swtype);
    swprops = vocabulary.filterUneditableProperties(swprops);    
    swprops = vocabulary.orderProperties(swprops);

    for(final MetadataProperty mprop : swprops) {
      PropertyFormGroup editor = new PropertyFormGroup(mprop, this);
      editor.addPluginResponseHandler(new PluginResponseHandler() {
        @Override
        public void onPluginResponse(PluginResponseEvent event) {
          fireEvent(event);
        }
      });
      if(mprop.isRequired())
        requiredtab.add(editor);
      else
        optionaltab.add(editor);
      propeditors.put(mprop.getId(), editor);
    }
  }
  
  @UiHandler("optionaltabitem")
  public void onClickOptionalTab(ClickEvent event) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        layout();
      }
    });
  }
  
  @UiHandler("requiredtabitem")
  public void onClickRequiredTab(ClickEvent event) {
    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
      public void execute() {
        layout();
      }
    });
  }

  public void layout() {
    for(PropertyFormGroup ed : propeditors.values())
      ed.layout();
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
    if(visible)
      this.layout();
  }

  public void initPermDialog(String propid) {
    this.propidselected = propid;
    
    permissiondialog.show();
    userlist.setVisible(true);
    permlist.setVisible(true);
    
    if (permlist.getItemCount() == 1)
      setPermissionList();
    
    if (userlist.getItemCount() == 1)
      setUserList();
    
    initAgents();
    
    String newuser = userlist.getValue();
    if (newuser != null && !newuser.equals(""))
      selectPermissionForUser(newuser);
  }
  
  private void initAgents() {
    ArrayList<Authorization> authorizations = new ArrayList<Authorization>(this.software.getPermission().getAuthorizations().values());
    ArrayList<Authorization> authlist = new ArrayList<Authorization>();
    HashSet<String> permusers = new HashSet<String>();

    String propid = KBConstants.ONTNS() + this.propidselected;
	
    for (Iterator<Authorization> iter = authorizations.listIterator(); iter.hasNext(); ) {
      Authorization auth = iter.next();
      if (!permusers.contains(auth.getAgentName()) &&
        auth.getAccessMode().getMode().equals("Write") &&
        (auth.getAccessToObjId().equals(propid) || auth.getAccessToObjId().equals(software.getId()))) {
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
        auth.setAccessToObjId(propid);
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

    Window.scrollTo(0, 0);        
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

  private void selectAccessLevel(String accesslevel) {
    permlist.setValue(accesslevel);
  }
  
  private void selectPermissionForUser(final String username) {	 
	permlist.setEnabled(true);
	setpermbutton.setEnabled(true);
	
	if (software.getPermission().ownernameExists(username) ||
      PermUtils.getAccessLevelForUser(software.getPermission(), username, software.getId()).equals("Write")) {
      selectAccessLevel("Write");
      permlist.setEnabled(false);
      setpermbutton.setEnabled(false);		      	
    } else {
      UserREST.getUserRoles(username, new Callback<List<String>, Throwable>() {
        @Override
        public void onFailure(Throwable reason) {
          AppNotification.notifyFailure(reason.getMessage());
        }

        @Override
        public void onSuccess(List<String> roles) {
          if (roles.contains("admin")) {
            selectAccessLevel("Write");  
            permlist.setEnabled(false);
            setpermbutton.setEnabled(false);
            permlist.refresh();
          } else {
            SoftwareREST.getPropertyAccessLevelForUser(software.getName(), propidselected, 
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
    }
	permlist.refresh();
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
  
  @UiHandler("userlist")
  void onUserChangedEvent(ValueChangeEvent<String> event) {
    permlist.setEnabled(true);
    permlist.refresh();
    setpermbutton.setEnabled(true);
    
    String newuser = userlist.getValue();
    selectPermissionForUser(newuser);
  }
  
  @UiHandler("setpermbutton")
  void onSetPermissionButtonClick(ClickEvent event) {
    submitPermissionForm();
    event.stopPropagation();
  }
  
  private void submitPermissionForm() {
    final String username = userlist.getValue();
    final String permtype = permlist.getValue();
    
    UserSession session = SessionStorage.getSession();
    if (session != null) {
      String loggedinuser = session.getUsername();
      
      if (software.getPermission().ownernameExists(loggedinuser) ||
        session.getRoles().contains("admin")) {
        final Authorization authorization = new Authorization();
        authorization.setId("");
        authorization.setAgentId("");
        
        String propid = KBConstants.ONTNS() + this.propidselected;
        authorization.setAccessToObjId(propid);
        
        authorization.setAgentName(username);
        
        AccessMode mode = new AccessMode();
        mode.setMode(permtype);
        authorization.setAccessMode(mode);

        SoftwareREST.setPropertyPermissionForUser(software.getName(), authorization, 
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
            software.getPermission().addOrUpdateAuth(authorization);
          }
        });
      } else {
        AppNotification.notifyFailure("Not Allowed!");
      }
    }
  }
}
