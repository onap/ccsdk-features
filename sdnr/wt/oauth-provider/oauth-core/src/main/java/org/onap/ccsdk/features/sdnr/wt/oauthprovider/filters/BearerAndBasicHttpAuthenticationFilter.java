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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.filters;

import java.util.Locale;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.filter.authc.BearerHttpAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BearerAndBasicHttpAuthenticationFilter extends BearerHttpAuthenticationFilter {

    // defined in lower-case for more efficient string comparison
    private static final Logger LOG = LoggerFactory.getLogger(BearerAndBasicHttpAuthenticationFilter.class);
    private ODLHttpAuthenticationHelperFilter basicAuthFilter;

    public BearerAndBasicHttpAuthenticationFilter() {
        this.basicAuthFilter = new ODLHttpAuthenticationHelperFilter();
    }

    protected static final String OPTIONS_HEADER = "OPTIONS";

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        final String authHeader = this.getAuthzHeader(request);
        if (authHeader != null && authHeader.startsWith("Basic")) {
            return this.createBasicAuthToken(request, response);
        }
        return super.createToken(request, response);
    }

    @Override
    protected String[] getPrincipalsAndCredentials(String scheme, String token) {
        LOG.debug("getPrincipalsAndCredentials with scheme {} and token {}", scheme, token);
        if (scheme.toLowerCase().equals("basic")) {
            return this.basicAuthFilter.getPrincipalsAndCredentials(scheme, token);
        }
        return super.getPrincipalsAndCredentials(scheme, token);
    }

    @Override
    protected boolean isLoginAttempt(String authzHeader) {
        LOG.debug("isLoginAttempt with header {}", authzHeader);
        if (this.basicAuthFilter.isLoginAttempt(authzHeader)) {
            return true;
        }
        return super.isLoginAttempt(authzHeader);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        final HttpServletRequest httpRequest = WebUtils.toHttp(request);
        final String httpMethod = httpRequest.getMethod();
        //always allow options requests
        if (OPTIONS_HEADER.equalsIgnoreCase(httpMethod)) {
            return true;
        }

        if (this.basicAuthFilter.isAccessAllowed(httpRequest, response, mappedValue)) {
            LOG.debug("isAccessAllowed succeeded on basicAuth");
            return true;
        }

        return super.isAccessAllowed(request, response, mappedValue);
    }

    protected AuthenticationToken createBasicAuthToken(ServletRequest request, ServletResponse response) {
        String authorizationHeader = getAuthzHeader(request);
        if (authorizationHeader == null || authorizationHeader.length() == 0) {
            // Create an empty authentication token since there is no
            // Authorization header.
            return createToken("", "", request, response);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempting to execute login with headers [" + authorizationHeader + "]");
        }

        String[] prinCred = getPrincipalsAndCredentials(authorizationHeader, request);
        if (prinCred == null || prinCred.length < 2) {
            // Create an authentication token with an empty password,
            // since one hasn't been provided in the request.
            String username = prinCred == null || prinCred.length == 0 ? "" : prinCred[0];
            return createToken(username, "", request, response);
        }

        String username = prinCred[0];
        String password = prinCred[1];

        return createToken(username, password, request, response);
    }

    private static class ODLHttpAuthenticationHelperFilter extends BasicHttpAuthenticationFilter {

        private static final Logger LOG = LoggerFactory.getLogger(ODLHttpAuthenticationHelperFilter.class);

        // defined in lower-case for more efficient string comparison
        protected static final String BEARER_SCHEME = "bearer";

        protected static final String OPTIONS_HEADER = "OPTIONS";

        public ODLHttpAuthenticationHelperFilter() {
            LOG.info("Creating the ODLHttpAuthenticationFilter");
        }

        @Override
        protected String[] getPrincipalsAndCredentials(String scheme, String encoded) {
            final String decoded = Base64.decodeToString(encoded);
            // attempt to decode username/password; otherwise decode as token
            if (decoded.contains(":")) {
                return decoded.split(":");
            }
            return new String[]{encoded};
        }

        @Override
        protected boolean isLoginAttempt(String authzHeader) {
            final String authzScheme = getAuthzScheme().toLowerCase(Locale.ROOT);
            final String authzHeaderLowerCase = authzHeader.toLowerCase(Locale.ROOT);
            return authzHeaderLowerCase.startsWith(authzScheme)
                    || authzHeaderLowerCase.startsWith(BEARER_SCHEME);
        }

        @Override
        protected boolean isAccessAllowed(ServletRequest request, ServletResponse response,
                                          Object mappedValue) {
            final HttpServletRequest httpRequest = WebUtils.toHttp(request);
            final String httpMethod = httpRequest.getMethod();
            if (OPTIONS_HEADER.equalsIgnoreCase(httpMethod)) {
                return true;
            } else {
                return super.isAccessAllowed(httpRequest, response, mappedValue);
            }
        }
    }
}
