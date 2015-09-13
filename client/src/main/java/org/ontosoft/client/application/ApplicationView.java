package org.ontosoft.client.application;

import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.TextBox;
import org.ontosoft.client.authentication.SessionStorage;
import org.ontosoft.client.rest.SoftwareREST;
import org.ontosoft.shared.classes.users.UserCredentials;
import org.ontosoft.shared.classes.users.UserSession;

import com.google.gwt.core.client.Callback;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ApplicationView extends ViewImpl implements
    ApplicationPresenter.MyView {
  interface Binder extends UiBinder<Widget, ApplicationView> { }

  @UiField
  SimplePanel contentContainer;
  
  @UiField
  TextBox username;
  
  @UiField
  Input password;
  
  @UiField
  Button loginbutton, login, logout;

  @UiField
  Modal loginform;
  
  @Inject
  public ApplicationView(Binder binder) {
    initWidget(binder.createAndBindUi(this));
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    if (slot == ApplicationPresenter.CONTENT_SLOT)
      contentContainer.setWidget(content);
    else
      super.setInSlot(slot, content);
  }
  
  @UiHandler("loginform")
  void onShowWindow(ModalShownEvent event) {
    username.setFocus(true);
  }
  
  @UiHandler("loginbutton")
  public void onLogin(ClickEvent event) {
    submitLoginForm();
    event.stopPropagation();
  }
  
  @UiHandler({"username", "password"})
  void onSoftwareEnter(KeyPressEvent event) {
    if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
      submitLoginForm();
    }
  }
  
  @UiHandler("logout")
  public void onLogout(ClickEvent event) {
    SoftwareREST.logout(new Callback<Void, Throwable>() {
      @Override
      public void onFailure(Throwable reason) {
        SoftwareREST.notifyFailure(reason.getMessage());
      }
      @Override
      public void onSuccess(Void session) {
        toggleLoginLogoutButtons();
      }
    });
  }
  
  @UiHandler("login")
  public void onLoginClick(ClickEvent event) {
    loginform.show();
  }
  
  private void submitLoginForm() {
    boolean ok1 = username.validate(true);
    boolean ok2 = password.validate(true);
    if(ok1 && ok2) {
      UserCredentials credentials = new UserCredentials();
      credentials.setName(username.getValue());
      credentials.setPassword(password.getValue());
      SoftwareREST.login(credentials, new Callback<UserSession, Throwable>() {
        @Override
        public void onFailure(Throwable reason) {
          SoftwareREST.notifyFailure(reason.getMessage());
        }
        @Override
        public void onSuccess(UserSession session) {
          toggleLoginLogoutButtons();
          username.setValue(null);
          password.setValue(null);
          loginform.hide();
        }
      });
    }
  }

  @Override
  public void initializeParameters(String[] parameters) {
    toggleLoginLogoutButtons();
  }
  
  private void toggleLoginLogoutButtons() {
    UserSession session = SessionStorage.getSession();
    if(session == null) {
      login.setVisible(true);
      logout.setVisible(false);
    }
    else {
      login.setVisible(false);
      logout.setText("Logout " + session.getUsername());
      logout.setVisible(true);
    }
  }

}
