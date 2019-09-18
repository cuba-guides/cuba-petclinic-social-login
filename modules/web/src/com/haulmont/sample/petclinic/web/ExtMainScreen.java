package com.haulmont.sample.petclinic.web;

import com.haulmont.cuba.gui.Screens;
import com.haulmont.cuba.gui.components.mainwindow.UserActionsButton;
import com.haulmont.cuba.gui.screen.Install;
import com.haulmont.cuba.gui.screen.OpenMode;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.main.MainScreen;

import javax.inject.Inject;

@UiController("main")
@UiDescriptor("ext-main-screen.xml")
public class ExtMainScreen extends MainScreen {

    @Inject
    private Screens screens;

    @SuppressWarnings("unused")
    @Install(to = "userActionsButton", subject = "loginHandler")
    private void loginHandler(UserActionsButton.LoginHandlerContext ctx) {
        showLoginDialog();
    }

    private void showLoginDialog() {
        screens.create(LoginDialog.class, OpenMode.DIALOG)
                .show();
    }
}