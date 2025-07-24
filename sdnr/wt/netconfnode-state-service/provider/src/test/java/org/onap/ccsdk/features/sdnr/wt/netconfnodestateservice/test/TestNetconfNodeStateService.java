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
 */
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.VesNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.rpc.NetconfnodeStateServiceRpcApiImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example.ExampleConfig;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example.TestNetconfHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.device.rev241009.ConnectionOper.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNodeBuilder;
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
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.binding.data.codec.impl.BindingCodecContext;
import org.opendaylight.yangtools.binding.impl.DataObjectReferenceBuilder;
import org.opendaylight.yangtools.binding.runtime.spi.BindingRuntimeHelpers;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestNetconfNodeStateService extends Mockito {

    private static Path KARAF_ETC = Paths.get("etc");
    private static NetconfNodeStateServiceImpl netconfStateService;
    //private static DataBrokerNetconfMock dataBrokerNetconf;
    private static DataBroker dataBrokerNetconf;
    private @NonNull
    static DataTreeChangeListener<Node> listener;


    private static final Logger LOG = LoggerFactory.getLogger(TestNetconfNodeStateService.class);

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static <T extends DataObject, L extends DataTreeChangeListener<T>> void before()
            throws InterruptedException, IOException {

        System.out.println("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        // Call System property to get the classpath value
        Path etc = KARAF_ETC;
        delete(etc);

        System.out.println("Create empty:" + etc.toString());
        Files.createDirectories(etc);

        // Create mocks
        //dataBrokerNetconf = new DataBrokerNetconfMock();
        //dataBrokerNetconf.newReadWriteTransaction();
        dataBrokerNetconf = mock(DataBroker.class);
        when(dataBrokerNetconf.registerTreeChangeListener(any(), any())).thenAnswer(invocation -> {
            Object pListener = invocation.getArguments()[1];
            System.out.println("Register " + pListener.getClass().getName());
            if (pListener instanceof DataTreeChangeListener) {
                System.out.println("Listener");
                listener = (DataTreeChangeListener<Node>) pListener;
            }
            return new Registration() {

                @Override
                public void close() {
                }
            };

        });
        MountPointService mountPointService = mock(MountPointService.class);
        NotificationPublishService notificationPublishService = mock(NotificationPublishService.class);
        RpcProviderService rpcProviderRegistry = mock(RpcProviderService.class);
        IEntityDataProvider entityProviderMock = mock(IEntityDataProvider.class);

        YangParserFactory yangParserFactory = new DefaultYangParserFactory();
        BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer =mock(BindingNormalizedNodeSerializer.class);
        when(bindingNormalizedNodeSerializer.fromNormalizedNode(any(),any())).thenReturn(new SimpleEntry<>(null,null));
        // start using blueprint interface
        netconfStateService = new NetconfNodeStateServiceImpl();

        netconfStateService.setDataBroker(dataBrokerNetconf);
        netconfStateService.setMountPointService(mountPointService);
        netconfStateService.setNotificationPublishService(notificationPublishService);
        netconfStateService.setRpcProviderRegistry(rpcProviderRegistry);
        netconfStateService.setEntityDataProvider(entityProviderMock);
        netconfStateService.setYangParserFactory(yangParserFactory);
        netconfStateService.setBindingNormalizedNodeSerializer(bindingNormalizedNodeSerializer);
        netconfStateService.init();
        System.out.println("Initialization done");
    }

    @AfterClass
    public static void after() throws InterruptedException, IOException {
        System.out.println("Start shutdown");
        // close using blueprint interface
        if (netconfStateService != null) {
            netconfStateService.close();
        }
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
        Registration res = netconfStateService.registerNetconfNodeStateListener(nSL);
        assertNotNull("Result should be null", res);

        res.close();
    }

    @Test
    public void test3() {

        System.out.println("Test3: Register connect listener");

        NetconfNodeConnectListener nCL = mock(NetconfNodeConnectListener.class);
        Registration res =                netconfStateService.registerNetconfNodeConnectListener(nCL);
        assertNotNull("Result should be null", res);
        res.close();
    }

    @Test
    public void test4() {
        System.out.println("Test4: Get status listener");
        GetStatusInputBuilder inputBuilder = new GetStatusInputBuilder();
        GetStatusOutputBuilder res = netconfStateService.getStatus(inputBuilder.build());
        assertNotNull("Result should be null", res);
    }

    //TODO enable again
    @SuppressWarnings("unchecked")
    @Ignore
    @Test
    public void test5OnConnect() throws InterruptedException {
        System.out.println("Test5: On Connect");
        String nodeIdString = "Test";
        String capabilityStringForNetworkElement = "network-element";
        NodeId nodeId = new NodeId(nodeIdString);
        Node rootNode = TestNetconfHelper.getTestNode(nodeId, capabilityStringForNetworkElement);

        DataObjectModification<Node> dom = mock(DataObjectModification.class);
        when(dom.dataAfter()).thenReturn(rootNode);
        when(dom.modificationType()).thenReturn(ModificationType.WRITE);

        DataTreeModification<Node> ntn = mock(DataTreeModification.class);
        when(ntn.getRootNode()).thenReturn(dom);

        NetconfNodeConnectListener nCL = mock(NetconfNodeConnectListener.class);
        netconfStateService.registerNetconfNodeConnectListener(nCL);

        List<DataTreeModification<Node>> changes = Arrays.asList(ntn);
        sendClusteredChanges(changes);
        sendChanges(changes);
        Thread.sleep(300);
        //verify that it was called one time and nodeId is the expected
        ArgumentCaptor<NetconfAccessor> varArgs = ArgumentCaptor.forClass(NetconfAccessor.class);
        verify(nCL).onEnterConnected(varArgs.capture());
        NetconfAccessor accessor = varArgs.getValue();
        System.out.println("Accessor " + accessor.getNodeId());
        assertEquals(nodeIdString, accessor.getNodeId().getValue());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test6Update() {
        System.out.println("Test6: OnChange");
        NetconfNodeBuilder netconfNodeBuilder = new NetconfNodeBuilder();
        netconfNodeBuilder.setConnectionStatus(ConnectionStatus.Connected);
        NetconfNode rootNodeNetconf = netconfNodeBuilder.build();

        NodeBuilder nodeBuilder = new NodeBuilder();
        nodeBuilder.addAugmentation((NetconfNodeAugment) () -> rootNodeNetconf);
        nodeBuilder.setNodeId(new NodeId("Test"));
        Node rootNodeAfter = nodeBuilder.build();

        DataObjectModification<Node> dom = mock(DataObjectModification.class);
        when(dom.getDataBefore()).thenReturn(rootNodeAfter);
        when(dom.getDataAfter()).thenReturn(rootNodeAfter);
        when(dom.getModificationType()).thenReturn(ModificationType.WRITE);

        DataTreeModification<Node> ntn = mock(DataTreeModification.class);
        when(ntn.getRootNode()).thenReturn(dom);

        List<DataTreeModification<Node>> changes = Arrays.asList(ntn);
        sendClusteredChanges(changes);
        sendChanges(changes);
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
        var registration = netconfStateService.registerVesNotifications(vNL);

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
        var registration = netconfStateService.registerVesNotifications(vNL);

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

    @Test
    @Ignore //TODO: fix mock for BindingNormalizedNodeSerializer (line 148)
    public void test10ApiPushNotifiction() throws YangParserException, IOException {
        ExampleConfig.exampleConfig(netconfStateService.getDomContext());
    }

    @Test
    public void test10NetconfAccessorClone() {

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

    public void sendChanges(List<DataTreeModification<Node>> changes) {
        listener.onDataTreeChanged(changes);
    }

    public void sendClusteredChanges(List<DataTreeModification<Node>> changes) {
        //listener.onDataTreeChanged(changes);
    }


}
