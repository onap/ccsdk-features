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
package org.onap.ccsdk.features.sdnr.wt.oauthprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.OAuth2Realm;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;
import org.opendaylight.aaa.api.shiro.principal.ODLPrincipal;
import org.opendaylight.aaa.shiro.filters.backport.BearerToken;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.AuthenticationManager;
import org.opendaylight.aaa.shiro.tokenauthrealm.auth.TokenAuthenticators;
import org.opendaylight.aaa.shiro.web.env.ThreadLocals;

public class TestRealm {

    private static OAuth2RealmToTest realm;
    private static TokenCreator tokenCreator;

    @BeforeClass
    public static void init() {
        ThreadLocals.AUTH_SETVICE_TL.set(new AuthenticationManager());
        ThreadLocals.TOKEN_AUTHENICATORS_TL.set(new TokenAuthenticators());
        try {
            Config config = Config.getInstance(TestConfig.TEST_CONFIG_FILENAME);
            tokenCreator = TokenCreator.getInstance(config);
            realm = new OAuth2RealmToTest();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }


    @Test
    public void testTokenSupport() {
        assertTrue(realm.supports(new UsernamePasswordToken()));
        assertTrue(realm.supports(new BearerToken("")));
    }


    @Test
    public void testAuthorizationInfo() {
        //bearer token use case
        PrincipalCollection c = mock(PrincipalCollection.class);
        final List<String> roles = Arrays.asList("admin", "provision");
        UserTokenPayload userData = createUserData("", roles);

        DecodedJWT decodedJwt = tokenCreator.verify(tokenCreator.createNewJWT(userData).getToken());
        when(c.getPrimaryPrincipal()).thenReturn(decodedJwt);

        AuthorizationInfo ai = realm.doGetAuthorizationInfo(c);
        for (String role : roles) {
            assertTrue(ai.getRoles().contains(role));
        }
        assertEquals(roles.size(), ai.getRoles().size());
        //odl token use case
        ODLPrincipal principal = mock(ODLPrincipal.class);
        when(principal.getRoles()).thenReturn(new HashSet<String>(roles));
        PrincipalCollection c2 = mock(PrincipalCollection.class);
        when(c2.getPrimaryPrincipal()).thenReturn(principal);
        ai = realm.doGetAuthorizationInfo(c2);
        for (String role : roles) {
            assertTrue(ai.getRoles().contains(role));
        }
        assertEquals(roles.size(), ai.getRoles().size());

    }

    @Test
    public void testAssertCredentialsMatch() {
        //bearer token use case
        UserTokenPayload userData = createUserData("", Arrays.asList("admin", "provision"));
        AuthenticationToken atoken = new BearerToken(tokenCreator.createNewJWT(userData).getToken());
        AuthenticationInfo ai = null;
        try {
            realm.assertCredentialsMatch(atoken, ai);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
        //odl token use case
        atoken = new UsernamePasswordToken("admin", "admin");
        try {
            realm.assertCredentialsMatch(atoken, ai);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testAuthenticationInfo() {
        //bearer token use case
        UserTokenPayload userData = createUserData("", Arrays.asList("admin", "provision"));
        AuthenticationToken atoken = new BearerToken(tokenCreator.createNewJWT(userData).getToken());
        AuthenticationInfo ai = null;
        try {
            ai = realm.doGetAuthenticationInfo(atoken);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
        //odl token use case
        atoken = new UsernamePasswordToken("admin", "admin");
        try {
            ai = realm.doGetAuthenticationInfo(atoken);
        } catch (AuthenticationException e) {
            fail(e.getMessage());
        }
    }

    private static UserTokenPayload createUserData(String username, List<String> roles) {
        UserTokenPayload userData = new UserTokenPayload();
        userData.setExp(tokenCreator.getDefaultExp());
        userData.setFamilyName("");
        userData.setGivenName("");
        userData.setPreferredUsername(username);
        userData.setRoles(roles);
        return userData;
    }

    public static class OAuth2RealmToTest extends OAuth2Realm {

        public OAuth2RealmToTest() throws IOException {
            super();
        }

        @Override
        public AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg) {
            return super.doGetAuthorizationInfo(arg);
        }

        @Override
        public void assertCredentialsMatch(AuthenticationToken atoken, AuthenticationInfo ai)
                throws AuthenticationException {
            super.assertCredentialsMatch(atoken, ai);
        }

        @Override
        public AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
            return super.doGetAuthenticationInfo(token);
        }
    }
}
