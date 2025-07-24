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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.northbound.addCMHandle.models.CpsCmHandleRequestBody;
import org.onap.ccsdk.sli.core.utils.common.EnvProperties;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataObjectModification.ModificationType;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.MountPointService;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.common.api.LogicalDatastoreType;
import org.opendaylight.mdsal.dom.api.DOMMountPointService;
import org.opendaylight.mdsal.singleton.api.ClusterSingletonServiceProvider;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandle;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleOutputBuilder;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.status.StatusBuilder;
import org.opendaylight.yang.gen.v1.org.onap.cps.ncmp.rev210520.DmiRegistryBuilder;
import org.opendaylight.yang.gen.v1.org.onap.cps.ncmp.rev210520.dmi.registry.CmHandle;
import org.opendaylight.yang.gen.v1.org.onap.cps.ncmp.rev210520.dmi.registry.CmHandleBuilder;
import org.opendaylight.yang.gen.v1.org.onap.cps.ncmp.rev210520.dmi.registry.CmHandleKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev240911.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yangtools.binding.Rpc;
import org.opendaylight.yangtools.binding.RpcInput;
import org.opendaylight.yangtools.binding.RpcOutput;
import org.opendaylight.yangtools.binding.data.codec.api.BindingNormalizedNodeSerializer;
import org.opendaylight.yangtools.concepts.Registration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"deprecation", "removal"})
public class AddCMHandleProvider implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AddCMHandleProvider.class);
    private final ObjectMapper objMapper = new ObjectMapper();
    private final String APPLICATION_NAME = "addCMHandle";
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String PROPERTIES_FILE_NAME = "cm-handle.properties";
    private static HashMap<String, String> config;
    private @NonNull Registration listener;
    private Registration rpcRegistration;
    private static final @NonNull InstanceIdentifier<Node> NETCONF_NODE_TOPO_IID =
            InstanceIdentifier.create(NetworkTopology.class)
                    .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())))
                    .child(Node.class);
    private static final @NonNull DataTreeIdentifier<Node> NETCONF_NODE_TOPO_TREE_ID =
            DataTreeIdentifier.of(LogicalDatastoreType.OPERATIONAL, NETCONF_NODE_TOPO_IID);

    private DataBroker dataBroker;
    private MountPointService mountPointService;
    private DOMMountPointService domMountPointService;
    private RpcProviderService rpcProviderService;
    @SuppressWarnings("unused")
    private NotificationPublishService notificationPublishService;
    @SuppressWarnings("unused")
    private ClusterSingletonServiceProvider clusterSingletonServiceProvider;
    private YangParserFactory yangParserFactory;
    private BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer;
    private Boolean isInitializationSuccessful = false;
    private Long lastNotificationSentOn = Long.valueOf(0);
    private List<String> nodeIdList = new ArrayList<>();

    public AddCMHandleProvider() {

        LOG.info("Creating provider for {}", APPLICATION_NAME);
        this.dataBroker = null;
        this.mountPointService = null;
        this.domMountPointService = null;
        this.rpcProviderService = null;
        this.notificationPublishService = null;
        this.clusterSingletonServiceProvider = null;
        this.yangParserFactory = null;
        this.bindingNormalizedNodeSerializer = null;

    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setRpcProviderService(RpcProviderService rpcProviderService) {
        this.rpcProviderService = rpcProviderService;
    }

    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
        this.notificationPublishService = notificationPublishService;
    }

    public void setMountPointService(MountPointService mountPointService) {
        this.mountPointService = mountPointService;
    }

    public void setDomMountPointService(DOMMountPointService domMountPointService) {
        this.domMountPointService = domMountPointService;
    }

    public void setClusterSingletonService(ClusterSingletonServiceProvider clusterSingletonService) {
        this.clusterSingletonServiceProvider = clusterSingletonService;
    }

    public void setYangParserFactory(YangParserFactory yangParserFactory) {
        this.yangParserFactory = yangParserFactory;
    }

    public void setBindingNormalizedNodeSerializer(BindingNormalizedNodeSerializer bindingNormalizedNodeSerializer) {
        this.bindingNormalizedNodeSerializer = bindingNormalizedNodeSerializer;
        LOG.info("Init bindingNormalizedNodeSerializer");
    }

    public Boolean isInitializationSuccessful() {
        return isInitializationSuccessful;
    }

    public void init() {
        LOG.info("Initializing {} for {}", this.getClass().getName(), APPLICATION_NAME);

        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {
            LOG.error("Environment variable SDNC_CONFIG_DIR is not set");
            propDir = "/opt/onap/ccsdk/data/properties/";
        } else if (!propDir.endsWith("/")) {
            propDir = propDir + "/";
        }

        config = new HashMap<String, String>();

        try (FileInputStream fileInput = new FileInputStream(propDir + PROPERTIES_FILE_NAME)) {
            EnvProperties properties = new EnvProperties();
            properties.load(fileInput);

            for (String param : new String[] {"cpsUrl", "user", "password", "dmaapUrl", "dmiServiceName", "client",
                    "timerThreshold"}) {
                config.put(param, properties.getProperty(param));
            }
        } catch (IOException e) {
            LOG.error("Error while reading properties file: ", e);
        }

        rpcRegistration = rpcProviderService.registerRpcImplementations(
                List.of(new RpcHelper<>(AddCMHandle.class, AddCMHandleProvider.this::addCMHandle)));



        listener = dataBroker.registerDataTreeChangeListener(NETCONF_NODE_TOPO_TREE_ID, new AddCmHandleListener());
        isInitializationSuccessful = true;
        LOG.info("Initialization complete for {}", APPLICATION_NAME);
        LOG.info("addCMHandle Session Initiated");
    }

    /**
     * AddCmHandleListener
     */
    private class AddCmHandleListener implements DataTreeChangeListener<Node> {

        @Override
        public void onDataTreeChanged(@NonNull List<DataTreeModification<Node>> changes) {
            LOG.info("AddCmHandleListener TreeChange enter changes: {}", changes.size());
            LOG.info("config: " + config);
            String nodeId = getNodeId(changes);
            if (Objects.nonNull(nodeId) && !(nodeIdList.contains(nodeId))) {
                nodeIdList.add(nodeId);
            }
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            Long difference = currentTime.getTime() - lastNotificationSentOn;
            if (difference > Long.valueOf(config.get("timerThreshold"))) {
                sendNotification(nodeIdList);
                nodeIdList.clear();
            }

        }
    }

    /**
     * Method to get nodeId.
     */
    private String getNodeId(@NonNull Collection<DataTreeModification<Node>> changes) {
        String nodeIdString = null;
        for (final DataTreeModification<Node> change : changes) {

            final DataObjectModification<Node> root = change.getRootNode();
            try {
                ModificationType modificationTyp = root.getModificationType();
                if ((modificationTyp != ModificationType.DELETE)) {

                    Node node = root.getDataAfter();
                    NodeId nodeId = node != null ? node.getNodeId() : null;
                    if (nodeId == null) {
                        LOG.info("without nodeid");
                    } else {
                        nodeIdString = nodeId.getValue();
                        LOG.info("AddCmHandle for nodeId: {}", nodeIdString);
                    }
                }

            } catch (NullPointerException | IllegalStateException e) {
                LOG.info("Data not available at ", e);
            }
        }

        return nodeIdString;
    }

    /**
     * Method called when cm-handle notification is to be sent.
     */
    protected void sendNotification(List<String> nodeIdList) {

        String sendNotificationTo = config.get("client");
        lastNotificationSentOn = new Timestamp(System.currentTimeMillis()).getTime();
        if (sendNotificationTo.equalsIgnoreCase("CPS")) {
            sendNotificationToCps(nodeIdList);

        }
        if (sendNotificationTo.equalsIgnoreCase("DMAAP")) {
            sendNotificationToDmaap(nodeIdList);
        }

        else {
            sendNotificationToCps(nodeIdList);
            sendNotificationToDmaap(nodeIdList);
        }
        lastNotificationSentOn = new Timestamp(System.currentTimeMillis()).getTime();

    }

    /**
     * Method called when cm-handle notification is to be sent to CPS.
     */
    protected String sendNotificationToCps(List<String> nodeIdList) {

        LOG.info("Sending Notification to CPS");
        String userCredential = config.get("user") + ":" + config.get("password");
        String url = config.get("cpsUrl");
        String requestBody = null;
        CpsCmHandleRequestBody cpsCmHandleRequestBody = new CpsCmHandleRequestBody(nodeIdList);
        LOG.info("url {}", url);
        LOG.info("userCredential: {}", userCredential);
        try {
            requestBody = objMapper.writeValueAsString(cpsCmHandleRequestBody);
            LOG.info("requestBody{} ", requestBody);
        } catch (JsonProcessingException e) {
            LOG.error("ERROR: {}", e);
        }
        String response = HttpRequester.sendPostRequest(url, userCredential, requestBody);
        LOG.info("response from CPS: {} ", response);
        return response;

    }

    /**
     * Method called when cm-handle notification is to be sent to Dmaap.
     */
    protected String sendNotificationToDmaap(List<String> nodeIdList) {

        LOG.info("Sending Notification to Dmaap");
        String url = config.get("dmaapUrl");
        Map<CmHandleKey, CmHandle> values = new HashMap<>();
        nodeIdList.forEach(nodeId -> {
            CmHandleBuilder cmHandleBuilder = new CmHandleBuilder();
            cmHandleBuilder.setDmiServiceName(config.get("dmiServiceName"));
            cmHandleBuilder.setId(nodeId);
            CmHandleKey cmHandleKey = new CmHandleKey(nodeId);
            values.put(cmHandleKey, cmHandleBuilder.build());
        });
        DmiRegistryBuilder dmiRegistryBuilder = new DmiRegistryBuilder();
        dmiRegistryBuilder.setCmHandle(values);

        String requestBody = null;
        LOG.info("url: {}", url);
        try {
            requestBody = objMapper.writeValueAsString(dmiRegistryBuilder.build());
            LOG.info("requestBody: {}", requestBody);
        } catch (JsonProcessingException e) {
            LOG.error("ERROR: {}", e);
        }
        String response = HttpRequester.sendPostRequest(url, null, requestBody);
        LOG.info("response from Dmaap: {}", response);
        return response;

    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    @Override
    public void close() {
        if (Objects.nonNull(listener)) {
            listener.close();
        }
        LOG.debug("AddCMHandleProvider Closed");
    }

    public ListenableFuture<RpcResult<AddCMHandleOutput>> addCMHandle(AddCMHandleInput input) {
        StatusBuilder statusBuilder = new StatusBuilder();
        statusBuilder.setMessage("SUCCESS");
        return RpcResultBuilder.success(new AddCMHandleOutputBuilder().setStatus(statusBuilder.build()).build())
                .buildFuture();

    }

    private interface RpcExecutionWrapper<I extends RpcInput, O extends RpcOutput> {

        ListenableFuture<@NonNull RpcResult<@NonNull O>> execute(@NonNull I input);
    }

    private static class RpcHelper<I extends RpcInput, O extends RpcOutput> implements Rpc<I, O> {

        private final RpcExecutionWrapper<I, O> executor;
        private final Class<? extends Rpc<I, O>> implementedInterface;

        RpcHelper(Class<? extends Rpc<I, O>> implementedInterface, RpcExecutionWrapper<I, O> executor) {
            this.implementedInterface = implementedInterface;
            this.executor = executor;
        }

        @Override
        public @NonNull ListenableFuture<@NonNull RpcResult<@NonNull O>> invoke(@NonNull I input) {
            return this.executor.execute(input);
        }

        @Override
        public @NonNull Class<? extends Rpc<I, O>> implementedInterface() {
            return this.implementedInterface;
        }
    }

}
