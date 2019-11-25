package com.haulmont.sample.petclinic.service;

import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.core.global.View;
import com.haulmont.cuba.security.entity.Group;
import com.haulmont.cuba.security.entity.User;
import com.haulmont.sample.petclinic.auth.SocialService;
import com.haulmont.sample.petclinic.core.config.SocialRegistrationConfig;
import com.haulmont.sample.petclinic.entity.SocialUser;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.regex.Pattern;

@Service(SocialRegistrationService.NAME)
public class SocialRegistrationServiceBean implements SocialRegistrationService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[^@]+@[^.]+\\..+");

    @Inject
    private DataManager dataManager;
    @Inject
    private Configuration configuration;

    @Override
    public User findOrRegisterUser(String socialServiceId, String login, String name,
                                   SocialService socialService) {
        User existingUser = findExistingUser(socialService, socialServiceId);
        if (existingUser != null) {
            return existingUser;
        }

        SocialUser user = createNewUser(socialServiceId, login, name, socialService);

        return dataManager.commit(user);
    }

    @Nullable
    private User findExistingUser(SocialService socialService, String socialServiceId) {
        String socialServiceField = getSocialIdParamName(socialService);

        return dataManager.load(User.class)
                .query("select u from sec$User u where " +
                        String.format("u.%s = :socialServiceId", socialServiceField))
                .parameter("socialServiceId", socialServiceId)
                .one();
    }

    private SocialUser createNewUser(String socialServiceId, String login,
                                     String name, SocialService socialService) {
        SocialUser user = dataManager.create(SocialUser.class);

        user.setLogin(login);
        user.setName(name);
        user.setGroup(getDefaultGroup());
        user.setActive(true);

        if (isEmail(login)) {
            user.setEmail(login);
        }

        switch (socialService) {
            case GOOGLE:
                user.setGoogleId(socialServiceId);
                break;
            case FACEBOOK:
                user.setFacebookId(socialServiceId);
                break;
            case GITHUB:
                user.setGithubId(socialServiceId);
                break;
        }

        return user;
    }

    private Group getDefaultGroup() {
        SocialRegistrationConfig config = configuration.getConfig(SocialRegistrationConfig.class);

        LoadContext<Group> ctx = new LoadContext<>(Group.class);
        ctx.setQueryString("select g from sec$Group g where g.id = :defaultGroupId")
                .setParameter("defaultGroupId", config.getDefaultGroupId());
        ctx.setView(View.MINIMAL);

        return dataManager.load(ctx);
    }

    private String getSocialIdParamName(SocialService socialService) {
        switch (socialService) {
            case GOOGLE:
                return "googleId";
            case FACEBOOK:
                return "facebookId";
            case GITHUB:
                return "githubId";
        }
        throw new IllegalArgumentException("No social id param found for service: " + socialService);
    }

    private boolean isEmail(String s) {
        return EMAIL_PATTERN.matcher(s).matches();
    }
}