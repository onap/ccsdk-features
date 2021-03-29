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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.ORanFaultNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultcurrentBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

@RunWith(MockitoJUnitRunner.class)
public class TestORanFaultNotificationListener {

    @Mock
    NetconfBindingAccessor bindingAccessor;
    @Mock
    DataProvider dataProvider;
    @Mock
    VESCollectorService vesCollectorService;
    @Mock
    VESCollectorCfgService vesCfgService;

    @Test
    public void test() {
        when(bindingAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);

        ORanFaultNotificationListener faultListener = new ORanFaultNotificationListener(bindingAccessor, dataProvider, vesCollectorService);
        faultListener.onAlarmNotif(new TestAlarmNotif());
        verify(dataProvider).updateFaultCurrent(new FaultcurrentBuilder().setCounter(0)
                                                        .setNodeId("nSky")
                                                        .setId("123")
                                                        .setProblem("CPRI Port Down")
                                                        .setSeverity(SeverityType.Critical)
                                                        .setObjectId("ORAN-RU-FH")
                                                        .setTimestamp(new DateAndTime("2021-03-23T18:19:42.326144Z"))
                                                        .build());
    }
}
