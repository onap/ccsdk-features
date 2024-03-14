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
import java.io.IOException;
import java.security.Security;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.apache.shiro.authc.BearerToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenCreator {

    private static final Logger LOG = LoggerFactory.getLogger(AuthHttpServlet.class.getName());
    private final String issuer;
    private static TokenCreator _instance;
    private final long tokenLifetimeSeconds;
    private final Algorithm algorithm;

    private static final String ROLES_CLAIM = "roles";
    private static final String FAMILYNAME_CLAIM = "family_name";
    private static final String NAME_CLAIM = "name";
    private static final String PROVIDERID_CLAIM = "provider_id";
    private static final String COOKIE_NAME_AUTH = "token";

    static {
        Security.addProvider(
                new BouncyCastleProvider()
       );
    }
    public static TokenCreator getInstance(Config config) throws IllegalArgumentException, IOException {
        if (_instance == null) {
            _instance = new TokenCreator(config);
        }
        return _instance;
    }

    public static TokenCreator getInstance(String alg, String secret, String issuer, long tokenLifetime)
            throws IllegalArgumentException, IOException {
        return getInstance(alg, secret, null, issuer, tokenLifetime);
    }

    public static TokenCreator getInstance(String alg, String secret, String pubkey, String issuer, long tokenLifetime)
            throws IllegalArgumentException, IOException {
        if (_instance == null) {
            _instance = new TokenCreator(alg, secret, pubkey, issuer, tokenLifetime);
        }
        return _instance;
    }

    private TokenCreator(Config config) throws IllegalArgumentException, IOException {
        this(config.getAlgorithm(), config.getTokenSecret(), config.getPublicKey(), config.getTokenIssuer(),
                config.getTokenLifetime());
    }

    private TokenCreator(String alg, String secret, String pubkey, String issuer, long tokenLifetime)
            throws IllegalArgumentException, IOException {
        this.issuer = issuer;
        this.tokenLifetimeSeconds = tokenLifetime;
        this.algorithm = this.createAlgorithm(alg, secret, pubkey);
    }

    private Algorithm createAlgorithm(String alg, String secret, String pubkey)
            throws IllegalArgumentException, IOException {
        if (alg == null) {
            alg = Config.TOKENALG_HS256;
        }
        switch (alg) {
            case Config.TOKENALG_HS256:
                return Algorithm.HMAC256(secret);
            case Config.TOKENALG_RS256:
                return Algorithm.RSA256(RSAKeyReader.getPublicKey(pubkey), RSAKeyReader.getPrivateKey(secret));
            case Config.TOKENALG_RS512:
                return Algorithm.RSA512(RSAKeyReader.getPublicKey(pubkey), RSAKeyReader.getPrivateKey(secret));
            case Config.TOKENALG_CLIENT_RS256:
                return Algorithm.RSA256(RSAKeyReader.getPublicKey(pubkey), null);
            case Config.TOKENALG_CLIENT_RS512:
                return Algorithm.RSA512(RSAKeyReader.getPublicKey(pubkey), null);
        }
        throw new IllegalArgumentException(String.format("unable to find algorithm for %s", alg));

    }

    public BearerToken createNewJWT(UserTokenPayload data) {
        final String token = JWT.create().withIssuer(issuer).withExpiresAt(new Date(data.getExp()))
                .withIssuedAt(new Date(data.getIat())).withSubject(data.getPreferredUsername())
                .withClaim(NAME_CLAIM, data.getGivenName()).withClaim(FAMILYNAME_CLAIM, data.getFamilyName())
                .withClaim(PROVIDERID_CLAIM, data.getProviderId())
                .withArrayClaim(ROLES_CLAIM, data.getRoles().toArray(new String[data.getRoles().size()]))
                .sign(this.algorithm);
        LOG.trace("token created: {}", token);
        return new BearerToken(token);
    }

    public DecodedJWT verify(String token) {
        DecodedJWT jwt = null;
        LOG.debug("try to verify token {}", token);
        try {
            JWTVerifier verifier = JWT.require(this.algorithm).withIssuer(issuer).build();
            jwt = verifier.verify(token);

        } catch (JWTVerificationException e) {
            LOG.warn("unable to verify token {}:", token, e);
        }
        return jwt;
    }

    public long getDefaultExp() {
        return new Date().getTime() + (this.tokenLifetimeSeconds * 1000);
    }

    public long getDefaultExp(long expIn) {
        return new Date().getTime() + expIn;
    }

    public long getDefaultIat() {
        return new Date().getTime();
    }

    public String getBearerToken(HttpServletRequest req) {
        return this.getBearerToken(req, false);
    }

    public String getBearerToken(HttpServletRequest req, boolean checkCookie) {
        final String authHeader = req.getHeader("Authorization");
        if ((authHeader == null || !authHeader.startsWith("Bearer")) && checkCookie) {
            Cookie[] cookies = req.getCookies();
            Optional<Cookie> ocookie = Optional.empty();
            if (cookies != null) {
                ocookie = Arrays.stream(cookies).filter(c -> c != null && COOKIE_NAME_AUTH.equals(c.getName()))
                        .findFirst();
            }
            if (ocookie.isEmpty()) {
                return null;
            }
            return ocookie.get().getValue();
        }
        return authHeader.substring(7);
    }

    public UserTokenPayload decode(HttpServletRequest req) throws JWTDecodeException {
        final String token = this.getBearerToken(req);
        return token != null ? this.decode(token) : null;
    }

    public UserTokenPayload decode(String token) {
        if (token == null) {
            return null;
        }
        DecodedJWT jwt = JWT.decode(token);
        UserTokenPayload data = new UserTokenPayload();
        data.setRoles(Arrays.asList(jwt.getClaim(ROLES_CLAIM).asArray(String.class)));
        data.setExp(jwt.getExpiresAt().getTime());
        data.setFamilyName(jwt.getClaim(FAMILYNAME_CLAIM).asString());
        data.setGivenName(jwt.getClaim(NAME_CLAIM).asString());
        data.setPreferredUsername(jwt.getClaim(NAME_CLAIM).asString());
        data.setProviderId(jwt.getClaim(PROVIDERID_CLAIM).asString());
        return data;
    }

    public Cookie createAuthCookie(BearerToken data) {
        Cookie cookie = new Cookie(COOKIE_NAME_AUTH, data.getToken());
        cookie.setMaxAge((int) this.tokenLifetimeSeconds);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        return cookie;
    }
}
