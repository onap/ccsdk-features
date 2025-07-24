/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave.pm.PerformanceDataAirInterface170324Builder;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.util.InconsistentPMDataException;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.Lp;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.logical.termination.point.g.LpBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.air._interface.historical.performances.g.HistoricalPerformanceDataList;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.air._interface.historical.performances.g.HistoricalPerformanceDataListBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceConfiguration;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.microwave.model.rev170324.mw.air._interface.pac.AirInterfaceConfigurationBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestWrapperNoPmData {

    @Test
    public void test() {
        NodeId nodeId = new NodeId("TestNode");
        Lp lp = new LpBuilder().setUuid(new UniversalId("NodeUUID")).build();
        //Build empty, without PerformanceData
        HistoricalPerformanceDataList pmRecord =
                new HistoricalPerformanceDataListBuilder().setSuspectIntervalFlag(true).setHistoryDataId("1").build();
        AirInterfaceConfiguration airConfiguration = new AirInterfaceConfigurationBuilder().build();

        @SuppressWarnings("unused")
        Exception exception = assertThrows(InconsistentPMDataException.class, () -> {
            PerformanceDataAirInterface170324Builder pmdata =
                    new PerformanceDataAirInterface170324Builder(nodeId, lp, pmRecord, airConfiguration);
        });
        String expectedMessage = "Ignore record without PerformanceData. Node/Lp: NodeUUID/default";
        String actualMessage = exception.getMessage();
        assertEquals(actualMessage,expectedMessage);
    }
}
