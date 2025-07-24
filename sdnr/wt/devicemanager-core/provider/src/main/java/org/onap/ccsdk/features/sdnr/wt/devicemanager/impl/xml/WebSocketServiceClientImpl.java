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

import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.WebsocketManagerService;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;

/**
 * Wrapper for forwarding web-socket notifications to the web-socket service, that is running as bundle.
 */
public class WebSocketServiceClientImpl implements WebSocketServiceClientInternal {

//    private static final Logger LOG = LoggerFactory.getLogger(ODLEventListenerHandler.class);

    private final WebsocketManagerService websocketmanagerService;

    /**
     * New: Implementation of Websocket notification processor.
     *
     * @param websocketmanagerService2 to be used
     */
    public WebSocketServiceClientImpl(WebsocketManagerService websocketmanagerService2) {
        super();
        this.websocketmanagerService = websocketmanagerService2;
    }


    @Override
    public void close() throws Exception {
    }


    @Override
    public <N extends Notification<N> & DataObject> void sendViaWebsockets(@NonNull NodeId nodeId, N notification,
            QName qname, DateAndTime timestamp) {
        this.websocketmanagerService.sendNotification(notification, nodeId, qname, timestamp);

    }

}
