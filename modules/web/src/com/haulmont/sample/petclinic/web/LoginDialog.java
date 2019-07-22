package com.haulmont.sample.petclinic.web;

import com.haulmont.cuba.gui.Route;
import com.haulmont.cuba.gui.components.Button;
import com.haulmont.cuba.gui.screen.DialogMode;
import com.haulmont.cuba.gui.screen.Subscribe;
import com.haulmont.cuba.gui.screen.UiController;
import com.haulmont.cuba.gui.screen.UiDescriptor;
import com.haulmont.cuba.web.app.login.LoginScreen;
import com.haulmont.sample.petclinic.auth.SocialService;
import com.haulmont.sample.petclinic.service.SocialLoginService;
import com.haulmont.sample.petclinic.web.auth.SocialServiceCallbackHandler;
import com.vaadin.server.Page;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.VaadinSession;

import javax.inject.Inject;

@Route
@DialogMode(width = "430")
@UiController("petclinic_LoginDialog")
@UiDescriptor("login-dialog.xml")
public class LoginDialog extends LoginScreen {

    @Inject
    private SocialLoginService socialLoginService;

    public void performDefaultLogin() {
        login();

        if (connection.isAuthenticated()) {
            close(WINDOW_CLOSE_ACTION);
        }
    }

    @Subscribe("googleLogin")
    private void onGoogleLoginClick(Button.ClickEvent event) {
        performSocialLogin(SocialService.GOOGLE);
    }

    @Subscribe("facebookLogin")
    private void onFacebookLoginClick(Button.ClickEvent event) {
        performSocialLogin(SocialService.FACEBOOK);
    }

    @Subscribe("githubLogin")
    private void onGithubLoginClick(Button.ClickEvent event) {
        performSocialLogin(SocialService.GITHUB);
    }

    private void performSocialLogin(SocialService socialService) {
        String loginUrl = socialLoginService.getLoginUrl(socialService);

        VaadinSession.getCurrent()
                .addRequestHandler(getCallbackHandler(socialService));

        close(WINDOW_CLOSE_ACTION);

        Page.getCurrent()
                .setLocation(loginUrl);
    }

    private RequestHandler getCallbackHandler(SocialService socialService) {
        return getBeanLocator()
                .getPrototype(SocialServiceCallbackHandler.NAME, socialService);
    }
}
