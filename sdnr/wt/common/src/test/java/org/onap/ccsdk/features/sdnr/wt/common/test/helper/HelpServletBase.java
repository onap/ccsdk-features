/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2018 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.common.test.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.onap.ccsdk.features.sdnr.wt.common.test.ServletInputStreamFromByteArrayInputStream;
import org.onap.ccsdk.features.sdnr.wt.common.test.ServletOutputStreamToStringWriter;

public class HelpServletBase {

    public static final String RESPONSE_GET = "This is the response get";
    public static final String RESPONSE_POST = "This is the response post";
    public static final String RESPONSE_PUT = "This is the response put";
    public static final String RESPONSE_DELETE = "This is the response delete";
    public static final String RESPONSE_OPTIONS = "This is the response options";

    public static final String HTTPMETHOD_GET = "GET";
    public static final String HTTPMETHOD_POST = "POST";
    public static final String HTTPMETHOD_PUT = "PUT";
    public static final String HTTPMETHOD_DELETE = "DELETE";
    public static final String HTTPMETHOD_OPTIONS = "OPTIONS";
    private IPublicServlet servlet;
    private static HttpServer server;
    private static ExecutorService httpThreadPool;

    public final String HOST = "localhost";
    protected static int testPort;
    private final String baseUri;
    protected static final String LR = "\n";

    public HelpServletBase(String baseuri, int port) {
        this.baseUri = baseuri;
        testPort = port;
    }

    public void setServlet(IPublicServlet s) {
        this.servlet = s;
    }

    protected void testrequest(String method, String data, String expectedResponse, boolean exact) {
        this.testrequest("/mwtn/test", method, data, expectedResponse, exact, null);
    }

    protected void testrequest(String uri, String method, String data, String expectedResponse, boolean exact) {
        this.testrequest(uri, method, data, expectedResponse, exact, null);
    }

    protected void testrequest(String uri, String method, String data, String expectedResponse, boolean exact,
            Map<String, String> headersToCheck) {

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        HttpServletResponse mockResponse = mock(HttpServletResponse.class);

        ServletOutputStreamToStringWriter printOut = new ServletOutputStreamToStringWriter();
        ServletInputStreamFromByteArrayInputStream inputStream = new ServletInputStreamFromByteArrayInputStream(data.getBytes());
        Vector<String> headers = new Vector<String>();
        headers.addElement("Accept");
        headers.add("User-Agent");
        Enumeration<String> headerNames = headers.elements();
        try {
            when(mockRequest.getRequestURI()).thenReturn(this.baseUri + uri);
            when(mockRequest.getHeaderNames()).thenReturn(headerNames);
            when(mockRequest.getHeader("Accept")).thenReturn("application/json");
            when(mockRequest.getHeader("User-Agent")).thenReturn("Gecko abc");
            when(mockRequest.getInputStream()).thenReturn(inputStream);
            when(mockResponse.getOutputStream()).thenReturn(printOut);
            System.out.println("do a " + method + " request");
            if (method == HTTPMETHOD_GET)
                this.servlet.doGet(mockRequest, mockResponse);
            else if (method == HTTPMETHOD_POST)
                this.servlet.doPost(mockRequest, mockResponse);
            else if (method == HTTPMETHOD_PUT)
                this.servlet.doPut(mockRequest, mockResponse);
            else if (method == HTTPMETHOD_DELETE)
                this.servlet.doDelete(mockRequest, mockResponse);
            else if (method == HTTPMETHOD_OPTIONS)
                this.servlet.doOptions(mockRequest, mockResponse);
            else
                fail("http request method " + method + " test not implemented");
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        verify(mockResponse).setStatus(200);
        if (exact)
            assertEquals(expectedResponse, printOut.getStringWriter().toString());
        else
            assertTrue("response not for method " + method + "correct", printOut.getStringWriter().toString().contains(expectedResponse));
        // currently unable to check extra headers
        if (headersToCheck != null) {

        }
    }

    @Before
    private void init() throws IOException {


        initEsTestWebserver(testPort);
    }

    @After
    private void deinit() {
        stopTestWebserver();
    }

    public static void initEsTestWebserver(int port) throws IOException {
        initEsTestWebserver(port, "/mwtn/test");
    }

    public static void initEsTestWebserver(int port, String baseUri) throws IOException {
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
                    os.close();
                }
            }
        }
    }
}
