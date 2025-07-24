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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification.ORanDOMChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestORanDOMNotification {

    private static final Logger log = LoggerFactory.getLogger(TestORanDOMNotification.class);
    static ContainerNode cn = null;
    static NodeId nodeId = new NodeId("nSky");
    private static NetconfDomAccessor domAccessor;
    private static VESCollectorService vesCollectorService;
    private static DataProvider databaseService;
    private static VESCollectorCfgService vesCfgService;
    // Use the below procedure for creating QName if binding generated classes are not available
    /*String ietf_netconf_notif_ns = "urn:ietf:params:xml:ns:yang:ietf-netconf-notifications";
    String ietf_netconf_notif_ns_date = "2012-02-06";
    QName username = QName.create(ietf_netconf_notif_ns, ietf_netconf_notif_ns_date, "username");*/


    @BeforeClass
    public static void prepare() {
        domAccessor = mock(NetconfDomAccessor.class);
        vesCollectorService = mock(VESCollectorService.class);
        databaseService = mock(DataProvider.class);
        vesCfgService = mock(VESCollectorCfgService.class);
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);
        when(domAccessor.getNodeId()).thenReturn(nodeId);
    }

    /*
    ImmutableContainerNode{
    identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)netconf-config-change,
    value=[
        ImmutableContainerNode{
            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)changed-by,
            value=[
                ImmutableChoiceNode{
                    identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)server-or-user,
                    value=[
                        ImmutableLeafNode{
                            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)username,
                            value=root
                        },
                        ImmutableLeafNode{
                            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)session-id,
                            value=2
                        }
                    ]
                }
            ]
        },
        ImmutableUnkeyedListNode{
            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)edit,
            value=[
                ImmutableUnkeyedListEntryNode{
                    identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)edit,
                    value=[
                        ImmutableLeafNode{
                            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)operation,
                            value=replace
                        },
                        ImmutableLeafNode{
                            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)target, value=/(urn:ietf:params:xml:ns:yang:ietf-hardware?revision=2018-03-13)hardware/component/component[{(urn:ietf:params:xml:ns:yang:ietf-hardware?revision=2018-03-13)name=chassis-fan3}]/alias
                        }
                    ]
                }
            ]
        },
        ImmutableLeafNode{
            identifier=(urn:ietf:params:xml:ns:yang:ietf-netconf-notifications?revision=2012-02-06)datastore,
            value=running
        }
    ]
    }
     */
    private static ContainerNode createDOMNotificationBody() {
        return Builders.containerBuilder()
                .withNodeIdentifier(
                        NodeIdentifier.create(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE))
                .withChild(Builders.containerBuilder()
                        .withNodeIdentifier(
                                NodeIdentifier.create(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_CHANGEDBY))
                        .withChild(Builders.choiceBuilder()
                                .withNodeIdentifier(NodeIdentifier
                                        .create(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_SERVERORUSER))
                                .withChild(ImmutableNodes
                                        .leafNode(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_USERNAME, "root"))
                                .withChild(ImmutableNodes.leafNode(
                                        ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_SESSIONID,
                                        Uint32.valueOf(2)))
                                .build())
                        .build())
                .withChild(Builders.unkeyedListBuilder()
                        .withNodeIdentifier(
                                NodeIdentifier.create(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_EDITNODE))
                        .withChild(Builders.unkeyedListEntryBuilder()
                                .withNodeIdentifier(NodeIdentifier
                                        .create(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_EDITNODE))
                                .withChild(ImmutableNodes.leafNode(
                                        ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_OPERATION, "replace"))
                                .withChild(ImmutableNodes.leafNode(
                                        ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_TARGET,
                                        "/(urn:ietf:params:xml:ns:yang:ietf-hardware?revision=2018-03-13)hardware/component[{(urn:ietf:params:xml:ns:yang:ietf-hardware?revision=2018-03-13)name=chassis-fan3}]/alias"))
                                .build())
                        .build())
                .withChild(ImmutableNodes.leafNode(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_DATASTORE,
                        "running"))
                .build();
    }

    @Test
    public void test() {
        ContainerNode cn = createDOMNotificationBody();
        NetconfDeviceNotification ndn = new NetconfDeviceNotification(cn, Instant.now());
        ORanDOMChangeNotificationListener changeListener =
                new ORanDOMChangeNotificationListener(domAccessor, vesCollectorService, databaseService);
        changeListener.onNotification(ndn);
        verify(databaseService).writeEventLog(any(EventlogEntity.class));
    }

    public static class NetconfDeviceNotification implements DOMNotification, DOMEvent {
        private final ContainerNode content;
        private final Absolute schemaPath;
        private final Instant eventTime;

        NetconfDeviceNotification(final ContainerNode content, final Instant eventTime) {
            this.content = content;
            this.eventTime = eventTime;
            this.schemaPath = Absolute.of(content.name().getNodeType());
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
