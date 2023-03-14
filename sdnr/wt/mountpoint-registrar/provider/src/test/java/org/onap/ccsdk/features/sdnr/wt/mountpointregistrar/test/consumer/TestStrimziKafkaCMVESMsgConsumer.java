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

package org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.impl.InvalidMessageException;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.test.config.GeneralConfigForTest;
import org.onap.ccsdk.features.sdnr.wt.mountpointregistrar.vesdomain.cm.StrimziKafkaCMVESMsgConsumer;

public class TestStrimziKafkaCMVESMsgConsumer {

    private static final String CONFIGURATION_FILE = "cm_test.properties";
    private StrimziKafkaCMVESMsgConsumer sKafkaCMVESMsgConsumer;
    private GeneralConfigForTest generalConfigForTest;

    @Before
    public void setUp() throws Exception {
        generalConfigForTest = new GeneralConfigForTest(CONFIGURATION_FILE);
        sKafkaCMVESMsgConsumer = new StrimziKafkaCMVESMsgConsumer(generalConfigForTest.getCfg());
    }

    @Test
    public void processValidMsg() throws URISyntaxException, IOException {
        File cmFileValid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_valid.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            sKafkaCMVESMsgConsumer.processMsg(cmEvent);
        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    @Test(expected = InvalidMessageException.class)
    public void processMsgThatMissesField() throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileInvalid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_invalid.json").toURI());
        String cmEvent = readFileToString(cmFileInvalid);
        sKafkaCMVESMsgConsumer.processMsg(cmEvent);
    }

    @Test(expected = InvalidMessageException.class)
    public void processMsgThatHasInvalidNotificationType()
        throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileInvalid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_invalid_type.json").toURI());
        String cmEvent = readFileToString(cmFileInvalid);
        sKafkaCMVESMsgConsumer.processMsg(cmEvent);
    }

    @Test(expected = JsonProcessingException.class)
    public void processMsgThatIsNotValidJson() throws URISyntaxException, IOException, InvalidMessageException {
        File cmFileInvalid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/not_a_json.json").toURI());
        String cmEvent = readFileToString(cmFileInvalid);
        sKafkaCMVESMsgConsumer.processMsg(cmEvent);
    }

    @Test
    public void processMsgWithOneElementMoiChangesArray() throws URISyntaxException, IOException {
        File cmFileValid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_valid.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            JsonNode rootNode = convertMessageToJsonNode(cmEvent);
            Iterator<JsonNode> nodes = rootNode
                .at("/event/stndDefinedFields/data/moiChanges")
                .elements();
            Map<String, String> payloadMap =
                sKafkaCMVESMsgConsumer.preparePayloadMapFromMoiChangesArray(rootNode, nodes);

            assertEquals("samsung-O-DU-1122", payloadMap.get("@node-id@"));
            assertEquals("0", payloadMap.get("@counter@"));
            assertEquals("2019-01-09T12:30:07.722Z", payloadMap.get("@timestamp@"));
            assertEquals("src_device_id_1732f1ad-53fd-4fd1-8b73-a677987d4e8f", payloadMap.get("@object-id@"));
            assertEquals("notifyMOIChanges", payloadMap.get("@notification-type@"));
            assertEquals("123", payloadMap.get("@notification-id@"));
            assertEquals("MANAGEMENT_OPERATION", payloadMap.get("@source-indicator@"));
            assertEquals("https://samsung.com/3GPP/simulation/network-function/ves=1", payloadMap.get("@path@"));
            assertEquals("REPLACE", payloadMap.get("@operation@"));
            assertEquals("{pnf-registration:true,faults-enabled:true}", payloadMap.get("@value@"));

        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    @Test
    public void processMsgWithTwoElementMoiChangesArray() throws URISyntaxException, IOException {
        File cmFileValid =
            new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_valid_two_element_moi_changes_array.json")
                .toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            JsonNode rootNode = convertMessageToJsonNode(cmEvent);
            Iterator<JsonNode> nodes = rootNode
                .at("/event/stndDefinedFields/data/moiChanges")
                .elements();
            Map<String, String> payloadMap =
                sKafkaCMVESMsgConsumer.preparePayloadMapFromMoiChangesArray(rootNode, nodes);

            assertEquals("samsung-O-DU-1122", payloadMap.get("@node-id@"));
            assertEquals("0", payloadMap.get("@counter@"));
            assertEquals("2019-01-09T12:30:07.722Z", payloadMap.get("@timestamp@"));
            assertEquals("src_device_id_1732f1ad-53fd-4fd1-8b73-a677987d4e8f", payloadMap.get("@object-id@"));
            assertEquals("notifyMOIChanges", payloadMap.get("@notification-type@"));
            assertEquals("123", payloadMap.get("@notification-id@"));
            assertEquals("MANAGEMENT_OPERATION", payloadMap.get("@source-indicator@"));
            assertEquals("https://samsung.com/3GPP/simulation/network-function/ves=1", payloadMap.get("@path@"));
            assertEquals("REPLACE", payloadMap.get("@operation@"));
            assertEquals("{pnf-registration:true,faults-enabled:true}", payloadMap.get("@value@"));

            Map<String, String> payloadMap2 = null;
            while (nodes.hasNext()) {
                payloadMap2 = sKafkaCMVESMsgConsumer.preparePayloadMapFromMoiChangesArray(rootNode, nodes);
            }
            assertEquals("samsung-O-DU-1122", payloadMap2.get("@node-id@"));
            assertEquals("124", payloadMap2.get("@notification-id@"));
            assertEquals("RESOURCE_OPERATION", payloadMap2.get("@source-indicator@"));
            assertEquals("https://samsung.com/3GPP/simulation/network-function/ves=2", payloadMap2.get("@path@"));
            assertEquals("CREATE", payloadMap2.get("@operation@"));
            assertEquals("{pnf-registration:false,faults-enabled:false}", payloadMap2.get("@value@"));

        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    @Test
    public void processMsgNotifyMoiCreationType() throws URISyntaxException, IOException {
        File cmFileValid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_moi_creation.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            JsonNode rootNode = convertMessageToJsonNode(cmEvent);
            Map<String, String> payloadMap = sKafkaCMVESMsgConsumer.preparePayloadMapFromMoi(rootNode,"/event/stndDefinedFields/data/attributeList");
            assertEquals("samsung-O-DU-1122", payloadMap.get("@node-id@"));
            assertEquals("0", payloadMap.get("@counter@"));
            assertEquals("2019-01-09T12:30:07.722Z", payloadMap.get("@timestamp@"));
            assertEquals("src_device_id_1732f1ad-53fd-4fd1-8b73-a677987d4e8f", payloadMap.get("@object-id@"));
            assertEquals("notifyMOICreation", payloadMap.get("@notification-type@"));
            assertNull(payloadMap.get("@notification-id@"));
            assertEquals("MANAGEMENT_OPERATION", payloadMap.get("@source-indicator@"));
            assertNull(payloadMap.get("@path@"));
            assertEquals("NULL", payloadMap.get("@operation@"));
            assertEquals("{pnf-registration:true,faults-enabled:true}", payloadMap.get("@value@"));

        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    @Test
    public void processMsgNotifyMoiDeletionType() throws URISyntaxException, IOException {
        File cmFileValid = new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_moi_deletion.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            JsonNode rootNode = convertMessageToJsonNode(cmEvent);
            Map<String, String> payloadMap = sKafkaCMVESMsgConsumer.preparePayloadMapFromMoi(rootNode,"/event/stndDefinedFields/data/attributeList");
            assertEquals("samsung-O-DU-1122", payloadMap.get("@node-id@"));
            assertEquals("0", payloadMap.get("@counter@"));
            assertEquals("2019-01-09T12:30:07.722Z", payloadMap.get("@timestamp@"));
            assertEquals("src_device_id_1732f1ad-53fd-4fd1-8b73-a677987d4e8f", payloadMap.get("@object-id@"));
            assertEquals("notifyMOIDeletion", payloadMap.get("@notification-type@"));
            assertNull(payloadMap.get("@notification-id@"));
            assertEquals("MANAGEMENT_OPERATION", payloadMap.get("@source-indicator@"));
            assertNull(payloadMap.get("@path@"));
            assertEquals("NULL", payloadMap.get("@operation@"));
            assertEquals("{pnf-registration:true,faults-enabled:true}", payloadMap.get("@value@"));

        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    @Test
    public void processMsgNotifyMoiAttributeValueChangesType() throws URISyntaxException, IOException {
        File cmFileValid =
            new File(TestStrimziKafkaCMVESMsgConsumer.class.getResource("/msgs/cm_moi_attribute_value_changes.json").toURI());
        String cmEvent = readFileToString(cmFileValid);
        try {
            JsonNode rootNode = convertMessageToJsonNode(cmEvent);
            Map<String, String> payloadMap = sKafkaCMVESMsgConsumer.preparePayloadMapFromMoi(rootNode,"/event/stndDefinedFields/data/attributeListValueChanges");
            assertEquals("samsung-O-DU-1122", payloadMap.get("@node-id@"));
            assertEquals("0", payloadMap.get("@counter@"));
            assertEquals("2019-01-09T12:30:07.722Z", payloadMap.get("@timestamp@"));
            assertEquals("src_device_id_1732f1ad-53fd-4fd1-8b73-a677987d4e8f", payloadMap.get("@object-id@"));
            assertEquals("notifyMOIAttributeValueChanges", payloadMap.get("@notification-type@"));
            assertNull(payloadMap.get("@notification-id@"));
            assertEquals("UNKNOWN", payloadMap.get("@source-indicator@"));
            assertNull(payloadMap.get("@path@"));
            assertEquals("NULL", payloadMap.get("@operation@"));
            assertEquals("[{attributeNameValuePairSet:{faults-enabled:true}}]", payloadMap.get("@value@"));

        } catch (Exception e) {
            fail("Test fail with message: " + e.getMessage());
        }
    }

    private String readFileToString(File file) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        Files.lines(Paths.get(file.toURI())).forEach(fileContent::append);
        return fileContent.toString();
    }

    private JsonNode convertMessageToJsonNode(String message) throws JsonProcessingException {
        return new ObjectMapper().readTree(message);
    }

    @After
    public void after() {
        generalConfigForTest.close();
    }
}