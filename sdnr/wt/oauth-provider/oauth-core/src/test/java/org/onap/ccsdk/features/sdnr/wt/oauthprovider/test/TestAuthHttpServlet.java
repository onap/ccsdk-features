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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.util.concurrent.FluentFuture;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.authc.BearerToken;
import org.jolokia.osgi.security.Authenticator;
import org.json.JSONArray;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.test.ServletOutputStreamToByteArrayOutputStream;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.Config;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.CustomObjectMapper;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OdlPolicy;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UserTokenPayload;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.test.helper.OdlJsonMapper;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.test.helper.OdlXmlMapper;
import org.opendaylight.aaa.api.Claim;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.aaa.api.PasswordCredentialAuth;
import org.opendaylight.aaa.api.PasswordCredentials;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.ReadTransaction;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.HttpAuthorization;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.aaa.rev161214.http.authorization.Policies;
import org.opendaylight.yangtools.util.concurrent.FluentFutures;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;


public class TestAuthHttpServlet {

    private static final String TESTCONFIGFILE = TestConfig.TEST_CONFIG_FILENAME;
    private static final String TESTSHIROCONFIGFILE = "src/test/resources/aaa-app-config.test.xml";
    private static final String MDSALDYNAUTHFILENAME = "src/test/resources/mdsalDynAuthData.json";
    private static TestServlet servlet;
    private static DataBroker dataBroker = loadDynamicMdsalAuthDataBroker();
    private static Authenticator odlAuthenticator = mock(Authenticator.class);
    private static IdMService odlIdentityService = mock(IdMService.class);
    private static PasswordCredentialAuth passwordCredentialAuth;
    private static TokenCreator tokenCreator;
//    private static final HttpServletRequest authreq = new HeadersOnlyHttpServletRequest(
//            Map.of("Authorization", BaseHTTPClient.getAuthorizationHeaderValue("admin@sdn", "admin")));

    @BeforeClass
    public static void init() throws IllegalArgumentException, Exception {

        try {
            Config config = createConfigFile();
            tokenCreator = TokenCreator.getInstance(config);
            servlet = new TestServlet();
        } catch (IOException | InvalidConfigurationException e) {
            fail(e.getMessage());
        }
        servlet.setDataBroker(dataBroker);
        passwordCredentialAuth = mock(PasswordCredentialAuth.class);

        servlet.setPasswordCredentialAuth(passwordCredentialAuth);
    }

    private static DataBroker loadDynamicMdsalAuthDataBroker() {
        DataBroker dataBroker = mock(DataBroker.class);
        ReadTransaction rotx = mock(ReadTransaction.class);
        InstanceIdentifier<Policies> iif = InstanceIdentifier.create(HttpAuthorization.class).child(Policies.class);
        try {
            when(rotx.read(LogicalDatastoreType.CONFIGURATION, iif))
                    .thenReturn(loadDataBrokerFile(MDSALDYNAUTHFILENAME, Policies.class));
        } catch (IOException e) {
            fail("problem init databroker read" + e.getMessage());
        }
        when(dataBroker.newReadOnlyTransaction()).thenReturn(rotx);
        return dataBroker;
    }

    private static <T> FluentFuture<Optional<T>> loadDataBrokerFile(String fn, Class<T> clazz) throws IOException {
        return FluentFutures.immediateFluentFuture(Optional.ofNullable(readJson(new File(fn), clazz)));
    }

    private static ShiroConfiguration loadShiroConfig(String filename)
            throws JsonParseException, JsonMappingException, IOException {
        OdlXmlMapper mapper = new OdlXmlMapper();
        return mapper.readValue(new File(filename), ShiroConfigurationBuilder.class).build();
    }

    private static Config createConfigFile() throws IOException, InvalidConfigurationException {
        return Config.getInstance(TESTCONFIGFILE);

    }

    @Test
    public void testValidLoginRedirect() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/login/keycloak");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        try {
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(302);
        verify(resp).setHeader("Location",
                "http://10.20.11.160:8080/auth/realms/onap/protocol/openid-connect/auth?client_id=odlux.app&response"
                        + "_type=code&scope=openid&redirect_uri=http%3A%2F%2Fnasp.diasf.de%2Foauth%2Fredirect%2Fkeycloak");
    }

    @Test
    public void testInValidLoginRedirect() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/login/unknownproviderid");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(404);
    }

    @Test
    public void testValidLogin() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/login");
        when(req.getParameter("username")).thenReturn("admin");
        when(req.getParameter("password")).thenReturn("admin");
        Claim claim = new Claim() {
            @Override
            public String clientId() {
                return "admin";
            }

            @Override
            public String userId() {
                return "admin";
            }

            @Override
            public String user() {
                return null;
            }

            @Override
            public String domain() {
                return "sdn";
            }

            @Override
            public Set<String> roles() {
                return Set.of("admin");
            }
        };
        when(passwordCredentialAuth.authenticate(any(PasswordCredentials.class))).thenReturn(claim);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doPost(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(200);
    }

    @Test
    public void testGetProviders() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/providers");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(200);
        String responseBody = printOut.getByteArrayOutputStream().toString(StandardCharsets.UTF_8);
        System.out.println(responseBody);
        JSONArray a = new JSONArray(responseBody);
        assertEquals(1, a.length());
        assertEquals("keycloak", a.getJSONObject(0).getString("id"));
        assertEquals("OSNL Keycloak Provider", a.getJSONObject(0).getString("title"));
        assertEquals("/oauth/login/keycloak", a.getJSONObject(0).getString("loginUrl"));

    }

    @Test
/*
    @Ignore
*/
    public void testPoliciesAnon() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/policies");
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(200);
        String responseBody = printOut.getByteArrayOutputStream().toString(StandardCharsets.UTF_8);
        System.out.println(responseBody);
        OdlPolicy[] anonPolicies = null;
        try {
            anonPolicies = readJson(responseBody, OdlPolicy[].class);
        } catch (JsonProcessingException e) {
            fail("unable to read anon policies response");
        }
        assertEquals(9, anonPolicies.length);
        OdlPolicy pApidoc = find(anonPolicies, "/apidoc/**");
        assertNotNull(pApidoc);
        assertAllEquals(false, pApidoc);
        OdlPolicy pOauth = find(anonPolicies, "/oauth/**");
        assertNotNull(pOauth);
        assertAllEquals(true, pOauth);
        OdlPolicy pRestconf = find(anonPolicies, "/rests/**");
        assertNotNull(pRestconf);
        assertAllEquals(false, pRestconf);
    }

    @Test
    public void testPoliciesBasicAuth() {

        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/policies");
        when(req.getHeader("Authorization")).thenReturn(BaseHTTPClient.getAuthorizationHeaderValue("admin", "admin"));
        when(odlIdentityService.listRoles("admin@sdn", "sdn")).thenReturn(Arrays.asList("admin"));
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(200);
        String responseBody = printOut.getByteArrayOutputStream().toString(StandardCharsets.UTF_8);
        System.out.println(responseBody);
        OdlPolicy[] anonPolicies = null;
        try {
            anonPolicies = readJson(responseBody, OdlPolicy[].class);
        } catch (JsonProcessingException e) {
            fail("unable to read anon policies response");
        }
        assertEquals(9, anonPolicies.length);
        OdlPolicy pApidoc = find(anonPolicies, "/apidoc/**");
        assertNotNull(pApidoc);
        assertAllEquals(false, pApidoc);
        OdlPolicy pOauth = find(anonPolicies, "/oauth/**");
        assertNotNull(pOauth);
        assertAllEquals(true, pOauth);
        OdlPolicy pRestconf = find(anonPolicies, "/rests/**");
        assertNotNull(pRestconf);
        assertAllEquals(false, pRestconf);
    }

    @Test
    public void testPoliciesBearer() {
        HttpServletRequest req = mock(HttpServletRequest.class);
        when(req.getRequestURI()).thenReturn("/oauth/policies");
        String token = createToken("admin", Arrays.asList("admin", "provision")).getToken();
        when(req.getHeader("Authorization")).thenReturn(String.format("Bearer %s", token));
        HttpServletResponse resp = mock(HttpServletResponse.class);
        ServletOutputStreamToByteArrayOutputStream printOut = new ServletOutputStreamToByteArrayOutputStream();
        try {
            when(resp.getOutputStream()).thenReturn(printOut);
            servlet.doGet(req, resp);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(200);
        String responseBody = printOut.getByteArrayOutputStream().toString(StandardCharsets.UTF_8);
        System.out.println(responseBody);
        OdlPolicy[] anonPolicies = null;
        try {
            anonPolicies = readJson(responseBody, OdlPolicy[].class);
        } catch (JsonProcessingException e) {
            fail("unable to read anon policies response");
        }
        assertEquals(9, anonPolicies.length);
        OdlPolicy pApidoc = find(anonPolicies, "/apidoc/**");
        assertNotNull(pApidoc);
        assertAllEquals(false, pApidoc);
        OdlPolicy pOauth = find(anonPolicies, "/oauth/**");
        assertNotNull(pOauth);
        assertAllEquals(true, pOauth);
        OdlPolicy pRestconf = find(anonPolicies, "/rests/**");
        assertNotNull(pRestconf);
        assertAllEquals(true, pRestconf);
    }

    private static BearerToken createToken(String username, List<String> roles) {
        UserTokenPayload data = new UserTokenPayload();
        data.setPreferredUsername(username);
        data.setFamilyName("");
        data.setGivenName(username);
        data.setExp(tokenCreator.getDefaultExp());
        data.setRoles(roles);
        return tokenCreator.createNewJWT(data);
    }

    private static void assertAllEquals(boolean b, OdlPolicy p) {
        assertEquals(b, p.getMethods().isGet());
        assertEquals(b, p.getMethods().isPost());
        assertEquals(b, p.getMethods().isPut());
        assertEquals(b, p.getMethods().isDelete());
        assertEquals(b, p.getMethods().isPatch());
    }

    private static OdlPolicy find(OdlPolicy[] policies, String path) {
        for (OdlPolicy p : policies) {
            if (path.equals(p.getPath())) {
                return p;
            }
        }
        return null;
    }

    private static <T> T readJson(String data, Class<T> clazz) throws JsonMappingException, JsonProcessingException {
        CustomObjectMapper mapper = new CustomObjectMapper();
        return mapper.readValue(data, clazz);
    }

    private static <T> T readJson(File file, Class<T> clazz) throws IOException {
        OdlJsonMapper mapper = new OdlJsonMapper();
        return mapper.readValue(file, clazz);
    }

    private static class TestServlet extends AuthHttpServlet {

        private static final long serialVersionUID = 1L;

        public TestServlet() throws IllegalArgumentException, Exception {
            super(TESTSHIROCONFIGFILE);
        }

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }

        @Override
        public void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doPost(req, resp);
        }
    }
}
