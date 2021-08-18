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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.dblib.test;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.entity.DatabaseIdGenerator;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.GranularityPeriodType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.NetworkElementConnectionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata15mEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Pmdata24hEntity;

public class TestObjectIds {

    private static final String NODEID1 = "nodeid1";
    private static final String OBJECTID1 = "objectid1";
    private static final String PROBLEMNAME1 = "problem1";
    private static final String FAULTCURRENTID1 = String.format("%s/%s/%s", NODEID1, OBJECTID1, PROBLEMNAME1);
    private static final String EQUIPMENT_UUID1 = "equipment1";
    private static final String INVENTORY_ID1 = String.format("%s/%s", NODEID1, EQUIPMENT_UUID1);
    private static final String PM15MUUID1 = "pm15muuid1";
    private static final String PM24HUUID1 = "pm24huuid1";
    private static final String TIMESTAMP1 = "2020-05-01T12:55:12.34Z";
    private static final String PMDATA15MID1 = String.format("%s/%s/%s", NODEID1, PM15MUUID1, TIMESTAMP1);
    private static final String PMDATA24HID1 = String.format("%s/%s/%s", NODEID1, PM24HUUID1, TIMESTAMP1);


    @Test
    public void testGenerator() {
        assertEquals(FAULTCURRENTID1, DatabaseIdGenerator.getFaultcurrentId(NODEID1, OBJECTID1, PROBLEMNAME1));
        assertEquals(INVENTORY_ID1, DatabaseIdGenerator
                .getInventoryId(new InventoryBuilder().setNodeId(NODEID1).setUuid(EQUIPMENT_UUID1).build()));
        assertEquals(NODEID1,
                DatabaseIdGenerator.getMaintenanceId(new MaintenanceBuilder().setNodeId(NODEID1).build()));
        assertEquals(NODEID1, DatabaseIdGenerator.getMaintenanceId(NODEID1));
        assertEquals(NODEID1, DatabaseIdGenerator.getNetworkelementConnectionId(NODEID1));
        assertEquals(NODEID1, DatabaseIdGenerator
                .getNetworkelementConnectionId(new NetworkElementConnectionBuilder().setNodeId(NODEID1).build()));
        Pmdata15mEntity e =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._15m.list.output.DataBuilder()
                        .setNodeName(NODEID1).setGranularityPeriod(GranularityPeriodType.Period15Min)
                        .setUuidInterface(PM15MUUID1)
                        .setTimeStamp(NetconfTimeStampImpl.getConverter().getTimeStamp(TIMESTAMP1)).build();
        assertEquals(PMDATA15MID1, DatabaseIdGenerator.getPmData15mId(e));
        assertEquals(PMDATA15MID1, DatabaseIdGenerator.getPmData15mId(NODEID1, PM15MUUID1, TIMESTAMP1));
        Pmdata24hEntity e2 =
                new org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.read.pmdata._24h.list.output.DataBuilder()
                        .setNodeName(NODEID1).setGranularityPeriod(GranularityPeriodType.Period24Hours)
                        .setUuidInterface(PM24HUUID1)
                        .setTimeStamp(NetconfTimeStampImpl.getConverter().getTimeStamp(TIMESTAMP1)).build();
        assertEquals(PMDATA24HID1, DatabaseIdGenerator.getPmData24hId(e2));
        assertEquals(PMDATA24HID1, DatabaseIdGenerator.getPmData24hId(NODEID1, PM24HUUID1, TIMESTAMP1));
    }
}
