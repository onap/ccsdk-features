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

import java.time.Instant;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.onap.ccsdk.features.sdnr.wt.common.configuration.ConfigurationFileRepresentation;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.config.WebSocketManagerConfig;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.DOMNotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.NotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManagerProvider implements WebsocketManagerService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManagerProvider.class);
    private static final String APPLICATION_NAME = WebSocketManagerProvider.class.getName();
    private static final String CONFIGURATIONFILE = "etc/websocketmanager.properties";
    private WebSocketManagerConfig wsConfig;
    private static final String ALIAS = "/websocket";
    private static final String DEFAULT_IP_ADDR = "0.0.0.0";

    private WebSocketManager wsServlet = null;
    private Server server = null;

    public WebSocketManagerProvider() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
    }

    public void init() {
        LOG.info("Init provider for {}", APPLICATION_NAME);
        ConfigurationFileRepresentation configFileRepresentation =
                new ConfigurationFileRepresentation(CONFIGURATIONFILE);

        wsConfig = new WebSocketManagerConfig(configFileRepresentation);

        if (wsConfig.getWebsocketPort().isPresent() && !wsConfig.getWebsocketPort().isEmpty()) {
            try {
                startServer(DEFAULT_IP_ADDR, wsConfig.getWebsocketPort().get().intValue(), ALIAS);
            } catch (Exception e) {
                LOG.error("Failed in Websocker server startup {}", e);
            }
        } else {
            LOG.error("WebSocket Port not configured, hence not starting WebSocket Manager");
        }
    }

    public void startServer(String wsHost, int wsPort, String wsPath) throws Exception {
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setHost(wsHost);
        connector.setPort(wsPort);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        NativeWebSocketServletContainerInitializer.configure(context,
                (servletContext, nativeWebSocketConfiguration) -> {
                    // Configure default max size
                    nativeWebSocketConfiguration.getPolicy().setMaxTextMessageBufferSize(65535);

                    // Add websockets
                    nativeWebSocketConfiguration.addMapping(wsPath, new WebSocketManagerCreator());
                });

        // Add generic filter that will accept WebSocket upgrade.
        WebSocketUpgradeFilter.configure(context);

        server.start();
    }

    public void stopServer() throws Exception {
        if (server != null)
            server.stop();
    }

    @Override
    public void close() throws Exception {
        LOG.info("Close provider for {}", APPLICATION_NAME);
        stopServer();
    }

    public void onUnbindService(HttpService httpService) {
        httpService.unregister(ALIAS);
        wsServlet = null;
    }

    public void setAboutServlet(WebSocketManager wsServlet) {
        this.wsServlet = wsServlet;
    }

    public void onBindService(HttpService httpService) throws ServletException, NamespaceException {
        if (httpService == null) {
            LOG.warn("Unable to inject HttpService into DluxLoader. dlux modules won't work without httpService");
        } else {
            if (wsServlet == null) {
                wsServlet = new WebSocketManager();
                httpService.registerServlet(ALIAS, wsServlet, null, null);
                LOG.info("websocket servlet registered.");
            } else {
                LOG.warn("Servelt ");
            }
        }

    }

    public WebSocketManager getWsServlet() {
        return wsServlet;
    }

    public void setWsServlet(WebSocketManager wsServlet) {
        this.wsServlet = wsServlet;
    }

    public static boolean assertNotificationType(Notification notification, QName eventType) {
        final String yangTypeName = eventType.getLocalName();
        final Class<?> cls = notification.getClass();
        final String clsNameToTest = YangToolsMapperHelper.toCamelCaseClassName(yangTypeName);
        if (cls.getSimpleName().equals(clsNameToTest)) {
            return true;
        }
        Class<?>[] ifs = cls.getInterfaces();
        for (Class<?> clsif : ifs) {
            if (clsif.getSimpleName().equals(clsNameToTest)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void sendNotification(Notification notification, NodeId nodeId, QName eventType) {
        if (!assertNotificationType(notification, eventType)) {
            return;
        }
        this.sendNotification(notification, nodeId, eventType,
                YangToolsMapperHelper.getTime(notification, Instant.now()));
    }

    @Override
    public void sendNotification(Notification notification, NodeId nodeId, QName eventType, DateAndTime eventTime) {
        WebSocketManagerSocket.broadCast(new NotificationOutput(notification, nodeId.getValue(), eventType, eventTime));

    }

    @Override
    public void sendNotification(DOMNotification notification, NodeId nodeId, QName eventType) {
        WebSocketManagerSocket.broadCast(new DOMNotificationOutput(notification, nodeId.getValue(), eventType,
                YangToolsMapperHelper.getTime(notification, Instant.now())));
    }

    @Override
    public void sendNotification(DOMNotification notification, NodeId nodeId, QName eventType, DateAndTime eventTime) {
        WebSocketManagerSocket
                .broadCast(new DOMNotificationOutput(notification, nodeId.getValue(), eventType, eventTime));
    }

}
