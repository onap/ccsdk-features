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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider;

import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.apache.shiro.authc.BearerToken;
import org.opendaylight.aaa.shiro.realm.TokenAuthRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OAuth2Realm extends TokenAuthRealm {

    public static final String REALM_NAME = "OAuth2Realm";
    private static final Logger LOG = LoggerFactory.getLogger(OAuth2Realm.class);
    private final TokenCreator tokenCreator;
    private final Config config;

    public OAuth2Realm() throws IllegalArgumentException, IOException, InvalidConfigurationException {
        super();
        super.setName(REALM_NAME);
        this.config = Config.getInstance();
        this.tokenCreator = TokenCreator.getInstance(this.config);
        LOG.info("instantiated");
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        boolean supports = (token instanceof BearerToken)
                || (this.config.doSupportOdlUsers() && (token instanceof UsernamePasswordToken));
        LOG.debug("supports {} is {}", token == null ? null : token.getClass().getName(), supports);
        return supports;
    }

    @Override
    public String getName() {
        return REALM_NAME;
    }

    @Override
    protected void assertCredentialsMatch(AuthenticationToken atoken, AuthenticationInfo ai)
            throws AuthenticationException {
        LOG.debug("assertCredentialsMatch");
        if (atoken instanceof BearerToken) {
            if (this.tokenCreator.verify(((BearerToken) atoken).getToken()) == null) {
                throw new AuthenticationException("Credentials do not match");
            }
        } else if (this.config.doSupportOdlUsers() && (atoken instanceof UsernamePasswordToken)) {
            //nothing to do
        } else {
            throw new AuthenticationException("AuthenticationInfo is not an OAuth2AuthenticationInfo");
        }
    }


    // check what I can do
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg) {

        LOG.debug("auth info in shiro");
        Object principal = arg.getPrimaryPrincipal();
        if (principal instanceof DecodedJWT) {
            LOG.debug("detected jwt token");
            try {
                DecodedJWT token = (DecodedJWT) arg.getPrimaryPrincipal();
                String[] roles = token.getClaim("roles").asArray(String.class);
                SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
                for (String role : roles) {
                    LOG.trace("found role {} in token", role);
                    authorizationInfo.addRole(role);
                }
                return authorizationInfo;
            } catch (ClassCastException e) {
                LOG.error("Couldn't decode authorization request", e);
            }
        } else if (principal instanceof ODLPrincipal) {
            LOG.debug("detected basic token");
            ODLPrincipal odlPrincipal = (ODLPrincipal) principal;
            return new SimpleAuthorizationInfo(odlPrincipal.getRoles());
        }
        return new SimpleAuthorizationInfo();
    }



    // check who I am
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        LOG.debug("auth token in shiro:");
        if (token instanceof UsernamePasswordToken && this.config.doSupportOdlUsers()) {
            LOG.debug("basic auth token found");
            return super.doGetAuthenticationInfo(token);
        } else if (token instanceof BearerToken) {
            LOG.debug("jwt token found");
            BearerToken oauthToken = (BearerToken) token;

            DecodedJWT jwt = this.tokenCreator.verify(oauthToken.getToken());
            if (jwt != null) {
                SimpleAuthenticationInfo authenticationInfo =
                        new SimpleAuthenticationInfo(jwt, token.getCredentials(), getName());
                return authenticationInfo;

            }
        } else {
            LOG.debug("no valid token found");
        }
        throw new AuthenticationException("unable to verify token " + token);

    }

}
