/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2021-2022 Wipro Limited.
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

package org.onap.ccsdk.features.sdnr.northbound.addCMHandle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.opendaylight.mdsal.binding.api.ClusteredDataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"deprecation", "removal"})
public class AddCMHandleProviderTest extends Mockito {

    private static AddCMHandleProvider addCMHandleProvider;
    private static DataBroker dataBrokerNetconf;
    private @NonNull static DataTreeChangeListener<Node> listener;
    private @NonNull static ClusteredDataTreeChangeListener<Node> listenerClustered;

    private static final Logger LOG = LoggerFactory.getLogger(AddCMHandleProviderTest.class);
    private static List<String> nodeIdList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @BeforeClass
    public static <T extends DataObject, L extends DataTreeChangeListener<T>> void before()
            throws InterruptedException, IOException {

        LOG.info("Logger: " + LOG.getClass().getName() + " " + LOG.getName());
        dataBrokerNetconf = mock(DataBroker.class);
        when(dataBrokerNetconf.registerDataTreeChangeListener(Mockito.any(), Mockito.any())).thenAnswer(invocation -> {
            Object pListener = invocation.getArguments()[1];
            LOG.info("Register " + pListener.getClass().getName());
            if (pListener instanceof ClusteredDataTreeChangeListener) {
                System.out.println("Clustered listener");
                listenerClustered = (ClusteredDataTreeChangeListener<Node>) pListener;
            } else if (pListener instanceof DataTreeChangeListener) {
                System.out.println("Listener");
                listener = (DataTreeChangeListener<Node>) pListener;
            }
            return new Registration() {
                @Override
                public void close() {
                }
            };

        });

        addCMHandleProvider = new AddCMHandleProvider();
        MountPointService mountPointService = mock(MountPointService.class);
        DOMMountPointService domMountPointService = mock(DOMMountPointService.class);
        RpcProviderService rpcProviderRegistry = mock(RpcProviderService.class);
        NotificationPublishService notificationPublishService = mock(NotificationPublishService.class);
        ClusterSingletonServiceProvider clusterSingletonServiceProvider = mock(ClusterSingletonServiceProvider.class);
        YangParserFactory yangParserFactory = mock(YangParserFactory.class);
        BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer = mock(BindingNormalizedNodeSerializer.class);
        addCMHandleProvider.setDataBroker(dataBrokerNetconf);
        addCMHandleProvider.setMountPointService(mountPointService);
        addCMHandleProvider.setDomMountPointService(domMountPointService);
        addCMHandleProvider.setNotificationPublishService(notificationPublishService);
        addCMHandleProvider.setRpcProviderService(rpcProviderRegistry);
        addCMHandleProvider.setClusterSingletonService(clusterSingletonServiceProvider);
        addCMHandleProvider.setYangParserFactory(yangParserFactory);
        addCMHandleProvider.setBindingNormalizedNodeSerializer(bindingNormalizedNodeSerializer);
        addCMHandleProvider.init();
        nodeIdList.add("ncserver1");
        LOG.info("Initialization done");
    }

    @Test
    public void initializationTest() {

        LOG.info("Verify init state");
        assertTrue("Devicemanager not initialized", addCMHandleProvider.isInitializationSuccessful());
    }

    @Test
    public void sendNotificationToCpsTest() {

        LOG.info("Send notification to Cps test");
        try (MockedStatic<HttpRequester> utilities = Mockito.mockStatic(HttpRequester.class)) {
            utilities.when(() -> HttpRequester.sendPostRequest(any(), any(), any())).thenReturn("Success");

            assertEquals(addCMHandleProvider.sendNotificationToCps(nodeIdList), "Success");
        }

    }

    @Test
    public void sendNotificationToDmaapTest() {

        LOG.info("Send notification to Cps test");
        try (MockedStatic<HttpRequester> utilities = Mockito.mockStatic(HttpRequester.class)) {
            utilities.when(() -> HttpRequester.sendPostRequest(any(), any(), any())).thenReturn("Success");

            assertEquals(addCMHandleProvider.sendNotificationToCps(nodeIdList), "Success");
        }

    }

    @AfterClass
    public static void after() throws InterruptedException, IOException {
        LOG.info("Start shutdown");
        addCMHandleProvider.close();

    }

}
