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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.xml.bind.JAXBException;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.eventdatahandler.ODLEventListenerHandler;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.websocketmanager.rev150105.WebsocketEventInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.websocketmanager.rev150105.WebsocketEventOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.websocketmanager.rev150105.WebsocketmanagerService;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for forwarding web-socket notifications to the web-socket service, that is running as bundle.
 */
public class WebSocketServiceClientImpl2 implements WebSocketServiceClientInternal {

    private static final Logger LOG = LoggerFactory.getLogger(ODLEventListenerHandler.class);

    private final WebsocketmanagerService websocketmanagerService;
    private final XmlMapper xmlMapper;

    /**
     * New: Implementation of Websocket notification processor.
     *
     * @param websocketmanagerService2 to be used
     */
    public WebSocketServiceClientImpl2(WebsocketmanagerService websocketmanagerService2) {
        super();
        this.websocketmanagerService = websocketmanagerService2;
        this.xmlMapper = new XmlMapper();
    }

    @Override
    public <T extends MwtNotificationBase & GetEventType> void sendViaWebsockets(@NonNull String nodeName,
            @NonNull T notificationXml) {
        LOG.debug("Send websocket event {} for mountpoint {}", notificationXml.getClass().getSimpleName(), nodeName);

        WebsocketEventInputBuilder wsBuilder = new WebsocketEventInputBuilder();
        wsBuilder.setNodeName(nodeName);
        wsBuilder.setEventType(notificationXml.getEventType());
        try {
            wsBuilder.setXmlEvent(xmlMapper.getXmlString(notificationXml));
            Future<RpcResult<WebsocketEventOutput>> result = websocketmanagerService.websocketEvent(wsBuilder.build());
            if (result != null)
                LOG.trace("Send websocket result: {}", result == null ? "null" : result.get().getResult().getResponse());
        } catch (JAXBException | InterruptedException | ExecutionException e) {
            LOG.warn("Can not send websocket event {} for mountpoint {} {}", notificationXml, nodeName, e.toString());
        }
    }

    @Override
    public void close() throws Exception {}

}
