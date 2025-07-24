/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.HtDatabaseMaintenance;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.IEntityDataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerApiServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.DeviceManagerImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.FaultService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.mock.RpcProviderServiceMock;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeConnectListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateService;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.MaintenanceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ClearCurrentFaultByNodenameInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.GetRequiredNetworkElementKeysInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushAttributeChangeNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.PushFaultNotificationInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.devicemanager.rev190109.ShowRequiredNetworkElementInputBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("deprecation")
public class TestDevicemanager extends Mockito {

    private static final Logger log = LoggerFactory.getLogger(TestDevicemanager.class);

    private static DeviceManagerImpl deviceManager = new DeviceManagerImpl();
    private static DeviceManagerApiServiceImpl apiService;
    private static HtDatabaseMaintenance htDataBaseMaintenance = mock(HtDatabaseMaintenance.class);

    @BeforeClass
    public static void before() throws Exception {

        DataBroker dataBroker = mock(DataBroker.class);
        RpcProviderServiceMock rpcProviderRegistry = new RpcProviderServiceMock();
        NotificationPublishService notificationPublishService = mock(NotificationPublishService.class);
        MountPointService mountPointService = mock(MountPointService.class);
        ClusterSingletonServiceProvider clusterSingletonService = mock(ClusterSingletonServiceProvider.class);
        NetconfNodeStateService netconfNodeStateService = mock(NetconfNodeStateService.class);

        @SuppressWarnings("unchecked")
        Registration lr1 = mock(Registration.class);
        //doNothing().when(lr1).close();
        when(netconfNodeStateService.registerNetconfNodeConnectListener(mock(NetconfNodeConnectListener.class)))
                .thenReturn(lr1);

        @SuppressWarnings("unchecked")
        Registration lr2 = mock(Registration.class);
        //doNothing().when(lr2).close();
        when(netconfNodeStateService.registerNetconfNodeStateListener(mock(NetconfNodeStateListener.class)))
                .thenReturn(lr2);

        WebsocketManagerService websocketmanagerService = mock(WebsocketManagerService.class);

        IEntityDataProvider iEntityDataProvider = mock(IEntityDataProvider.class);

        DataProvider dataProvider = mock(DataProvider.class);
        when(iEntityDataProvider.getDataProvider()).thenReturn(dataProvider);

        when(iEntityDataProvider.getHtDatabaseMaintenance()).thenReturn(htDataBaseMaintenance);

        deviceManager.setDataBroker(dataBroker);
        deviceManager.setRpcProviderRegistry(rpcProviderRegistry);
        deviceManager.setNotificationPublishService(notificationPublishService);
        deviceManager.setMountPointService(mountPointService);
        deviceManager.setClusterSingletonService(clusterSingletonService);
        deviceManager.setNetconfNodeStateService(netconfNodeStateService);
        deviceManager.setWebsocketmanagerService(websocketmanagerService);
        deviceManager.setEntityDataProvider(iEntityDataProvider);

        deviceManager.init();

        apiService = rpcProviderRegistry.getDeviceManagerApiService();

    }


    @Test
    public void testInit() {
        assertTrue(deviceManager.isDevicemanagerInitializationOk());
    }

    @Test
    public void testMapping() {

    }

    @Test
    public void testChangeNotification() {
        NotificationService n = deviceManager.getNotificationService();

        n.creationNotification(new NodeId("NodeTest1"), 1, InternalDateAndTime.getTestpatternDateAndTime(), "ObjTest1");

        n.changeNotification(new NodeId("NodeTest1"), 2, InternalDateAndTime.getTestpatternDateAndTime(), "ObjTest1",
                "AtrributeTest1", "NewTest1");

        n.deletionNotification(new NodeId("NodeTest1"), 3, InternalDateAndTime.getTestpatternDateAndTime(), "ObjTest1");

    }

    @Test
    public void testFaultNotification() {
        log.info("testFaultNotification");

        MaintenanceBuilder mb = new MaintenanceBuilder();
        when(htDataBaseMaintenance.getMaintenance("")).thenReturn(mb.build());

        FaultService n = deviceManager.getFaultService();
        FaultlogBuilder faultLogEntityBuilder = new FaultlogBuilder();
        n.faultNotification(faultLogEntityBuilder.setNodeId("node1").setSeverity(SeverityType.Critical)
                .setTimestamp(NetconfTimeStampImpl.getTestpatternDateAndTime()).build());

    }

    @Test
    public void testGet() {
        log.info("testGet");
        GetRequiredNetworkElementKeysInputBuilder inputBuilder = new GetRequiredNetworkElementKeysInputBuilder();
        apiService.getRequiredNetworkElementKeys(inputBuilder.build());

    }

    @Test
    public void testShow() {
        log.info("testShow");
        ShowRequiredNetworkElementInputBuilder inputBuilder = new ShowRequiredNetworkElementInputBuilder();
        inputBuilder.setMountpointName("test");
        apiService.showRequiredNetworkElement(inputBuilder.build());

    }

    @Test
    public void testResync() {
        log.info("testResync");

        ClearCurrentFaultByNodenameInputBuilder inputBuilder = new ClearCurrentFaultByNodenameInputBuilder();
        inputBuilder.setNodenames(new HashSet<>(Arrays.asList("test1", "test2")));
        apiService.clearCurrentFaultByNodename(inputBuilder.build());

    }

    @Test
    public void testPushFault() {
        log.info("testPushFault");

        PushFaultNotificationInputBuilder inputBuilder = new PushFaultNotificationInputBuilder();
        inputBuilder.setNodeId("NodeTest23");
        inputBuilder.setTimestamp(new DateAndTime("2020-01-01T01:02:03.4Z"));
        apiService.pushFaultNotification(inputBuilder.build());

    }

    @Test
    public void testPushChange() {
        log.info("testPushChange");

        PushAttributeChangeNotificationInputBuilder inputBuilder = new PushAttributeChangeNotificationInputBuilder();
        inputBuilder.setNodeId("NodeTest24");
        apiService.pushAttributeChangeNotification(inputBuilder.build());

    }


    @AfterClass
    public static void after() {
        deviceManager.close();
    }

}
