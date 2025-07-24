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
package org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.NetconfNodeStateServiceImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.NetconfCommunicatorManager;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.binding.NetconfBindingAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.DomContextImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.impl.access.dom.NetconfDomAccessorImpl;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.test.example.TestNetconfHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.MountPoint;
import org.opendaylight.mdsal.binding.api.NotificationService;
import org.opendaylight.mdsal.binding.api.RpcService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.mdsal.dom.api.DOMMountPoint;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.mdsal.dom.api.DOMNotificationService;
import org.opendaylight.mdsal.dom.api.DOMRpcService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscription;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.CreateSubscriptionInput;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.notification._1._0.rev080714.StreamNameType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.Stream;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netmod.notification.rev080714.netconf.streams.StreamBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.NetconfNodeAugment;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.netconf.node.augment.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class TestNetconfAccessorImpl extends Mockito {

    @Test
    public void testConstruct() {
        NetconfCommunicatorManager netconfCommunicatorManager = mock(NetconfCommunicatorManager.class);
        DomContextImpl domContext = mock(DomContextImpl.class);
        String nodeIdString = "Test";
        String capabilityStringForNetworkElement = "network-element";
        NodeId nodeId = new NodeId(nodeIdString);
        NetconfNode testNode = TestNetconfHelper.getTestNode(nodeId, capabilityStringForNetworkElement)
                .augmentation(NetconfNodeAugment.class).getNetconfNode();

        NetconfNodeStateServiceImpl netconfNodeStateService = mock(NetconfNodeStateServiceImpl.class);
        NetconfAccessorImpl netconfAccessor = new NetconfAccessorImpl(nodeId, testNode, netconfCommunicatorManager,
                domContext, netconfNodeStateService);

        Assert.assertNotNull(netconfAccessor);

        NetconfAccessorImpl netconfAcessor2 = new NetconfAccessorImpl(netconfAccessor);

        Assert.assertNotNull(netconfAcessor2);
    }

    @Test
    public void testBindingNotifications() {
        NetconfAccessorImpl netconfAccessor = TestNetconfHelper.getNetconfAcessorImpl();

        DataBroker dataBroker = mock(DataBroker.class);

        NotificationService notificationService = mock(NotificationService.class);

        MountPoint mountPoint = mock(MountPoint.class);
        RpcService rpcService = mock(RpcService.class);
        when(mountPoint.getService(NotificationService.class)).thenReturn(Optional.of(notificationService));
        when(mountPoint.getService(RpcService.class)).thenReturn(Optional.of(rpcService));
        CreateSubscription createSubscriptionRpc = mock(CreateSubscription.class);
        when(rpcService.getRpc(CreateSubscription.class)).thenReturn(createSubscriptionRpc);
        //Start here
        NetconfBindingAccessorImpl test =
                new NetconfBindingAccessorImpl(netconfAccessor, dataBroker, mountPoint);

        String streamName = "NETCONF";
        test.registerNotificationsStream(streamName);

        //Capture parameters and assert them
        ArgumentCaptor<CreateSubscriptionInput> captor = ArgumentCaptor.forClass(CreateSubscriptionInput.class);
        verify(createSubscriptionRpc).invoke(captor.capture());

        assertEquals("StreamName", streamName, captor.getValue().getStream().getValue());
    }

    @Test
    public void testNotificationList() {

        NetconfAccessorImpl netconfAccessor = TestNetconfHelper.getNetconfAcessorImpl();

        DataBroker dataBroker = mock(DataBroker.class);
        NotificationService notificationService = mock(NotificationService.class);
        MountPoint mountPoint = mock(MountPoint.class);
        RpcService rpcService = mock(RpcService.class);
        when(mountPoint.getService(NotificationService.class)).thenReturn(Optional.of(notificationService));
        when(mountPoint.getService(RpcService.class)).thenReturn(Optional.of(rpcService));
        CreateSubscription createSubscriptionRpc = mock(CreateSubscription.class);
        when(rpcService.getRpc(CreateSubscription.class)).thenReturn(createSubscriptionRpc);

        //Start here
        NetconfBindingAccessorImpl test =
                new NetconfBindingAccessorImpl(netconfAccessor, dataBroker, mountPoint);

        String streamName = NetconfAccessor.DefaultNotificationsStream + "ChangeIt";
        StreamNameType streamNameType = new StreamNameType(streamName);
        Stream stream = new StreamBuilder().setName(streamNameType).build();
        test.registerNotificationsStream(Arrays.asList(stream));

        //Capture parameters and assert them
        ArgumentCaptor<CreateSubscriptionInput> captor = ArgumentCaptor.forClass(CreateSubscriptionInput.class);
        verify(createSubscriptionRpc).invoke(captor.capture());

        assertEquals("StreamName", streamName, captor.getValue().getStream().getValue());

    }

    @Test
    public void testNetconfDomNotification() {

        DOMDataBroker domDataBroker = mock(DOMDataBroker.class);
        DomContextImpl domContext = mock(DomContextImpl.class);
        DOMRpcService domRpcService = mock(DOMRpcService.class);
        NetconfAccessorImpl netconfAccessor = TestNetconfHelper.getNetconfAcessorImpl();
        DOMNotificationService domNotificationService = mock(DOMNotificationService.class);
        DOMMountPoint domMountPoint = mock(DOMMountPoint.class);

        when(domNotificationService.registerNotificationListener(any(DOMNotificationListener.class),
                ArgumentMatchers.<Collection<Absolute>>any()))
                .thenReturn(new Registration() {
                    @Override
                    public void close() {

                    }
                });

        YangInstanceIdentifier mountpointPath = YangInstanceIdentifier.builder().node(NetworkTopology.QNAME).build();
        when(domMountPoint.getIdentifier()).thenReturn(mountpointPath);
        when(domMountPoint.getService(DOMNotificationService.class)).thenReturn(Optional.of(domNotificationService));
        when(domMountPoint.getService(DOMRpcService.class)).thenReturn(Optional.of(domRpcService));

        /* Remark: Throws WARN java.lang.UnsupportedOperationException
         *  "[main] WARN org.opendaylight.netconf.util.NetconfUtil -
         *  Unable to set namespace context, falling back to setPrefix()
         *  during initialization."
         */
        NetconfDomAccessorImpl netconfDomAccessor =
                new NetconfDomAccessorImpl(netconfAccessor, domDataBroker, domMountPoint, domContext);

        Collection<Absolute> types = Arrays.asList(Absolute.of(NetworkTopology.QNAME));
        DOMNotificationListener listener = (notification) -> System.out.println("Notification: " + notification);
        Registration res = netconfDomAccessor.doRegisterNotificationListener(listener, types);

        //Capture parameters and assert them
        ArgumentCaptor<DOMNotificationListener> captor1 = ArgumentCaptor.forClass(DOMNotificationListener.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<Absolute>> captor2 = ArgumentCaptor.forClass(Collection.class);
        verify(domNotificationService).registerNotificationListener(captor1.capture(), captor2.capture());

        assertEquals("Listener", listener, captor1.getValue());
        assertEquals("SchemaPath", types, captor2.getValue());
        res.close();
    }

}
