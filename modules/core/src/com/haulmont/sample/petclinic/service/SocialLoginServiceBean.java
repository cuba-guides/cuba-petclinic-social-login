package com.haulmont.sample.petclinic.service;

import com.google.common.base.Strings;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.haulmont.cuba.core.global.Configuration;
import com.haulmont.cuba.core.global.GlobalConfig;
import com.haulmont.sample.petclinic.auth.SocialLoginHelper;
import com.haulmont.sample.petclinic.auth.SocialService;
import com.haulmont.sample.petclinic.core.config.FacebookConfig;
import com.haulmont.sample.petclinic.core.config.GitHubConfig;
import com.haulmont.sample.petclinic.core.config.GoogleConfig;
import com.haulmont.sample.petclinic.core.config.SocialServiceConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service(SocialLoginService.NAME)
public class SocialLoginServiceBean implements SocialLoginService {

    @Inject
    private Configuration configuration;

    @Override
    public String getLoginUrl(SocialService socialService) {
        String authEndpoint = SocialLoginHelper.getAuthEndpoint(socialService);
        String params = SocialLoginHelper.getAuthParams(
                socialService,
                getClientId(socialService),
                getRedirectUri());

        return authEndpoint + params;
    }

    @Override
    public SocialUserData getUserData(SocialService socialService, String authCode) {
        String accessToken = getAccessToken(socialService, authCode);
        String userDataJson = getUserDataAsJson(socialService, accessToken);
        return parseUserData(userDataJson);
    }

    private String getAccessToken(SocialService socialService, String authCode) {
        HttpRequestBase accessTokenRequest = getAccessTokenRequest(socialService, authCode);
        String response = performRequest(accessTokenRequest);
        return extractAccessToken(response);
    }

    private HttpRequestBase getAccessTokenRequest(SocialService socialService, String authCode) {
        switch (socialService) {
            case GOOGLE: {
                HttpPost tokenRequest = new HttpPost(getAccessTokenPath(socialService, authCode));
                tokenRequest.setEntity(getGoogleAccessTokenParams(authCode));
                return tokenRequest;
            }
            case FACEBOOK:
            case GITHUB: {
                HttpGet tokenRequest = new HttpGet(getAccessTokenPath(socialService, authCode));
                tokenRequest.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
                return tokenRequest;
            }
        }
        throw new IllegalArgumentException("Unable to create request for social service: " + socialService);
    }

    private String getAccessTokenPath(SocialService socialService, String authCode) {
        String clientId = getClientId(socialService);
        String clientSecret = getClientSecret(socialService);
        String redirectUri = getRedirectUri();
        return SocialLoginHelper.getAccessTokenPath(socialService, clientId,
                clientSecret, redirectUri, authCode);
    }

    private UrlEncodedFormEntity getGoogleAccessTokenParams(String authCode) {
        Map<String, String> params = SocialLoginHelper.getGoogleAccessTokenParams(
                getClientId(SocialService.GOOGLE),
                getClientSecret(SocialService.GOOGLE),
                getRedirectUri(),
                authCode);
        List<BasicNameValuePair> requestParams = params.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new UrlEncodedFormEntity(requestParams, StandardCharsets.UTF_8);
    }

    private String extractAccessToken(String response) {
        return new JsonParser()
                .parse(response)
                .getAsJsonObject()
                .get("access_token")
                .getAsString();
    }

    private String getUserDataAsJson(SocialService socialService, String accessToken) {
        String userDataEndpoint = SocialLoginHelper.getUserDataEndpoint(socialService);
        String params = SocialLoginHelper.getUserDataEndpointParams(socialService, accessToken,
                getUserDataFields(socialService));
        String url = userDataEndpoint + params;

        return performRequest(new HttpGet(url));
    }

    private SocialUserData parseUserData(String userDataJson) {
        JsonObject response = new JsonParser()
                .parse(userDataJson)
                .getAsJsonObject();

        String id = Strings.nullToEmpty(response.get("id").getAsString());
        String name = Strings.nullToEmpty(response.get("name").getAsString());

        String login = Strings.nullToEmpty(response.get("email").getAsString());
        if (StringUtils.isEmpty(login)) {
            login = Strings.nullToEmpty(response.get("login").getAsString());
        }

        return new SocialUserData(id, login, name);
    }

    private String performRequest(HttpRequestBase request) {
        HttpClientConnectionManager cm = new BasicHttpClientConnectionManager();
        HttpClient httpClient = HttpClientBuilder.create()
                .setConnectionManager(cm)
                .build();
        try {
            HttpResponse httpResponse = httpClient.execute(request);
            if (httpResponse.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException("Unable to perform request. Response HTTP status: "
                        + httpResponse.getStatusLine().getStatusCode());
            }
            return EntityUtils.toString(httpResponse.getEntity());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } finally {
            request.releaseConnection();
        }
    }

    private String getClientId(SocialService socialService) {
        return getSocialServiceConfig(socialService).getClientId();
    }

    private String getClientSecret(SocialService socialService) {
        return getSocialServiceConfig(socialService).getClientSecret();
    }

    private String getRedirectUri() {
        return configuration.getConfig(GlobalConfig.class).getWebAppUrl();
    }

    private String getUserDataFields(SocialService socialService) {
        return getSocialServiceConfig(socialService).getUserDataFields();
    }

    private SocialServiceConfig getSocialServiceConfig(SocialService socialService) {
        switch (socialService) {
            case GOOGLE:
                return configuration.getConfig(GoogleConfig.class);
            case FACEBOOK:
                return configuration.getConfig(FacebookConfig.class);
            case GITHUB:
                return configuration.getConfig(GitHubConfig.class);
        }
        throw new IllegalArgumentException("No config found for service: " + socialService);
    }
}