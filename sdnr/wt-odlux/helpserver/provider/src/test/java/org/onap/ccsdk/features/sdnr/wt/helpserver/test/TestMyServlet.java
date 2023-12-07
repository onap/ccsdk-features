/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.helpserver.test;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.helpserver.HelpServlet;


public class TestMyServlet extends Mockito {

    private static final String GETHELPDIRECTORYBASE = "data";
    private static final String CONTENT = "abbccdfkamaosie aksdmais";

    public static void createHelpFile(String filename, String content) {
        File file = new File("bitnami/nginx/help" + filename);
        File folder = file.getParentFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            if (file.exists()) {
                file.delete();
            }
            Files.write(file.toPath(), content.getBytes(),
                    new OpenOption[] {WRITE, CREATE_NEW, CREATE, TRUNCATE_EXISTING});
        } catch (IOException e1) {
            fail(e1.getMessage());
        }
    }

//    @Before
//    public void init() {
//        try {
//            ExtactBundleResource.deleteRecursively(new File(GETHELPDIRECTORYBASE));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }


   // @Test We dont have implementation of meta in HelpServlet
    public void testServlet() throws Exception {

        System.out.println("Test get");

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("help/");
        when(request.getQueryString()).thenReturn("?meta");

        ServletOutputStreamToStringWriter out = new ServletOutputStreamToStringWriter();
        when(response.getOutputStream()).thenReturn(out);

        HelpServlet helpServlet = null;
        try {
            helpServlet = new HelpServlet();
            System.out.println("Server created");
            createHelpFile("/meta.json", CONTENT);

            helpServlet.doOptions(request, response);
            System.out.println("Get calling");
            helpServlet.doGet(request, response);
            System.out.println("Get called");
        } catch (Exception e) {
            fail(e.getMessage());
        }
        if (helpServlet != null) {
            helpServlet.close();
        }

        String result = out.getStringWriter().toString().trim();
        System.out.println("Result: '" + result + "'");
        assertEquals(CONTENT, result);
    }

    @Test
    public void testServlet2() {
        this.testGetRequest("test/test.txt");
        this.testGetRequest("test.css");
        this.testGetRequest("test.eps");
        this.testGetRequest("test.pdf");
    }

    private void testGetRequest(String fn) {
        HelpServlet helpServlet = new HelpServlet();
        createHelpFile("/" + fn, CONTENT);
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);

        when(request.getRequestURI()).thenReturn("help/" + fn);
        ServletOutputStreamToStringWriter out = new ServletOutputStreamToStringWriter();
        try {
            when(response.getOutputStream()).thenReturn(out);
            helpServlet.doGet(request, response);
        } catch (ServletException | IOException e) {
            fail(e.getMessage());
        }
        try {
            out.close();
        } catch (Exception e) {
        }
        try {
            helpServlet.close();
        } catch (Exception e) {
        }

        assertEquals("compare content for " + fn, CONTENT, out.getStringWriter().toString().trim());
    }
    
    public class ServletOutputStreamToStringWriter extends ServletOutputStream {

        // variables
        private StringWriter out = new StringWriter();
        // end of variables

        public StringWriter getStringWriter() {
            return out;
        }

        @Override
        public void write(int arg0) throws IOException {
            out.write(arg0);
        }

        @Override
        public String toString() {
            return out.toString();
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }


    }

}
