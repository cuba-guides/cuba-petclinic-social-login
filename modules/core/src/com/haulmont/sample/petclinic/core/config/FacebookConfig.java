package com.haulmont.sample.petclinic.core.config;

import com.haulmont.cuba.core.config.Config;
import com.haulmont.cuba.core.config.Property;
import com.haulmont.cuba.core.config.Source;
import com.haulmont.cuba.core.config.SourceType;
import com.haulmont.cuba.core.config.defaults.Default;

/**
 * @see SocialServiceConfig
 */
@Source(type = SourceType.APP)
public interface FacebookConfig extends Config, SocialServiceConfig {

    @Property("facebook.clientId")
    String getClientId();

    @Property("facebook.clientSecret")
    String getClientSecret();

    @Default("id,name,email")
    @Property("facebook.userDataFields")
    String getUserDataFields();
}
