/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.*;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappedBaseHttpResponse;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappingBaseHttpClient;
import org.apache.shiro.authc.BearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthService {


    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);
    private final MappingBaseHttpClient httpClient;
    protected final ObjectMapper mapper;
    protected final OAuthProviderConfig config;
    protected final TokenCreator tokenCreator;
    private final String redirectUri;
    private final String tokenEndpointRelative;
    private final String authEndpointAbsolute;
    private final String logoutEndpointAbsolute;

    private final Map<String, String> logoutTokenMap;
    protected abstract String getTokenVerifierUri();

    protected abstract Map<String, String> getAdditionalTokenVerifierParams();

    protected abstract ResponseType getResponseType();

    protected abstract boolean doSeperateRolesRequest();

    protected abstract UserTokenPayload mapAccessToken(String spayload)
            throws JsonMappingException, JsonProcessingException;

    protected abstract String getLoginUrl(String callbackUrl);
    protected abstract String getLogoutUrl();

    protected abstract UserTokenPayload requestUserRoles(String access_token, long issued_at, long expires_at);

    protected abstract boolean verifyState(String state);

    public AuthService(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) throws UnableToConfigureOAuthService {
        this.config = config;
        this.tokenCreator = tokenCreator;
        this.redirectUri = redirectUri;
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = new MappingBaseHttpClient(this.config.getUrlOrInternal(), this.config.trustAll());
        this.logoutTokenMap = new HashMap<>();
        if (this.config.hasToBeConfigured()){
            Optional<MappedBaseHttpResponse<OpenIdConfigResponseData>> oresponse = this.httpClient.sendMappedRequest(
                    this.config.getOpenIdConfigUrl(), "GET", null, null, OpenIdConfigResponseData.class);
            if(oresponse.isEmpty()){
                throw new UnableToConfigureOAuthService(this.config.getOpenIdConfigUrl());
            }
            MappedBaseHttpResponse<OpenIdConfigResponseData> response = oresponse.get();
            if(!response.isSuccess()){
                throw new UnableToConfigureOAuthService(this.config.getOpenIdConfigUrl(), response.code);
            }
            this.tokenEndpointRelative = trimUrl(this.config.getUrlOrInternal(),response.body.getToken_endpoint());
            this.authEndpointAbsolute = extendUrl(this.config.getUrlOrInternal(),response.body.getAuthorization_endpoint());
            this.logoutEndpointAbsolute = extendUrl(this.config.getUrlOrInternal(),response.body.getEnd_session_endpoint());
        }
        else{
            this.tokenEndpointRelative = null;
            this.authEndpointAbsolute = null;
            this.logoutEndpointAbsolute = null;
        }
    }

    public static String trimUrl(String baseUrl, String endpoint) {
        if(endpoint.startsWith(baseUrl)){
            return endpoint.substring(baseUrl.length());
        }
        if(endpoint.startsWith("http")){
            return endpoint.substring(endpoint.indexOf("/",8));
        }
        return endpoint;
    }
    public static String extendUrl(String baseUrl, String endpoint) {
        if(endpoint.startsWith("http")){
            endpoint= endpoint.substring(endpoint.indexOf("/",8));
        }
        if(baseUrl.endsWith("/")){
            baseUrl=baseUrl.substring(0,baseUrl.length()-2);
        }
        return baseUrl+endpoint;
    }

    public PublicOAuthProviderConfig getConfig() {
        return new PublicOAuthProviderConfig(this);
    }

    protected MappingBaseHttpClient getHttpClient() {
        return this.httpClient;
    }

    public void handleRedirect(HttpServletRequest req, HttpServletResponse resp, String host) throws IOException {
        switch (this.getResponseType()) {
            case CODE:
                this.handleRedirectCode(req, resp, host);
                break;
            case TOKEN:
                sendErrorResponse(resp, "not yet implemented");
                break;
            case SESSION_STATE:
                break;
        }
    }

    public void sendLoginRedirectResponse(HttpServletResponse resp, String callbackUrl) {
        resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        String url = this.authEndpointAbsolute !=null?String.format(
                "%s?client_id=%s&response_type=code&scope=%s&redirect_uri=%s",
                this.authEndpointAbsolute, urlEncode(this.config.getClientId()), this.config.getScope(),
                urlEncode(callbackUrl)):this.getLoginUrl(callbackUrl);
        resp.setHeader("Location", url);
    }
    public void sendLogoutRedirectResponse(String token, HttpServletResponse resp, String redirectUrl)
            throws IOException {
        String idToken = this.logoutTokenMap.getOrDefault(token, null);
        String logoutEndpoint = this.logoutEndpointAbsolute!=null?this.logoutEndpointAbsolute:this.getLogoutUrl();
        if(idToken==null) {
            LOG.debug("unable to find token in map. Do unsafe logout.");
            resp.sendRedirect(this.logoutEndpointAbsolute);
            return;
        }
        LOG.debug("id token found. redirect to specific logout");
        resp.sendRedirect(String.format("%s?id_token_hint=%s&post_logout_redirect_uri=%s",logoutEndpoint, idToken,
                urlEncode(redirectUrl)));
    }



    private static void sendErrorResponse(HttpServletResponse resp, String message) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, message);
    }

    private void handleRedirectCode(HttpServletRequest req, HttpServletResponse resp, String host) throws IOException {
        final String code = req.getParameter("code");
        final String state = req.getParameter("state");
        OAuthResponseData response = null;
        if(this.verifyState(state)) {
            response = this.getTokenForUser(code, host);
        }
        if (response != null) {
            if (this.doSeperateRolesRequest()) {
                LOG.debug("do a seperate role request");
                long expiresAt = this.tokenCreator.getDefaultExp();
                long issuedAt = this.tokenCreator.getDefaultIat();
                UserTokenPayload data = this.requestUserRoles(response.getAccess_token(), issuedAt, expiresAt);
                if (data != null) {
                    BearerToken createdToken = this.handleUserInfoToken(data, resp, host);
                    this.logoutTokenMap.put(createdToken.getToken(),response.getId_token());
                } else {
                    sendErrorResponse(resp, "unable to verify user");
                }
            } else {
                BearerToken createdToken = this.handleUserInfoToken(response.getAccess_token(), resp, host);
                this.logoutTokenMap.put(createdToken.getToken(),response.getId_token());
            }
        } else {
            sendErrorResponse(resp, "unable to verify code");
        }
    }

    private BearerToken handleUserInfoToken(UserTokenPayload data, HttpServletResponse resp, String localHostUrl)
            throws IOException {
        BearerToken onapToken = this.tokenCreator.createNewJWT(data);
        sendTokenResponse(resp, onapToken, localHostUrl);
        return onapToken;
    }

    private BearerToken handleUserInfoToken(String accessToken, HttpServletResponse resp, String localHostUrl)
            throws IOException {
        try {
            DecodedJWT jwt = JWT.decode(accessToken);
            String spayload = base64Decode(jwt.getPayload());
            LOG.debug("payload in jwt='{}'", spayload);
            UserTokenPayload data = this.mapAccessToken(spayload);
            return this.handleUserInfoToken(data, resp, localHostUrl);
        } catch (JWTDecodeException | JsonProcessingException e) {
            LOG.warn("unable to decode jwt token {}: ", accessToken, e);
            sendErrorResponse(resp, e.getMessage());
        }
        return null;
    }


    protected List<String> mapRoles(List<String> roles) {
        final Map<String, String> map = this.config.getRoleMapping();
        return roles.stream().map(r -> map.getOrDefault(r, r)).collect(Collectors.toList());
    }

    private void sendTokenResponse(HttpServletResponse resp, BearerToken data, String localHostUrl) throws IOException {
        if (this.redirectUri == null) {
            byte[] output = data != null ? mapper.writeValueAsString(data).getBytes() : new byte[0];
            resp.setStatus(200);
            resp.setContentLength(output.length);
            resp.setContentType("application/json");
            resp.addCookie(this.tokenCreator.createAuthCookie(data));
            ServletOutputStream os = null;
            os = resp.getOutputStream();
            os.write(output);
        } else {
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", assembleUrl(localHostUrl, this.redirectUri, data.getToken()));
            resp.addCookie(this.tokenCreator.createAuthCookie(data));
        }
    }



    private static String base64Decode(String data) {
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    private OAuthResponseData getTokenForUser(String code, String localHostUrl) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("Accept", "application/json");
        Map<String, String> params = this.getAdditionalTokenVerifierParams();
        params.put("code", code);
        params.put("client_id", this.config.getClientId());
        params.put("client_secret", this.config.getSecret());
        params.put("redirect_uri", assembleRedirectUrl(localHostUrl, AuthHttpServlet.REDIRECTURI, this.config.getId()));
        StringBuilder body = new StringBuilder();
        for (Entry<String, String> p : params.entrySet()) {
            body.append(String.format("%s=%s&", p.getKey(), urlEncode(p.getValue())));
        }

        String url = this.tokenEndpointRelative !=null?this.tokenEndpointRelative :this.getTokenVerifierUri();
        Optional<MappedBaseHttpResponse<OAuthResponseData>> response =
                this.httpClient.sendMappedRequest(url, "POST",
                        body.substring(0, body.length() - 1), headers, OAuthResponseData.class);
        if (response.isPresent() && response.get().isSuccess()) {
            return response.get().body;
        }
        LOG.warn("problem get token for code {}", code);

        return null;
    }

    /**
     * Assemble callback url for service provider {host}{baseUri}/{serviceId} e.g.
     * http://10.20.0.11:8181/oauth/redirect/keycloak
     *
     * @param host
     * @param baseUri
     * @param serviceId
     * @return
     */
    public static String assembleRedirectUrl(String host, String baseUri, String serviceId) {
        return String.format("%s%s/%s", host, baseUri, serviceId);
    }

    private static String assembleUrl(String host, String uri, String token) {
        return String.format("%s%s%s", host, uri, token);
    }

    public static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }



    public enum ResponseType {
        CODE, TOKEN, SESSION_STATE
    }


    public static class PublicOAuthProviderConfig {

        private String id;
        private String title;
        private String loginUrl;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getLoginUrl() {
            return loginUrl;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public PublicOAuthProviderConfig(AuthService authService) {
            this.id = authService.config.getId();
            this.title = authService.config.getTitle();
            this.loginUrl = String.format(AuthHttpServlet.LOGIN_REDIRECT_FORMAT, authService.config.getId());
        }

    }



}
