/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.json.JSONObject;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.ComponentName;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.Release;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data.SearchHitConverter;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto.ElAltoReleaseInformation;

/**
 * @author Michael Dürre
 *
 */
public class TestElAltoToFrankfurtConversion {

    private static final JSONObject FAULTLOG_SEARCHHIT = new JSONObject("{\n" + "\"_index\": \"sdnevents_v1\",\n"
            + "\"_type\": \"faultlog\",\n" + "\"_id\": \"sim12600/LP-MWS-TTP-01/unknownProblem1\",\n"
            + "\"_version\": 1,\n" + "\"_score\": 1,\n" + "\"_source\": {\n" + "\"fault\": {\n"
            + "\"nodeName\": \"sim12600\",\n" + "\"counter\": \"1\",\n" + "\"timeStamp\": \"2017-01-01T00:00:00.0Z\",\n"
            + "\"objectId\": \"LP-MWS-TTP-01\",\n" + "\"problem\": \"unknownProblem1\",\n"
            + "\"severity\": \"Critical\",\n" + "\"type\": \"ProblemNotificationXml\"\n" + "}\n" + "}\n" + "}");
    private static final JSONObject FAULTCURRENT_SEARCHHIT = new JSONObject("{\n" + "\"_index\": \"sdnevents_v1\",\n"
            + "\"_type\": \"faultcurrent\",\n" + "\"_id\": \"sim12600/LP-MWS-TTP-01/unknownProblem1\",\n"
            + "\"_version\": 1,\n" + "\"_score\": 1,\n" + "\"_source\": {\n" + "\"faultCurrent\": {\n"
            + "\"nodeName\": \"sim12600\",\n" + "\"counter\": \"1\",\n" + "\"timeStamp\": \"2017-01-01T00:00:00.0Z\",\n"
            + "\"objectId\": \"LP-MWS-TTP-01\",\n" + "\"problem\": \"unknownProblem1\",\n"
            + "\"severity\": \"Critical\",\n" + "\"type\": \"ProblemNotificationXml\"\n" + "}\n" + "}\n" + "}");
    private static final JSONObject INVENTORY_SEARCHHIT =
            new JSONObject("{\n" + "\"_index\": \"sdnevents_v1\",\n" + "\"_type\": \"inventoryequipment\",\n"
                    + "\"_id\": \"sim12600/a2.module-1.1.5.5\",\n" + "\"_version\": 1,\n" + "\"_score\": 1,\n"
                    + "\"_source\": {\n" + "\"treeLevel\": 2,\n" + "\"parentUuid\": \"CARD-1.1.5.0\",\n"
                    + "\"mountpoint\": \"sim12600\",\n" + "\"uuid\": \"a2.module-1.1.5.5\",\n"
                    + "\"containedHolder\": [\n" + "\"SUBRACK-1.55.0.0\"\n" + "],\n" + "\"manufacturerName\": null,\n"
                    + "\"manufacturerIdentifier\": \"ONF-Wireless-Transport\",\n" + "\"serial\": \"310330015\",\n"
                    + "\"date\": \"2013-04-13T00:00:00.0Z\",\n" + "\"version\": \"a2.module-newest\",\n"
                    + "\"description\": \"WS/p8.module/a2.module#5\",\n" + "\"partTypeId\": \"3EM23141AD01\",\n"
                    + "\"modelIdentifier\": \"CRPQABVFAA\",\n" + "\"typeName\": \"a2.module\"\n" + "}\n" + "}");
    private static final JSONObject REQUIREDNE_SEARCHHIT = new JSONObject("{\n" + "\"_index\": \"mwtn_v1\",\n"
            + "\"_type\": \"required-networkelement\",\n" + "\"_id\": \"sim2230\",\n" + "\"_version\": 1,\n"
            + "\"_score\": 1,\n" + "\"_source\": {\n" + "\"mountId\": \"sim2230\",\n" + "\"host\": \"10.20.5.2\",\n"
            + "\"port\": 2230,\n" + "\"username\": \"adsa\",\n" + "\"password\": \"asda\"\n" + "}\n" + "}");
    private static final JSONObject EVENTLOG_SEARCHHIT = new JSONObject("{\n" + "\"_index\": \"sdnevents_v1\",\n"
            + "\"_type\": \"eventlog\",\n" + "\"_id\": \"AXB7cJHlZ_FApnwi29xq\",\n" + "\"_version\": 1,\n"
            + "\"_score\": 1,\n" + "\"_source\": {\n" + "\"event\": {\n"
            + "\"nodeName\": \"SDN-Controller-465e2ae306ca\",\n" + "\"counter\": \"1\",\n"
            + "\"timeStamp\": \"2020-02-25T08:22:19.8Z\",\n" + "\"objectId\": \"sim2230\",\n"
            + "\"attributeName\": \"ConnectionStatus\",\n" + "\"newValue\": \"connecting\",\n"
            + "\"type\": \"AttributeValueChangedNotificationXml\"\n" + "}\n" + "}\n" + "}");
    private static final JSONObject MAINTENANCE_SEARCHHIT =
            new JSONObject("{\n" + "\"_index\": \"mwtn_v1\",\n" + "\"_type\": \"maintenancemode\",\n"
                    + "\"_id\": \"sim2230\",\n" + "\"_version\": 1,\n" + "\"_score\": 1,\n" + "\"_source\": {\n"
                    + "\"node\": \"sim2230\",\n" + "\"filter\": [\n" + "{\n" + "\"definition\": {\n"
                    + "\"object-id-ref\": \"\",\n" + "\"problem\": \"\"\n" + "},\n" + "\"description\": \"\",\n"
                    + "\"start\": \"\",\n" + "\"end\": \"\"\n" + "}\n" + "],\n" + "\"active\": false\n" + "}\n" + "}");

    @Test
    public void test() {
        ElAltoReleaseInformation ri = new ElAltoReleaseInformation();
        //faultlog 
        SearchHitConverter faultlogConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.FAULTLOG);
        assertNotNull(faultlogConverter);
        SearchHit frankfurtFaultlogEntry = faultlogConverter.convert(new SearchHit(FAULTLOG_SEARCHHIT));
        assertNotNull(frankfurtFaultlogEntry);
        //faultcurrent
        SearchHitConverter faultcurrentConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.FAULTCURRENT);
        assertNotNull(faultcurrentConverter);
        SearchHit frankfurtFaultcurrentEntry = faultcurrentConverter.convert(new SearchHit(FAULTCURRENT_SEARCHHIT));
        assertNotNull(frankfurtFaultcurrentEntry);
        //inventory
        SearchHitConverter inventoryConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.INVENTORY);
        assertNotNull(inventoryConverter);
        SearchHit frankfurtInventory = inventoryConverter.convert(new SearchHit(INVENTORY_SEARCHHIT));
        assertNotNull(frankfurtInventory);
        //inventory
        SearchHitConverter neConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.REQUIRED_NETWORKELEMENT);
        assertNotNull(neConverter);
        SearchHit frankfurtNE = neConverter.convert(new SearchHit(REQUIREDNE_SEARCHHIT));
        assertNotNull(frankfurtNE);
        //eventlog
        SearchHitConverter eventlogConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.EVENTLOG);
        assertNotNull(eventlogConverter);
        SearchHit frankfurtEvent = eventlogConverter.convert(new SearchHit(EVENTLOG_SEARCHHIT));
        assertNull(frankfurtEvent);
        //eventlog->connectionlog
        SearchHitConverter conlogConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.CONNECTIONLOG);
        assertNotNull(conlogConverter);
        SearchHit frankfurtconlog = conlogConverter.convert(new SearchHit(EVENTLOG_SEARCHHIT));
        assertNotNull(frankfurtconlog);
        //maintenance
        SearchHitConverter maintenanceConverter = ri.getConverter(Release.FRANKFURT_R1, ComponentName.MAINTENANCE);
        assertNotNull(maintenanceConverter);
        SearchHit frankfurtmaint = maintenanceConverter.convert(new SearchHit(MAINTENANCE_SEARCHHIT));
        assertNotNull(frankfurtmaint);

    }

}
