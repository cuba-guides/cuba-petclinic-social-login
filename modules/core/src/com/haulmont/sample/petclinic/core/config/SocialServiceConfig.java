package com.haulmont.sample.petclinic.core.config;

/**
 * Common interface for Social Service Configs.
 */
public interface SocialServiceConfig {

    /**
     * @return app client id provided by service
     */
    String getClientId();

    /**
     * @return app client secret provided by service
     */
    String getClientSecret();

    /**
     * @return a set of fields to fetch in user data
     */
    String getUserDataFields();
}
