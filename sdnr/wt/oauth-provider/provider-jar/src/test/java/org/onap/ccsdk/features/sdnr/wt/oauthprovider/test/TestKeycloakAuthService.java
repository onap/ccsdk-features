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

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.OAuthProviderConfig;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.KeycloakProviderService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.providers.TokenCreator;

public class TestKeycloakAuthService {

    private static HttpServer server;
    private static ExecutorService httpThreadPool;
    private static KeycloakProviderServiceToTest oauthService;
    private static final int PORT = randomPort(50000, 55000);
    private static final String KEYCLOAKURL = String.format("http://127.0.0.1:%d", PORT);
    private static final String OAUTH_SECRET = "oauthsecret";
    private static final String TOKENCREATOR_SECRET = "secret";
    private static final String REDIRECT_URI = "/odlux/token?";

    @BeforeClass
    public static void init() {

        TokenCreator tokenCreator = TokenCreator.getInstance(TOKENCREATOR_SECRET, "issuer");
        OAuthProviderConfig config =
                new OAuthProviderConfig("kc", KEYCLOAKURL, "odlux.app", OAUTH_SECRET, "openid", "keycloak test");
        oauthService = new KeycloakProviderServiceToTest(config, REDIRECT_URI, tokenCreator);
        try {
            initKeycloakTestWebserver(PORT, "/");
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @AfterClass
    public static void close() {
        stopTestWebserver();
    }

    @Test
    public void test() {
        HttpServletRequest req;
        HttpServletResponse resp = null;
        String host = "http://localhost:8412";
        final String state = "stateabc";
        try {
            req = mock(HttpServletRequest.class);
            resp = mock(HttpServletResponse.class);
            when(req.getParameter("code")).thenReturn("abcdefg");
            when(req.getParameter("state")).thenReturn(state);
            oauthService.handleRedirect(req, resp, host);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        verify(resp).setStatus(302);
        //verify(resp).setHeader("Location",any(String.class));
    }

    public void test2() {
        oauthService.sendLoginRedirectResponse(null, null);
    }

    public static class KeycloakProviderServiceToTest extends KeycloakProviderService {

        public KeycloakProviderServiceToTest(OAuthProviderConfig config, String redirectUri, TokenCreator tokenCreator) {
            super(config, redirectUri, tokenCreator);
        }
    }

    private static int randomPort(int min, int max) {
        Random random = new Random();
        return random.nextInt(max + 1 - min) + min;
    }

    public static void initKeycloakTestWebserver(int port, String baseUri) throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        httpThreadPool = Executors.newFixedThreadPool(5);
        server.setExecutor(httpThreadPool);
        server.createContext(baseUri, new MyHandler());
        //server.createContext("/", new MyRootHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("http server started");
    }

    public static void stopTestWebserver() {
        if (server != null) {
            server.stop(0);
            httpThreadPool.shutdownNow();
            System.out.println("http server stopped");
        }
    }

    private static String loadResourceFileContent(String filename) {
        try {
            return Files.readString(new File(filename).toPath());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return null;
    }
    public static class MyHandler implements HttpHandler {
        private static final String KEYCLOAK_TOKEN_ENDPOINT = "/auth/realms/onap/protocol/openid-connect/token";
        private static final String KEYCLOAK_TOKEN_RESPONSE = loadResourceFileContent("src/test/resources/oauth/keycloak-token-response.json");

        @Override
        public void handle(HttpExchange t) throws IOException {
            final String method = t.getRequestMethod();
            final String uri = t.getRequestURI().toString();
            System.out.println(String.format("req received: %s %s", method, t.getRequestURI()));
            OutputStream os = null;
            try {
               if (method.equals("POST")) {
                    if(uri.equals(KEYCLOAK_TOKEN_ENDPOINT)){
                        t.sendResponseHeaders(200, KEYCLOAK_TOKEN_RESPONSE.length());
                        os = t.getResponseBody();
                        os.write(KEYCLOAK_TOKEN_RESPONSE.getBytes());
                    }
                    else {
                        t.sendResponseHeaders(404, 0);
                    }
                } else {
                    t.sendResponseHeaders(404, 0);
                }
                System.out.println("req handled successful");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (os != null) {
                    os.close();
                }
            }
        }
    }
}
