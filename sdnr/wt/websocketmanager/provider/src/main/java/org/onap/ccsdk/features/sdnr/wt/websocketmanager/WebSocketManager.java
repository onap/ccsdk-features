/*
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
 */
package org.onap.ccsdk.features.sdnr.wt.websocketmanager;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.WebSocketManagerSocket.EventInputCallback;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.AkkaConfig;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.AkkaConfig.ClusterConfig;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.utils.AkkaConfig.ClusterNodeInfo;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.websocket.SyncWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManager extends WebSocketServlet {

    private static final long serialVersionUID = -681665669062744439L;

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManager.class.getName());
    private static final String APPLICATION_NAME = WebSocketManager.class.getName();
    private static final int PORT = 8181;
    private final EventInputCallback rpcEventInputCallback;
    private final AkkaConfig akkaConfig;
    /**
     * timeout for websocket with no messages in ms
     */
    //private static final long IDLE_TIMEOUT = 5 * 60 * 1000L;
    private static final long IDLE_TIMEOUT = 0L;

    private final ArrayList<URI> clusterNodeClients = new ArrayList<>();

    public WebSocketManager() {
        this(null, null);
    }

    public WebSocketManager(AkkaConfig akkaconfig, EventInputCallback cb) {
        super();
        this.akkaConfig = akkaconfig;
        if (cb != null) {
            this.rpcEventInputCallback = cb;
        } else {
            this.rpcEventInputCallback = message -> {
                LOG.debug("onMessagePushed: " + message);
                SyncWebSocketClient client;
                for (URI clientURI : WebSocketManager.this.clusterNodeClients) {
                    client = new SyncWebSocketClient(clientURI);
                    LOG.debug("try to push message to " + client.getURI());
                    client.openAndSendAndCloseSync(message);
                }
            };
        }
        LOG.info("Create servlet for {}", APPLICATION_NAME);
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        LOG.info("Configure provider for {}", APPLICATION_NAME);
        // set a second timeout
        factory.getPolicy().setIdleTimeout(IDLE_TIMEOUT);
        factory.getPolicy().setMaxBinaryMessageSize(1);
        factory.getPolicy().setMaxTextMessageSize(64 * 1024);

        // register Socket as the WebSocket to create on Upgrade
        factory.register(WebSocketManagerSocket.class);

        AkkaConfig cfg = this.akkaConfig;
        if (cfg == null) {
            try {
                cfg = AkkaConfig.load();
            } catch (Exception e) {
                LOG.warn("problem loading akka config: " + e.getMessage());
            }
        }
        if (cfg != null && cfg.isCluster()) {
            this.initWSClients(cfg.getClusterConfig());
        }
    }

    /**********************************************************
     * Private functions
     */

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getHeader("Upgrade") != null) {
            /* Accept upgrade request */
            resp.setStatus(101);
            resp.setHeader("Upgrade", "XYZP");
            resp.setHeader("Connection", "Upgrade");
            resp.setHeader("OtherHeaderB", "Value");
        }
    }

    private void initWSClients(ClusterConfig clusterConfig) {
        for (ClusterNodeInfo nodeConfig : clusterConfig.getSeedNodes()) {
            if (clusterConfig.isMe(nodeConfig)) {
                continue;
            }
            String url = String.format("ws://%s:%d/websocket", nodeConfig.getRemoteAddress(), PORT);
            try {
                LOG.debug("registering ws client for " + url);
                clusterNodeClients.add(new URI(url));
            } catch (URISyntaxException e) {
                LOG.warn("problem instantiating wsclient for url: " + url);
            }
        }
    }
}
