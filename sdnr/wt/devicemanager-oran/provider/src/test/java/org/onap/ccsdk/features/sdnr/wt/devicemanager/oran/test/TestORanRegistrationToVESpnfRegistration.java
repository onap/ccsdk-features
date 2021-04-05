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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.test;

import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.ORanRegistrationToVESpnfRegistrationMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Host;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.IpAddress;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Address;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.optional.rev190614.netconf.node.augmented.optional.fields.IgnoreMissingSchemaSources;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.parameters.NonModuleCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.parameters.OdlHelloMessageCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.parameters.Protocol;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.parameters.YangModuleCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.AvailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.ClusteredConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.PassThrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.connection.status.UnavailableCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.node.credentials.Credentials;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.netconf.schema.storage.YangLibrary;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;

@RunWith(MockitoJUnitRunner.class)
public class TestORanRegistrationToVESpnfRegistration {

    @Mock
    NetconfAccessor netconfAccessor;
    @Mock
    VESCollectorService vesCollectorService;
    @Mock
    VESCollectorCfgService vesCfgService;

    private final int SEQUENCE_NO = 10;

    @Test
    public void test() {
        String dateTimeString = "2020-02-05T12:30:45.283Z";
        String name = "Slot-0";

        when(netconfAccessor.getNodeId()).thenReturn(new NodeId("nSky"));
        when(netconfAccessor.getNetconfNode()).thenReturn(new TestNetconfNode());
        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(vesCfgService.getReportingEntityName()).thenReturn("SDN-R");
        Component testComponent = ComponentHelper.get(name, dateTimeString);

        ORanRegistrationToVESpnfRegistrationMapper mapper = new ORanRegistrationToVESpnfRegistrationMapper(netconfAccessor, vesCollectorService, testComponent);
        mapper.mapCommonEventHeader(SEQUENCE_NO);
        mapper.mapPNFRegistrationFields();
    }

    public class TestNetconfNode implements NetconfNode {

        @Override
        public @Nullable Credentials getCredentials() {
            return null;
        }

        @Override
        public @Nullable Host getHost() {
            return new Host(new IpAddress(new Ipv4Address("10.10.10.10")));
        }

        @Override
        public @Nullable PortNumber getPort() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Boolean isTcpOnly() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Protocol getProtocol() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Boolean isSchemaless() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable YangModuleCapabilities getYangModuleCapabilities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable NonModuleCapabilities getNonModuleCapabilities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Boolean isReconnectOnChangedSchema() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint32 getConnectionTimeoutMillis() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint32 getDefaultRequestTimeoutMillis() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint32 getMaxConnectionAttempts() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint16 getBetweenAttemptsTimeoutMillis() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable BigDecimal getSleepFactor() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint32 getKeepaliveDelay() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint16 getConcurrentRpcLimit() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable Uint16 getActorResponseWaitTime() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable OdlHelloMessageCapabilities getOdlHelloMessageCapabilities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable ConnectionStatus getConnectionStatus() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable ClusteredConnectionStatus getClusteredConnectionStatus() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable String getConnectedMessage() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable AvailableCapabilities getAvailableCapabilities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable UnavailableCapabilities getUnavailableCapabilities() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable PassThrough getPassThrough() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable String getSchemaCacheDirectory() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable YangLibrary getYangLibrary() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public @Nullable IgnoreMissingSchemaSources getIgnoreMissingSchemaSources() {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
