/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPClient;
import org.onap.ccsdk.features.sdnr.wt.common.http.BaseHTTPResponse;

public class TestBaseHttpClient {

    public static final String HTTPMETHOD_GET = "GET";
    public static final String HTTPMETHOD_POST = "POST";
    public static final String HTTPMETHOD_PUT = "PUT";
    public static final String HTTPMETHOD_DELETE = "DELETE";
    public static final String HTTPMETHOD_OPTIONS = "OPTIONS";
    public static final String RESPONSE_GET = "This is the response get";
    public static final String RESPONSE_POST = "This is the response post";
    public static final String RESPONSE_PUT = "This is the response put";
    public static final String RESPONSE_DELETE = "This is the response delete";
    public static final String RESPONSE_OPTIONS = "This is the response options";
    private static final String TESTURI = "/mwtn/test";
    private static HttpServer server;
    private static ExecutorService httpThreadPool;
    private static final int testPort = 54440;

    @Test
    public void test() {
        MyHttpClient httpClient = new MyHttpClient("http://localhost:" + testPort, true);
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", BaseHTTPClient.getAuthorizationHeaderValue("admin", "admin"));
        headers.put("Content-Type", "application/json");
        BaseHTTPResponse response = null;
        try {
            response = httpClient.sendRequest(TESTURI, HTTPMETHOD_GET, null, headers);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertEquals(RESPONSE_GET, response.body);
        try {
            response = httpClient.sendRequest(TESTURI, HTTPMETHOD_POST, "{}", headers);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertTrue(response.isSuccess());
        System.out.println(response.toString());
        assertEquals(RESPONSE_POST, response.body);
        try {
            response = httpClient.sendRequest(TESTURI, HTTPMETHOD_PUT, "{}", headers);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertEquals(RESPONSE_PUT, response.body);
        try {
            response = httpClient.sendRequest(TESTURI, HTTPMETHOD_DELETE, "{}", headers);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertNotNull(response);
        assertEquals(RESPONSE_DELETE, response.body);

    }



    @BeforeClass
    public static void initTestWebserver() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", testPort), 0);
        httpThreadPool = Executors.newFixedThreadPool(5);
        server.setExecutor(httpThreadPool);
        server.createContext(TESTURI, new MyHandler());
        //server.createContext("/", new MyRootHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("http server started");
    }

    @AfterClass
    public static void stopTestWebserver() {
        System.out.println("try to stop server");
        if (server != null) {
            server.stop(0);
            httpThreadPool.shutdownNow();
            System.out.println("http server stopped");
        }
    }

    private class MyHttpClient extends BaseHTTPClient {

        public MyHttpClient(String base, boolean trustAllCerts) {
            super(base, trustAllCerts);
        }

        @Override
        public BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers)
                throws IOException {
            return super.sendRequest(uri, method, body, headers);
        }
    }

    public static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String method = t.getRequestMethod();
            System.out.println(String.format("req received: %s %s", method, t.getRequestURI()));
            OutputStream os = null;
            try {
                if (method.equals(HTTPMETHOD_GET)) {
                    t.sendResponseHeaders(200, RESPONSE_GET.length());
                    os = t.getResponseBody();
                    os.write(RESPONSE_GET.getBytes());
                } else if (method.equals(HTTPMETHOD_POST)) {
                    t.sendResponseHeaders(200, RESPONSE_POST.length());
                    os = t.getResponseBody();
                    os.write(RESPONSE_POST.getBytes());
                } else if (method.equals(HTTPMETHOD_PUT)) {
                    t.sendResponseHeaders(200, RESPONSE_PUT.length());
                    os = t.getResponseBody();
                    os.write(RESPONSE_PUT.getBytes());
                } else if (method.equals(HTTPMETHOD_DELETE)) {
                    t.sendResponseHeaders(200, RESPONSE_DELETE.length());
                    os = t.getResponseBody();
                    os.write(RESPONSE_DELETE.getBytes());
                } else if (method.equals(HTTPMETHOD_OPTIONS)) {
                    t.sendResponseHeaders(200, RESPONSE_OPTIONS.length());
                    //os = t.getResponseBody();
                    //os.write(RESPONSE_OPTIONS.getBytes());
                } else {
                    t.sendResponseHeaders(404, 0);
                }
                System.out.println("req handled successful");

            } catch (Exception e) {
                System.out.println(e.getMessage());
            } finally {
                if (os != null) {
                    os.flush();
                    os.close();
                }
            }
        }
    }
}
