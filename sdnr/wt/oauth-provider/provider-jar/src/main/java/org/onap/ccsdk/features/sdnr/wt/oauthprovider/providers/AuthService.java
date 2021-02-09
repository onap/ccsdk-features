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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthResponseData;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappedBaseHttpResponse;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.client.MappingBaseHttpClient;
import org.opendaylight.aaa.shiro.filters.backport.BearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AuthService {


    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class.getName());
    private final MappingBaseHttpClient httpClient;
    protected final ObjectMapper mapper;
    protected final OAuthProviderConfig config;
    protected final TokenCreator tokenCreator;
    private final String redirectUri;
    private String localHostUrl;
    public void setLocalHostUrl(String url) {
        this.localHostUrl = url;
    }
    protected abstract String getTokenVerifierUri();

    protected abstract Map<String, String> getAdditionalTokenVerifierParams();

    protected abstract ResponseType getResponseType();

    protected abstract boolean doSeperateRolesRequest();

    protected abstract UserTokenPayload mapAccessToken(String spayload)
            throws JsonMappingException, JsonProcessingException;

    protected abstract String getLoginUrl(String callbackUrl);

    public AuthService(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) {
        this.config = config;
        this.tokenCreator = tokenCreator;
        this.redirectUri = redirectUri;
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.httpClient = new MappingBaseHttpClient(this.config.getHost());
    }

    public PublicOAuthProviderConfig getConfig(String host) {
        return new PublicOAuthProviderConfig(this, host);
    }

    public void handleRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (this.getResponseType()) {
            case CODE:
                this.handleRedirectCode(req, resp);
                break;
            case TOKEN:
                sendErrorResponse(resp, "not yet implemented");
                break;
            case SESSION_STATE:
                break;
        }

    }

    private static void sendErrorResponse(HttpServletResponse resp, String message) throws IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, message);

    }

    private void handleRedirectCode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        final String code = req.getParameter("code");
        OAuthResponseData response = this.getTokenForUser(code);
        if (response != null) {
            if (this.doSeperateRolesRequest()) {

            } else {
                this.handleUserInfoToken(response.getAccess_token(), resp);
            }
        } else {
            sendErrorResponse(resp, "unable to verify code");
        }

    }

    private void handleUserInfoToken(String accessToken, HttpServletResponse resp) throws IOException {
        try {
            DecodedJWT jwt = JWT.decode(accessToken);

            String spayload = base64Decode(jwt.getPayload());
            LOG.debug("payload in jwt from keycload='{}'", spayload);

            UserTokenPayload data = this.mapAccessToken(spayload);
            BearerToken onapToken = this.tokenCreator.createNewJWT(data);
            sendTokenResponse(resp, onapToken);
        } catch (JWTDecodeException | JsonProcessingException e) {
            LOG.warn("unable to decode jwt token {}: ", accessToken, e);
            sendErrorResponse(resp, e.getMessage());
        }
    }


    private void sendTokenResponse(HttpServletResponse resp, BearerToken data) throws IOException {
        if (this.redirectUri == null) {
            byte[] output = data != null ? mapper.writeValueAsString(data).getBytes() : new byte[0];
            resp.setStatus(200);
            resp.setContentLength(output.length);
            resp.setContentType("application/json");
            ServletOutputStream os = null;
            os = resp.getOutputStream();
            os.write(output);
        } else {
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            resp.setHeader("Location", assembleUrl(this.localHostUrl, this.redirectUri, data.getToken()));
        }
    }



    private static String base64Decode(String data) {
        return new String(Base64.getDecoder().decode(data), StandardCharsets.UTF_8);
    }

    private OAuthResponseData getTokenForUser(String code) {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        Map<String, String> params = this.getAdditionalTokenVerifierParams();
        params.put("code", code);
        params.put("client_id", this.config.getClientId());
        params.put("client_secret", this.config.getSecret());
        params.put("redirect_uri",
                assembleRedirectUrl(localHostUrl, AuthHttpServlet.REDIRECTURI, this.config.getId()));
        StringBuilder body = new StringBuilder();
        for (Entry<String, String> p : params.entrySet()) {
            body.append(String.format("%s=%s&", p.getKey(), urlEncode(p.getValue())));
        }

        Optional<MappedBaseHttpResponse<OAuthResponseData>> response = this.httpClient.sendMappedRequest(this.getTokenVerifierUri(),
                "POST", body.substring(0, body.length() - 1), headers, OAuthResponseData.class);
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

        public PublicOAuthProviderConfig(AuthService authService, String host) {
            this.id = authService.config.getId();
            this.title = authService.config.getTitle();
            this.loginUrl = authService.getLoginUrl(
                    assembleRedirectUrl(host, AuthHttpServlet.REDIRECTURI, this.id));
        }

    }



}
