package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.security.entity.User;
import com.haulmont.sample.petclinic.auth.SocialService;

/**
 * Service is intended to find or register new user considering provided user data.
 */
public interface SocialRegistrationService {

    String NAME = "petclinic_SocialRegistrationService";

    /**
     * Returns newly registered or already existing user.
     */
    User findOrRegisterUser(String socialServiceId, String login, String name,
                            SocialService socialService);
}