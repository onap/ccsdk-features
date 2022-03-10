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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding.ORanFaultNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.VESCollectorServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestORanFaultNotificationListener {

    private static final Logger LOG = LoggerFactory.getLogger(TestORanFaultNotificationListener.class);
    private static final String TESTFILENAME = "configFile.txt";

    // @formatter:off
    private static final String TESTCONFIG_CONTENT = "[VESCollector]\n"
            + "VES_COLLECTOR_ENABLED=true\n"
            + "VES_COLLECTOR_TLS_ENABLED=true\n"
            + "VES_COLLECTOR_TRUST_ALL_CERTS=true\n"
            + "VES_COLLECTOR_USERNAME=sample1\n"
            + "VES_COLLECTOR_PASSWORD=sample1\n"
            + "VES_COLLECTOR_IP=[2001:db8:1:1::1]\n"
            + "VES_COLLECTOR_PORT=8443\n"
            + "VES_COLLECTOR_VERSION=v7\n"
            + "REPORTING_ENTITY_NAME=ONAP SDN-R\n"
            + "EVENTLOG_MSG_DETAIL=SHORT\n"
            + "";
    // @formatter:on

    @Mock NetconfBindingAccessor bindingAccessor;
    @Mock DataProvider dataProvider;
    @Mock FaultService faultService;
    @Mock DeviceManagerServiceProvider serviceProvider;
    @Mock WebsocketManagerService websocketManagerService;
    @Mock DataProvider databaseService;
    VESCollectorService vesCollectorService;

    @After
    @Before
    public void afterAndBefore() {
        File f = new File(TESTFILENAME);
        if (f.exists()) {
            LOG.info("Remove {}", f.getAbsolutePath());
            f.delete();
        }
    }

    @Test
    public void test() throws IOException {
        Files.asCharSink(new File(TESTFILENAME), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        vesCollectorService =
                new VESCollectorServiceImpl(new ConfigurationFileRepresentation(TESTFILENAME));
        when(bindingAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        ORanFaultNotificationListener faultListener = new ORanFaultNotificationListener(bindingAccessor,
                vesCollectorService, faultService, websocketManagerService, databaseService);
        faultListener.onAlarmNotif(new TestAlarmNotif());

        verify(faultService).faultNotification(getFaultLog());
    }

    private FaultlogEntity getFaultLog() {
        FaultlogBuilder faultAlarm = new FaultlogBuilder();
        faultAlarm.setNodeId("nSky");
        faultAlarm.setObjectId("ORAN-RU-FH");
        faultAlarm.setProblem("CPRI Port Down");
        faultAlarm.setSeverity(SeverityType.NonAlarmed);
        faultAlarm.setCounter(1);
        faultAlarm.setId("123");
        faultAlarm.setSourceType(SourceType.Netconf);
        faultAlarm.setTimestamp(new DateAndTime("2021-03-23T18:19:42.326144Z"));
        return faultAlarm.build();
    }

}
