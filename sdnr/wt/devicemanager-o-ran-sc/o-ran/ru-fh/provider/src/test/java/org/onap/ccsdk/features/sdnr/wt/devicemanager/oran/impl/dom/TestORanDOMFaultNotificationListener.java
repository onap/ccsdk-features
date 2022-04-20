/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.vescollectorconnector.impl.VESCollectorServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(MockitoJUnitRunner.class)
public class TestORanDOMFaultNotificationListener {
    private static final Logger LOG = LoggerFactory.getLogger(TestORanDOMFaultNotificationListener.class);
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

    @Mock
    NetconfDomAccessor domAccessor;
    @Mock
    DataProvider dataProvider;
    @Mock
    FaultService faultService;
    @Mock
    DeviceManagerServiceProvider serviceProvider;
    @Mock
    WebsocketManagerService websocketManagerService;
    @Mock
    DataProvider databaseService;
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
        vesCollectorService = new VESCollectorServiceImpl(new ConfigurationFileRepresentation(TESTFILENAME));
        when(domAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        ORanDOMFaultNotificationListener faultListener = new ORanDOMFaultNotificationListener(domAccessor,
                vesCollectorService, faultService, websocketManagerService, databaseService);
        NetconfDeviceNotification ndn = new NetconfDeviceNotification(createORANDOMFault(), Instant.now());
        faultListener.onNotification(ndn);

        verify(faultService).faultNotification(any(FaultlogEntity.class));
    }

    public static ContainerNode createORANDOMFault() {
        final QName fault_id = QName.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF, "fault-id");
        final QName fault_source = QName.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF, "fault-source");
        final QName fault_severity = QName.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF, "fault-severity");
        final QName is_cleared = QName.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF, "is-cleared");
        final QName fault_text = QName.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF, "fault-text");
        return Builders.containerBuilder().withNodeIdentifier(NodeIdentifier.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF))
                .withChild(ImmutableNodes.leafNode(fault_id, "47"))
                .withChild(ImmutableNodes.leafNode(fault_source, "Slot-2-Port-B"))
                .withChild(ImmutableNodes.leafNode(fault_severity, "MAJOR"))
                .withChild(ImmutableNodes.leafNode(is_cleared, "true"))
                .withChild(ImmutableNodes.leafNode(fault_text, "CPRI Port Down")).build();
    }


    public static class NetconfDeviceNotification implements DOMNotification, DOMEvent {
        private final ContainerNode content;
        private final Absolute schemaPath;
        private final Instant eventTime;

        NetconfDeviceNotification(final ContainerNode content, final Instant eventTime) {
            this.content = content;
            this.eventTime = eventTime;
            this.schemaPath = Absolute.of(content.getIdentifier().getNodeType());
        }

        NetconfDeviceNotification(final ContainerNode content, final Absolute schemaPath, final Instant eventTime) {
            this.content = content;
            this.eventTime = eventTime;
            this.schemaPath = schemaPath;
        }

        @Override
        public Absolute getType() {
            return schemaPath;
        }

        @Override
        public ContainerNode getBody() {
            return content;
        }

        @Override
        public Instant getEventInstant() {
            return eventTime;
        }
    }
}
