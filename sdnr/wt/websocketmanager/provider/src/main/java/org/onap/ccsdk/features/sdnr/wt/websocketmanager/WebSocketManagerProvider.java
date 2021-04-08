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
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data.NotificationOutput;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketManagerProvider implements WebsocketManagerService, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketManagerProvider.class);
    private static final String APPLICATION_NAME = WebSocketManagerProvider.class.getName();
    private static final String ALIAS = "/websocket";

    private WebSocketManager wsServlet = null;

    public WebSocketManagerProvider() {
        LOG.info("Creating provider for {}", APPLICATION_NAME);
    }


    public void init() {
        LOG.info("Init provider for {}", APPLICATION_NAME);
    }

    @Override
    public void close() throws Exception {
        LOG.info("Close provider for {}", APPLICATION_NAME);
    }

    public void onUnbindService(HttpService httpService) {
        httpService.unregister(ALIAS);
        wsServlet = null;
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


    @Override
    public void sendNotification(Notification notification, String nodeId, QName eventType) {
        this.sendNotification(notification, nodeId, eventType, YangToolsMapperHelper.getTime(notification,Instant.now()));
    }

    @Override
    public void sendNotification(Notification notification, String nodeId, QName eventType, DateAndTime eventTime) {
        WebSocketManagerSocket.broadCast(new NotificationOutput(notification, nodeId, eventType, eventTime));

    }

    @Override
    public void sendNotification(DOMNotification notification, String nodeId, QName eventType) {
        LOG.warn("not yet implemented");

    }

    @Override
    public void sendNotification(DOMNotification notification, String nodeId, QName eventType, DateAndTime eventTime) {
        LOG.warn("not yet implemented");

    }

}
