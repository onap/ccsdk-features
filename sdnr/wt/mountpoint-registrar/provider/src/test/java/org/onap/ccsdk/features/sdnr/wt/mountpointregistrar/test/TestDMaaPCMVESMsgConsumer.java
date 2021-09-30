/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2021 Samsung Electronics Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.DMaaPCMVESMsgConsumer;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.InvalidMessageException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestDMaaPCMVESMsgConsumer {

    private static final String CONFIGURATIONFILE = "cm_test.properties";
    private DMaaPCMVESMsgConsumer dMaaPCMVESMsgConsumer;
    private GeneralConfigForTest generalConfigForTest;

    @Before
    public void setUp() throws Exception {
        generalConfigForTest = new GeneralConfigForTest(CONFIGURATIONFILE);
        dMaaPCMVESMsgConsumer = new DMaaPCMVESMsgConsumer(generalConfigForTest.getCfg());
    }

    @Test
    public void processValidMsg() throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileValid = new File(TestDMaaPCMVESMsgConsumer.class.getResource("/msgs/cm_valid.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        dMaaPCMVESMsgConsumer.processMsg(cmEvent);
    }

    @Test(expected = InvalidMessageException.class)
    public void processMsgThatMissesField() throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileInvalid = new File(TestDMaaPCMVESMsgConsumer.class.getResource("/msgs/cm_invalid.json").toURI());
        String cmEvent = readFileToString(cmFileInvalid);
        dMaaPCMVESMsgConsumer.processMsg(cmEvent);
    }

    @Test(expected = JsonProcessingException.class)
    public void processMsgThatIsNotValidJson() throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileInvalid = new File(TestDMaaPCMVESMsgConsumer.class.getResource("/msgs/not_a_json.json").toURI());
        String cmEvent = readFileToString(cmFileInvalid);
        dMaaPCMVESMsgConsumer.processMsg(cmEvent);
    }

    private String readFileToString(File file) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        Files.lines(Paths.get(file.toURI())).forEach(fileContent::append);
        return fileContent.toString();
    }

    @After
    public void after() {
        generalConfigForTest.close();
    }
}