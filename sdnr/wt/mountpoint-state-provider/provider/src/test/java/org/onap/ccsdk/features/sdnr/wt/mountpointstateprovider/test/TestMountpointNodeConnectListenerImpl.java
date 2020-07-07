/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
 ******************************************************************************/

package org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test;

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.GeneralConfig;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointNodeConnectListenerImpl;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.impl.MountpointStatePublisherMain;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfAccessorMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfNodeMock;
import org.onap.ccsdk.features.sdnr.wt.mountpointstateprovider.test.mock.NetconfNodeStateServiceMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

import com.google.common.io.Files;

public class TestMountpointNodeConnectListenerImpl {

    private static final String TESTCONFIG_CONTENT =
            "[general]\n" + "dmaapEnabled=false\n" + "TransportType=HTTPNOAUTH\n" + "host=onap-dmap:3904\n"
                    + "topic=unauthenticated.SDNR_MOUNTPOINT_STATE_INFO\n" + "contenttype=application/json\n"
                    + "timeout=20000\n" + "limit=10000\n" + "maxBatchSize=100\n" + "maxAgeMs=250\n"
                    + "MessageSentThreadOccurance=50\n";

    private ConfigurationFileRepresentation globalCfg;


    NetconfNodeStateServiceMock netconfNodeStateServiceMock = new NetconfNodeStateServiceMock();
    MountpointNodeConnectListenerImpl nodeConnectListener =
            new MountpointNodeConnectListenerImpl(netconfNodeStateServiceMock);
    MountpointStatePublisherMain mountpointStatePublisher;
    NetconfNodeMock netconfNodeMock = new NetconfNodeMock();
    NetconfNode netconfNode = netconfNodeMock.getNetconfNode();
    NodeId nNodeId = new NodeId("nSky");
    NetconfAccessor accessor = new NetconfAccessorMock(nNodeId, netconfNode);

    @Before
    public void initialize() throws IOException {
        Files.asCharSink(new File("test.properties"), StandardCharsets.UTF_8).write(TESTCONFIG_CONTENT);
        globalCfg = new ConfigurationFileRepresentation("test.properties");
        GeneralConfig cfg = new GeneralConfig(globalCfg);
        mountpointStatePublisher = new MountpointStatePublisherMain(cfg);
        nodeConnectListener.start(mountpointStatePublisher);
    }

    @Test
    public void testOnEnterConnected() {
        nodeConnectListener.onEnterConnected(accessor);
        assertNotEquals(mountpointStatePublisher.stateObjects.size(), 0);
    }

    @Test
    public void testOnLeaveConnected() {
        nodeConnectListener.onLeaveConnected(nNodeId, Optional.of(netconfNode));
        assertNotEquals(MountpointStatePublisherMain.stateObjects.size(), 0);
    }

    @Test
    public void testClose() throws Exception {
        //assertEquals(MountpointStatePublisher.stateObjects.size(), 0);
        nodeConnectListener.close();
    }

}
