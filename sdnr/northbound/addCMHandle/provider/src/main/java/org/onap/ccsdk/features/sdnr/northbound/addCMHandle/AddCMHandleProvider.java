/*-
 * ============LICENSE_START=======================================================
 * ONAP : CCSDK
 * ================================================================================
 * Copyright (C) 2021 Wipro Limited.
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

import static org.opendaylight.mdsal.common.api.LogicalDatastoreType.CONFIGURATION;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONObject;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfNodeStateListener;
import org.onap.ccsdk.sli.core.sli.provider.MdsalHelper;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.mdsal.binding.api.DataObjectModification;
import org.opendaylight.mdsal.binding.api.DataTreeChangeListener;
import org.opendaylight.mdsal.binding.api.DataTreeIdentifier;
import org.opendaylight.mdsal.binding.api.DataTreeModification;
import org.opendaylight.mdsal.binding.api.NotificationPublishService;
import org.opendaylight.mdsal.binding.api.RpcProviderService;
import org.opendaylight.mdsal.dom.api.DOMDataBroker;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleInput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.AddCMHandleOutput;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev210615.CMHandleAPIService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.network.topology.topology.topology.types.TopologyNetconf;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Node;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.NodeKey;
import org.opendaylight.yangtools.concepts.ListenerRegistration;
import org.opendaylight.yangtools.concepts.ObjectRegistration;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AddCMHandleProvider implements CMHandleAPIService, NetconfNodeStateListener, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AddCMHandleProvider.class);
    private final String APPLICATION_NAME = "addCMHandle";
    private static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    private static final String PROPERTIES_FILE_NAME = "cm-handle.properties";
    private static final String PARSING_ERROR =
            "Could not create the request message to send to the server; no message will be sent";
    private final ExecutorService executor;
    protected DataBroker dataBroker;
    protected DOMDataBroker domDataBroker;
    protected NotificationPublishService notificationService;
    protected RpcProviderService rpcProviderRegistry;
    private ObjectRegistration<CMHandleAPIService> rpcRegistration;
    public static final InstanceIdentifier<Topology> NETCONF_TOPO_IID = InstanceIdentifier.create(NetworkTopology.class)
            .child(Topology.class, new TopologyKey(new TopologyId(TopologyNetconf.QNAME.getLocalName())));
    private static HashMap<String, String> config;

    public AddCMHandleProvider() {

        LOG.info("Creating provider for {}", APPLICATION_NAME);
        executor = Executors.newFixedThreadPool(1);
        this.dataBroker = null;
        this.domDataBroker = null;
        this.notificationService = null;
        this.rpcProviderRegistry = null;
        this.rpcRegistration = null;
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.dataBroker = dataBroker;
    }

    public void setDomDataBroker(DOMDataBroker domDataBroker) {
        this.domDataBroker = domDataBroker;
    }

    public void setRpcProviderRegistry(RpcProviderService rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    public void setNotificationPublishService(NotificationPublishService notificationPublishService) {
        this.notificationService = notificationPublishService;
    }

    public void init() {
        LOG.info("Initializing {} for {}", this.getClass().getName(), APPLICATION_NAME);

        if (rpcRegistration == null) {
            if (rpcProviderRegistry != null) {
                rpcRegistration = rpcProviderRegistry.registerRpcImplementation(CMHandleAPIService.class, this);
                LOG.info("Initialization complete for {}", APPLICATION_NAME);
            } else {
                LOG.warn("Error initializing {} : rpcRegistry unset", APPLICATION_NAME);
            }
        }

        String propDir = System.getenv(SDNC_CONFIG_DIR);
        if (propDir == null) {
            LOG.error("Environment variable SDNC_CONFIG_DIR is not set");
            propDir = "/opt/onap/ccsdk/data/properties/";
        } else if (!propDir.endsWith("/")) {
            propDir = propDir + "/";
        }

        // GET configuration from properties file
        config = new HashMap<String, String>();

        try (FileInputStream fileInput = new FileInputStream(propDir + PROPERTIES_FILE_NAME)) {
            Properties properties = new Properties();
            properties.load(fileInput);

            for (String param : new String[] {"url", "user", "password",
                    "authentication, dmi-service-name"}) {
                config.put(param, properties.getProperty(param));
            }
        } catch (IOException e) {
            LOG.error("Error while reading properties file: ", e);
        }

        LOG.info("addCMHandle Session Initiated");
    }

    @Override
    public void onCreated(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("NetConf device connected {}", nNodeId.getValue());
        JSONObject obj = new JSONObject();
        obj.put("cm-handle-id", nNodeId.getValue());
        obj.put("dmi-service-name", config.get("dmi-service-name"));
        ClientConfig dmaapClientConfig = new DefaultClientConfig();
        dmaapClientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 180000);
        dmaapClientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 60000);
        Client dmaapClient = Client.create(dmaapClientConfig);
        String authenticationMethod = config.get("authentication");
        ClientResponse response = null;
        try {
            if ("basic".equals(authenticationMethod)) {
                LOG.debug("Sending message to dmaap-message-router: {}", obj.toString());
                dmaapClient.addFilter(new HTTPBasicAuthFilter(config.get("user"), config.get("password")));

                response = dmaapClient.resource(config.get("url")).type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, obj);
            } else {
                response = dmaapClient.resource(config.get("url")).type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, obj);
            }
            LOG.info("Received response from dmaap-message-router: \n {}", response.toString());
        } catch (Exception e) {
            LOG.error("Error while posting message to CM_HANDLE topic: ", e);
        }

    }

    @Override
    public void onRemoved(NodeId nNodeId) {

        LOG.info("NetConf device removed - nNodeId = {}", nNodeId);
    }

    @Override
    public void onStateChange(NodeId nNodeId, NetconfNode netconfNode) {
        LOG.info("NetConf device state changed nNodeId = {}}", nNodeId);
    }

    /**
     * Method called when the blueprint container is destroyed.
     */
    public void close() {
        rpcRegistration.close();
        LOG.debug("AddCMHandleProvider Closed");
    }

    @Override
    public ListenableFuture<RpcResult<AddCMHandleOutput>> addCMHandle(AddCMHandleInput input) {

        return null;
    }
}
