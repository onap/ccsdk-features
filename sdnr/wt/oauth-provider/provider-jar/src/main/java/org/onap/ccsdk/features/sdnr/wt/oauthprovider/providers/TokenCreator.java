/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import java.util.Arrays;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.opendaylight.aaa.shiro.filters.backport.BearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenCreator {

    private static final Logger LOG = LoggerFactory.getLogger(AuthHttpServlet.class.getName());
    private static final long DEFAULT_TOKEN_LIFETIME_MS = 30 * 60 * 1000;
    private final String issuer;
    private static TokenCreator _instance;
    private final String secret;

    private static final String ROLES_CLAIM = "roles";
    private static final String FAMILYNAME_CLAIM = "family_name";
    private static final String NAME_CLAIM = "name";

    public static TokenCreator getInstance(Config config) {
        if (_instance == null) {
            _instance = new TokenCreator(config);
        }
        return _instance;
    }
    public static TokenCreator getInstance(String secret, String issuer) {
        if (_instance == null) {
            _instance = new TokenCreator(secret, issuer);
        }
        return _instance;
    }

    private TokenCreator(Config config) {
        this(config.getTokenSecret(),config.getTokenIssuer());
    }
    private TokenCreator(String secret, String issuer) {
        this.secret = secret;
        this.issuer = issuer;
    }

    public BearerToken createNewJWT(UserTokenPayload data) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        final String token = JWT.create().withIssuer(issuer).withExpiresAt(new Date(data.getExp()))
                .withSubject(data.getPreferredUsername()).withClaim(NAME_CLAIM, data.getGivenName())
                .withClaim(FAMILYNAME_CLAIM, data.getFamilyName())
                .withArrayClaim(ROLES_CLAIM, data.getRoles().toArray(new String[data.getRoles().size()]))
                .sign(algorithm);
        return new BearerToken(token);
    }

    public DecodedJWT verify(String token) {
        DecodedJWT jwt = null;
        LOG.debug("try to verify token {}", token);
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
            jwt = verifier.verify(token);

        } catch (JWTVerificationException e) {
            LOG.warn("unable to verify token {}:", token, e);
        }
        return jwt;
    }

    public long getDefaultExp() {
        return new Date().getTime() + DEFAULT_TOKEN_LIFETIME_MS;
    }

    public long getDefaultExp(long exp_in) {
        return new Date().getTime() + exp_in;
    }

    public UserTokenPayload decode(HttpServletRequest req) throws JWTDecodeException {
        final String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer")) {
            return null;
        }
        DecodedJWT jwt = JWT.decode(authHeader.substring(7));
        UserTokenPayload data = new UserTokenPayload();
        data.setRoles(Arrays.asList(jwt.getClaim(ROLES_CLAIM).asArray(String.class)));
        data.setExp(jwt.getExpiresAt().getTime());
        data.setFamilyName(jwt.getClaim(FAMILYNAME_CLAIM).asString());
        data.setGivenName(jwt.getClaim(NAME_CLAIM).asString());
        data.setPreferredUsername(jwt.getClaim(NAME_CLAIM).asString());

        return data;
    }
}
