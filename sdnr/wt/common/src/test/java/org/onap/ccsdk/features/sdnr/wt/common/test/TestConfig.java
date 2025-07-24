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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.exception.ConversionException;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.filechange.IConfigChangedListener;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.subtypes.Section;

public class TestConfig {

    private static final String TESTFILENAME = "test.properties";
    private static final String TESTKEY1 = "abc";
    private static final String TESTKEY2 = "def";
    private static final String TESTKEY3 = "hhh";
    private static final String TESTKEY4 = "hhv";

    private static final int TESTVALUE1 = 123;
    private static final int TESTVALUE1_2 = 1234;
    private static final boolean TESTVALUE2 = true;
    private static final String TESTCOMMENT1 = "my comment for this value";
    private static final String TESTCOMMENT1_2 = "my comment line 2 for this value";
    private static final String TESTVALUE3 = "http://localhost:2223";
    private static final String TESTVALUE4 = "httasdasdas";
    private static final String TESTCONTENT1 = "	[test]\n" + TESTKEY1 + "=" + TESTVALUE1 + "\n" + "#her a comment\n"
            + TESTKEY2 + "=" + TESTVALUE2 + "\n" + TESTKEY3 + "=" + TESTVALUE3;



    @After
    @Before
    public void init() {
        File f = new File(TESTFILENAME);
        if (f.exists()) {
            f.delete();
        }
    }

    public void write(String filename, String lines) {

        try {
            Files.write(new File(filename).toPath(), lines.getBytes());
        } catch (IOException e) {
            fail("problem writing file " + filename);
        }
    }

    @Test
    public void testRead() {
        this.write(TESTFILENAME, TESTCONTENT1);
        ConfigurationFileRepresentation confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        Section section = confiuration.getSection("test").get();
        assertNotNull(section);
        try {
            assertEquals(TESTVALUE1, section.getInt(TESTKEY1, 0));
            assertEquals(TESTVALUE2, section.getBoolean(TESTKEY2, !TESTVALUE2));
            assertEquals(TESTVALUE3, section.getString(TESTKEY3, ""));
        } catch (ConversionException e) {
            fail(e.getMessage());
        }
        this.init();
    }

    @Test
    public void testWrite() {
        final String SECTIONNAME = "test";
        //write values
        ConfigurationFileRepresentation confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        Section section = confiuration.addSection(SECTIONNAME);

        section.setProperty(TESTKEY1, String.valueOf(TESTVALUE1));
        section.addComment(TESTKEY1, TESTCOMMENT1);
        section.addComment(TESTKEY1, TESTCOMMENT1_2);
        section.setProperty(TESTKEY2, String.valueOf(TESTVALUE2));
        section.setProperty(TESTKEY3, String.valueOf(TESTVALUE3));
        confiuration.save();

        //verify written
        ConfigurationFileRepresentation confiuration2 = new ConfigurationFileRepresentation(TESTFILENAME);

        section = confiuration2.getSection(SECTIONNAME).get();
        assertNotNull(section);
        try {
            assertEquals(TESTVALUE1, section.getInt(TESTKEY1, 0));
            assertEquals(TESTVALUE2, section.getBoolean(TESTKEY2, !TESTVALUE2));
            assertEquals(TESTVALUE3, section.getString(TESTKEY3, ""));
        } catch (ConversionException e) {
            fail(e.getMessage());
        }
        this.init();

        //write directly into base
        confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        section = confiuration.addSection(SECTIONNAME);
        confiuration.setProperty(SECTIONNAME, TESTKEY1, TESTVALUE1);
        confiuration.setProperty(SECTIONNAME, TESTKEY2, TESTVALUE2);
        confiuration.setProperty(SECTIONNAME, TESTKEY3, TESTVALUE3);
        confiuration.save();

        //verify
        confiuration2 = new ConfigurationFileRepresentation(TESTFILENAME);
        section = confiuration2.getSection(SECTIONNAME).get();
        assertNotNull(section);
        assertEquals(TESTVALUE1, confiuration.getPropertyLong(SECTIONNAME, TESTKEY1).get().intValue());
        assertEquals(TESTVALUE2, confiuration.getPropertyBoolean(SECTIONNAME, TESTKEY2));
        assertEquals(TESTVALUE3, confiuration.getProperty(SECTIONNAME, TESTKEY3));
        this.init();


    }

    @Test
    public void testOverwrite() {
        final String SECTIONNAME = "test";
        //write values
        ConfigurationFileRepresentation confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        Section section = confiuration.addSection(SECTIONNAME);

        section.setProperty(TESTKEY1, String.valueOf(TESTVALUE1));
        section.setProperty(TESTKEY2, String.valueOf(TESTVALUE2));
        section.setProperty(TESTKEY3, String.valueOf(TESTVALUE3));
        confiuration.save();

        //verify written
        ConfigurationFileRepresentation confiuration2 = new ConfigurationFileRepresentation(TESTFILENAME);

        section = confiuration2.getSection(SECTIONNAME).get();

        assertNotNull(section);
        try {
            assertEquals(TESTVALUE1, section.getInt(TESTKEY1, 0));
            assertEquals(TESTVALUE2, section.getBoolean(TESTKEY2, !TESTVALUE2));
            assertEquals(TESTVALUE3, section.getString(TESTKEY3, ""));
        } catch (ConversionException e) {
            fail(e.getMessage());
        }

        //write directly into base
        confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        section = confiuration.addSection(SECTIONNAME);
        confiuration.setPropertyIfNotAvailable(SECTIONNAME, TESTKEY1, TESTVALUE1_2);
        confiuration.setPropertyIfNotAvailable(SECTIONNAME, TESTKEY4, TESTVALUE4);

        confiuration.save();

        //verify
        confiuration2 = new ConfigurationFileRepresentation(TESTFILENAME);
        section = confiuration2.getSection(SECTIONNAME).get();
        assertNotNull(section);
        assertEquals(TESTVALUE1, confiuration.getPropertyLong(SECTIONNAME, TESTKEY1).get().intValue());
        assertEquals(TESTVALUE2, confiuration.getPropertyBoolean(SECTIONNAME, TESTKEY2));
        assertEquals(TESTVALUE3, confiuration.getProperty(SECTIONNAME, TESTKEY3));
        assertEquals(TESTVALUE4, confiuration.getProperty(SECTIONNAME, TESTKEY4));
        this.init();


    }

    static boolean changeFlag = false;

    @Test
    public void testChangeListener() {

        changeFlag = false;
        this.init();
        ConfigurationFileRepresentation confiuration = new ConfigurationFileRepresentation(TESTFILENAME);
        IConfigChangedListener listener = new IConfigChangedListener() {

            @Override
            public void onConfigChanged() {
                changeFlag = true;
            }
        };
        confiuration.registerConfigChangedListener(listener);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        this.write(TESTFILENAME, TESTCONTENT1);
        int i = 10;
        while (i-- > 0) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (changeFlag) {
                break;
            }
        }
        confiuration.unregisterConfigChangedListener(listener);
        assertTrue("changelistener not called", changeFlag);
    }

    @Test
    public void testEnvPropert() {
        final String KEY = "basada";
        Section section = new Section("test");
        section.addLine(KEY + "=${USER} in ${HOME}");
        section.parseLines();
        assertTrue(section.getProperty(KEY).length() > " in ".length());
    }
}
