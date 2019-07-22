package com.haulmont.sample.petclinic.service;

import com.haulmont.sample.petclinic.auth.SocialService;

import java.io.Serializable;

public interface SocialLoginService {

    String NAME = "petclinic_SocialLoginService";

    /**
     * Returns a login URL for the given {@code socialService}.
     * <p>
     * Intended to redirect a user to social service login page.
     */
    String getLoginUrl(SocialService socialService);

    /**
     * Exchanges the given {@code authCode} for social service access token
     * and fetches profile info of authenticated user.
     */
    SocialUserData getUserData(SocialService socialService, String authCode);

    /**
     * Immutable POJO that stores social service user info.
     */
    class SocialUserData implements Serializable {

        private String id;
        private String login;
        private String name;

        public SocialUserData(String id, String login, String name) {
            this.id = id;
            this.login = login;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getLogin() {
            return login;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "SocialUserData{" +
                    "id='" + id + '\'' +
                    ", login='" + login + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}