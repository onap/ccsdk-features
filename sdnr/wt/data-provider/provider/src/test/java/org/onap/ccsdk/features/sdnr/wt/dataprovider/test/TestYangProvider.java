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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.test.ServletOutputStreamToStringWriter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.yangschema.YangFileProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.yangschema.YangFilename;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.http.yangschema.YangSchemaHttpServlet;

public class TestYangProvider {

    private static final String TESTPATH = "cache/schema/";
    private static final String TESTPATH_BASE = "cache/";
    private static final String TESTPATH_SEPERATE_1 = TESTPATH_BASE+"abc1/";


    @BeforeClass
    public static void init() {
        Set<PosixFilePermission> perms;
        FileAttribute<?> attr;
        perms = EnumSet.noneOf(PosixFilePermission.class);

        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);

        attr = PosixFilePermissions.asFileAttribute(perms);
        try {
            Files.createDirectories(new File(TESTPATH).toPath(), attr);
            new File(TESTPATH + new YangFilename("module1", "2010-01-01").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module2", "2010-01-01").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module2", "2010-04-01").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module3", "2010-01-01").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module4", "2010-05-01").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module5", "2010-01-11").getFilename()).createNewFile();
            new File(TESTPATH + new YangFilename("module6", "2010-01-01").getFilename()).createNewFile();
            Files.createDirectories(new File(TESTPATH_SEPERATE_1).toPath(), attr);
            new File(TESTPATH_SEPERATE_1 + new YangFilename("module7", "2011-01-01").getFilename()).createNewFile();
            } catch (IOException | ParseException e) {

        }
    }

    @AfterClass
    public static void deinit() {
        try {
            Files.walk(new File("cache").toPath()).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    @Test
    public void testExisting() {
        YangFileProvider provider = new YangFileProvider(TESTPATH_BASE, TESTPATH);
        assertTrue(provider.hasFileForModule("module1", "2010-01-01"));
        assertTrue(provider.hasFileForModule("module2"));
        assertTrue(provider.hasFileForModule("module3"));
        assertFalse(provider.hasFileForModule("module5", "2010-01-01"));
        assertTrue(provider.hasFileForModule("module7"));
    }

    @Test
    public void testRevision() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        YangFileProvider provider = new YangFileProvider(TESTPATH_BASE, TESTPATH);
        YangFilename f1 = provider.getFileForModule("module1", "2010-01-01");
        assertEquals("module1", f1.getModule());
        assertEquals(sdf.parse("2010-01-01"), f1.getRevision());
        YangFilename f2 = provider.getFileForModule("module2");
        assertEquals("module2", f2.getModule());
        assertEquals(sdf.parse("2010-04-01"), f2.getRevision());
        f2 = provider.getFileForModule("module2", "2010-02-01");
        assertEquals("module2", f2.getModule());
        assertEquals(sdf.parse("2010-04-01"), f2.getRevision());
        YangFilename f3 = provider.getFileForModule("module3");
        assertEquals("module3", f3.getModule());
        assertEquals(sdf.parse("2010-01-01"), f3.getRevision());
        f3 = provider.getFileForModule("module3", "2010-04-01");
        assertNull(f3);
    }

    @Test
    public void testServlet() throws IOException, ServletException {
        HelpYangSchemaHttpServlet servlet = new HelpYangSchemaHttpServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn("/yang-schema/module1");
        ServletOutputStreamToStringWriter printOut = new ServletOutputStreamToStringWriter();
        when(resp.getOutputStream()).thenReturn(printOut);
        servlet.doGet(req, resp);
        verify(resp).setStatus(200);
        verify(resp).setContentType("text/plain");

    }

    @Test
    public void testServletBad() throws IOException, ServletException {
        HelpYangSchemaHttpServlet servlet = new HelpYangSchemaHttpServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn("/yang-schema/module1/2020-01-01");
        ServletOutputStreamToStringWriter printOut = new ServletOutputStreamToStringWriter();
        when(resp.getOutputStream()).thenReturn(printOut);
        servlet.doGet(req, resp);
        verify(resp).sendError(HttpServletResponse.SC_NOT_FOUND);

    }

    @Test
    public void testServletNear() throws IOException, ServletException {
        HelpYangSchemaHttpServlet servlet = new HelpYangSchemaHttpServlet();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getRequestURI()).thenReturn("/yang-schema/module2/2010-03-01");
        ServletOutputStreamToStringWriter printOut = new ServletOutputStreamToStringWriter();
        when(resp.getOutputStream()).thenReturn(printOut);
        servlet.doGet(req, resp);
        verify(resp).setStatus(200);
        verify(resp).setContentType("text/plain");

    }

    private static class HelpYangSchemaHttpServlet extends YangSchemaHttpServlet {

        /**
         *
         */
        private static final long serialVersionUID = 1L;

        @Override
        public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            super.doGet(req, resp);
        }
    }


}
