package com.haulmont.sample.petclinic.web.auth;

import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.executors.BackgroundWorker;
import com.haulmont.cuba.gui.executors.UIAccessor;
import com.haulmont.cuba.security.auth.Credentials;
import com.haulmont.cuba.web.App;
import com.haulmont.cuba.web.controllers.ControllerUtils;
import com.haulmont.sample.petclinic.auth.SocialService;
import com.haulmont.sample.petclinic.service.SocialLoginService;
import com.vaadin.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

/**
 * Bean is intended to handle social service responses containing an auth code.
 * <p>
 * Loads user data via {@link SocialLoginService} and creates {@link SocialCredentials}
 * to trigger login process via custom {@link SocialLoginProvider}.
 */
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@Component(SocialServiceCallbackHandler.NAME)
public class SocialServiceCallbackHandler implements RequestHandler, InitializingBean {

    public static final String NAME = "petclinic_SocialServiceCallbackHandler";

    private static final Logger log = LoggerFactory.getLogger(SocialServiceCallbackHandler.class);

    private App app;
    private BackgroundWorker backgroundWorker;
    private UIAccessor uiAccessor;
    private SocialLoginService socialLoginService;
    private Messages messages;

    private final SocialService service;
    private final URI redirectUri;

    public SocialServiceCallbackHandler(SocialService service) {
        this.service = service;
        redirectUri = Page.getCurrent().getLocation();
    }

    @Override
    public boolean handleRequest(VaadinSession session,
                                 VaadinRequest request,
                                 VaadinResponse response) throws IOException {
        if (request.getParameter("code") == null) {
            return false;
        }

        uiAccessor.accessSynchronously(() -> {
            try {
                Credentials credentials = getCredentials(request.getParameter("code"), service);

                app.getConnection()
                        .login(credentials);
            } catch (Exception e) {
                log.error("Unable to login using service: " + service, e);
            } finally {
                session.removeRequestHandler(this);
            }
        });

        ((VaadinServletResponse) response).getHttpServletResponse().
                sendRedirect(ControllerUtils.getLocationWithoutParams(redirectUri));

        return true;
    }


    @Inject
    public void setApp(App app) {
        this.app = app;
    }

    @Inject
    public void setBackgroundWorker(BackgroundWorker backgroundWorker) {
        this.backgroundWorker = backgroundWorker;
    }

    @Inject
    public void setSocialLoginService(SocialLoginService socialLoginService) {
        this.socialLoginService = socialLoginService;
    }

    @Inject
    public void setMessages(Messages messages) {
        this.messages = messages;
    }

    @Override
    public void afterPropertiesSet() {
        uiAccessor = backgroundWorker.getUIAccessor();
    }

    private Credentials getCredentials(String authCode, SocialService socialService) {
        SocialLoginService.SocialUserData userData = socialLoginService.getUserData(socialService, authCode);

        Locale defaultLocale = messages.getTools()
                .getDefaultLocale();

        return new SocialCredentials(userData, socialService, defaultLocale);
    }
}
