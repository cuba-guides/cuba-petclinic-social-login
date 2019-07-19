package com.haulmont.sample.petclinic.web;

import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.screen.DialogMode;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.login.LoginScreen;

@Route
@DialogMode(width = "430")
@UiController("petclinic_LoginDialog")
@UiDescriptor("login-dialog.xml")
public class LoginDialog extends LoginScreen {

    public void performDefaultLogin() {
        login();

        if (connection.isAuthenticated()) {
            close(WINDOW_CLOSE_ACTION);
        }
    }
}
