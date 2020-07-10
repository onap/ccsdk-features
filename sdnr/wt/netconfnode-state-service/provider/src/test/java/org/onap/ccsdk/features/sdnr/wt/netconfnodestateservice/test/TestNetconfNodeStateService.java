/*
 * ============LICENSE_START======================================================= ONAP : ccsdk
 * feature sdnr wt ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * ================================================================================ Licensed under
 * the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License. ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.util.concurrent.ListenableFuture;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.VesNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.GenericTransactionUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.rpc.NetconfnodeStateServiceRpcApiImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.ClusterSingletonServiceProviderMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.DataBrokerNetconfMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.MountPointMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.MountPointServiceMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.NotificationPublishServiceMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.mock.RpcProviderRegistryMock;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.singleton.common.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.AttributeChangeNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.AttributeChangeNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.FaultNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.GetStatusOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushAttributeChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushAttributeChangeNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushFaultNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.netconfnode.state.rev191011.PushFaultNotificationOutput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeBuilder;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestNetconfNodeStateService {

    private static Path KARAF_ETC = Paths.get("etc");
    private static NetconfNodeStateServiceImpl netconfStateService;
    private static MountPointMock mountPoint;
    private static DataBrokerNetconfMock dataBrokerNetconf;

    private static final Logger LOG = LoggerFactory.getLogger(TestNetconfNodeStateService.class);

    @BeforeClass
    public static void before() throws InterruptedException, IOException {

        System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        // Call System property to get the classpath value
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);

        // Create mocks
        dataBrokerNetconf = new DataBrokerNetconfMock();
        dataBrokerNetconf.newReadWriteTransaction();
        mountPoint = new MountPointMock();
        ClusterSingletonServiceProvider clusterSingletonService = new ClusterSingletonServiceProviderMock();
        MountPointService mountPointService = new MountPointServiceMock(mountPoint);
        NotificationPublishService notificationPublishService = new NotificationPublishServiceMock();
        RpcProviderService rpcProviderRegistry = new RpcProviderRegistryMock();
        IEntityDataProvider entityProviderMock = mock(IEntityDataProvider.class);

        // start using blueprint interface
        netconfStateService = new NetconfNodeStateServiceImpl();

        netconfStateService.setDataBroker(dataBrokerNetconf);
        netconfStateService.setMountPointService(mountPointService);
        netconfStateService.setNotificationPublishService(notificationPublishService);
        netconfStateService.setRpcProviderRegistry(rpcProviderRegistry);
        netconfStateService.setClusterSingletonService(clusterSingletonService);
        netconfStateService.setEntityDataProvider(entityProviderMock);
        netconfStateService.init();
        System.out.println("Initialization done");
    }

    @AfterClass
    public static void after() throws InterruptedException, IOException {
        System.out.println("Start shutdown");
        // close using blueprint interface
        netconfStateService.close();
        delete(KARAF_ETC);

    }

    @Test
    public void test1() {

        System.out.println("Test1: Verify init state");
        assertTrue("Devicemanager not initialized", netconfStateService.isInitializationSuccessful());
    }


    @Test
    public void test2() {

        System.out.println("Test2: Register state listener");

        NetconfNodeStateListener nSL = mock(NetconfNodeStateListener.class);
        ListenerRegistration<NetconfNodeStateListener> res = netconfStateService.registerNetconfNodeStateListener(nSL);
        assertNotNull("Result should be null", res);
        res.getInstance();
        res.close();
    }

    @Test
    public void test3() {

        System.out.println("Test3: Register connect listener");

        NetconfNodeConnectListener nCL = mock(NetconfNodeConnectListener.class);
        ListenerRegistration<NetconfNodeConnectListener> res =
                netconfStateService.registerNetconfNodeConnectListener(nCL);
        assertNotNull("Result should be null", res);
        res.getInstance();
        res.close();
    }

    @Test
    public void test4() {
        System.out.println("Test4: Get status listener");
        GetStatusInputBuilder inputBuilder = new GetStatusInputBuilder();
        GetStatusOutputBuilder res = netconfStateService.getStatus(inputBuilder.build());
        assertNotNull("Result should be null", res);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test5OnConnect() throws InterruptedException {
        System.out.println("Test5: On Connect");
        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();
        netconfNodeBuilder.setConnectionStatus(ConnectionStatus.Connected);
        NetconfNode rootNodeNetconf = netconfNodeBuilder.build();

        String nodeIdString = "Test";
        NodeId nodeId = new NodeId(nodeIdString);
        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.addAugmentation(NetconfNode.class, rootNodeNetconf);
        nodeBuilder.setNodeId(nodeId);
        Node rootNode = nodeBuilder.build();
        NetconfAccessor acessor = new NetconfAccessorImpl(nodeId, rootNodeNetconf, mountPoint.getDataBroker(),
                mountPoint, new GenericTransactionUtils());

        DataObjectModification<Node> dom = mock(DataObjectModification.class);
        when(dom.getDataAfter()).thenReturn(rootNode);
        when(dom.getModificationType()).thenReturn(ModificationType.WRITE);

        DataTreeModification<Node> ntn = mock(DataTreeModification.class);
        when(ntn.getRootNode()).thenReturn(dom);

        NetconfNodeConnectListener nCL = mock(NetconfNodeConnectListener.class);
        netconfStateService.registerNetconfNodeConnectListener(nCL);
        mountPoint.setDatabrokerAbsent(false);

        Collection<DataTreeModification<Node>> changes = Arrays.asList(ntn);
        dataBrokerNetconf.sendClusteredChanges(changes);
        dataBrokerNetconf.sendChanges(changes);
        Thread.sleep(300);
        //verify that it was called one time and nodeId is the expected
        ArgumentCaptor<NetconfAccessor> varArgs = ArgumentCaptor.forClass(NetconfAccessor.class);
        verify(nCL).onEnterConnected(varArgs.capture());
        System.out.println("Accessor " + varArgs.getValue().getNodeId());
        assertEquals(nodeIdString, varArgs.getValue().getNodeId().getValue());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void test6Update() {
        System.out.println("Test6: OnChange");
        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();
        netconfNodeBuilder.setConnectionStatus(ConnectionStatus.Connected);
        NetconfNode rootNodeNetconf = netconfNodeBuilder.build();

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.addAugmentation(NetconfNode.class, rootNodeNetconf);
        nodeBuilder.setNodeId(new NodeId("Test"));
        Node rootNodeAfter = nodeBuilder.build();

        DataObjectModification<Node> dom = mock(DataObjectModification.class);
        when(dom.getDataBefore()).thenReturn(rootNodeAfter);
        when(dom.getDataAfter()).thenReturn(rootNodeAfter);
        when(dom.getModificationType()).thenReturn(ModificationType.WRITE);

        DataTreeModification<Node> ntn = mock(DataTreeModification.class);
        when(ntn.getRootNode()).thenReturn(dom);

        Collection<DataTreeModification<Node>> changes = Arrays.asList(ntn);
        dataBrokerNetconf.sendClusteredChanges(changes);
        dataBrokerNetconf.sendChanges(changes);
    }

    @Test
    public void test7ApiStatus() throws InterruptedException, ExecutionException {

        NetconfnodeStateServiceRpcApiImpl api = netconfStateService.getNetconfnodeStateServiceRpcApiImpl();

        GetStatusInputBuilder statusInput = new GetStatusInputBuilder();
        ListenableFuture<RpcResult<GetStatusOutput>> statusOutput = api.getStatus(statusInput.build());
        RpcResult<GetStatusOutput> res = statusOutput.get();
        GetStatusOutput output = res.getResult();
        System.out.println("Output " + output);
    }


    @Test
    public void test8ApiPushFault() throws InterruptedException, ExecutionException {

        NetconfnodeStateServiceRpcApiImpl api = netconfStateService.getNetconfnodeStateServiceRpcApiImpl();

        VesNotificationListener vNL = mock(VesNotificationListener.class);
        ListenerRegistration<VesNotificationListener> registration = netconfStateService.registerVesNotifications(vNL);

        FaultNotificationBuilder faultBuilder = new FaultNotificationBuilder();
        faultBuilder.setProblem("problem1");
        FaultNotification fault = faultBuilder.build();
        PushFaultNotificationInputBuilder statusInput = new PushFaultNotificationInputBuilder();
        statusInput.fieldsFrom(fault);
        ListenableFuture<RpcResult<PushFaultNotificationOutput>> rpcOutput =
                api.pushFaultNotification(statusInput.build());
        RpcResult<PushFaultNotificationOutput> res = rpcOutput.get();
        PushFaultNotificationOutput output = res.getResult();

        //verify that it was called one time
        verify(vNL, times(1)).onNotification(fault);

        registration.close();
        System.out.println("Output " + output);
    }

    @Test
    public void test9ApiPushNotifiction() throws InterruptedException, ExecutionException {

        NetconfnodeStateServiceRpcApiImpl api = netconfStateService.getNetconfnodeStateServiceRpcApiImpl();

        VesNotificationListener vNL = mock(VesNotificationListener.class);
        ListenerRegistration<VesNotificationListener> registration = netconfStateService.registerVesNotifications(vNL);

        AttributeChangeNotificationBuilder changeBuilder = new AttributeChangeNotificationBuilder();
        changeBuilder.setAttributeName("attribute1");
        AttributeChangeNotification change = changeBuilder.build();
        PushAttributeChangeNotificationInputBuilder statusInput = new PushAttributeChangeNotificationInputBuilder();
        statusInput.fieldsFrom(change);
        ListenableFuture<RpcResult<PushAttributeChangeNotificationOutput>> rpcOutput =
                api.pushAttributeChangeNotification(statusInput.build());
        RpcResult<PushAttributeChangeNotificationOutput> res = rpcOutput.get();
        PushAttributeChangeNotificationOutput output = res.getResult();

        //verify that it was called one time
        verify(vNL, times(1)).onNotification(change);

        registration.close();
        System.out.println("Output " + output);
    }


    // ------- private section

    private static void delete(Path etc) throws IOException {
        if (Files.exists(etc)) {
            System.out.println("Found and remove:" + etc.toString());
            delete(etc.toFile());
        }
    }

    private static void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (!f.delete()) {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }



}
