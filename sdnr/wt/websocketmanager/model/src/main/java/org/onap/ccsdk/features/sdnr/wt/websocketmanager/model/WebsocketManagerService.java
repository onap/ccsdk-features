package org.onap.ccsdk.features.sdnr.wt.websocketmanager.model;

import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.binding.DataObject;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;

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
/**
 *
 * outgoing message will be wrapped into container:
 * <pre>
 * {@code
 * <notification>
 *   <eventTime>2017-07-12T12:00:00.0Z</eventTime>
 *   <problem-notification xmlns="urn:onf:params:xml:ns:yang:microwave-model">
 *       <problem>signalIsLostMinor</problem>
 *       <object-id-ref>LP-MWPS-RADIO</object-id-ref>
 *       <severity>non-alarmed</severity>
 *       <counter>$COUNTER</counter>
 *       <time-stamp>$TIME</time-stamp>
 *   </problem-notification>
 *   <node-id>ROADM-A</node-id>
 *   <eventType></eventType>
 * </notification>
 * }
 * </pre>
 * @author jack
 *
 */
public interface WebsocketManagerService {

    /**
     * Send notification via Websocket to the connected clients.
     * eventTime is extracted out of notification if {@link #EventInstantAware } is implemented
     * @param notification
     * @param nodeId
     * @param eventType
     */
    <N extends Notification<N> & DataObject> void sendNotification(N notification, NodeId nodeId, QName eventType);
    /**
     * Send notification via Websocket to the connected clients.
     * @param notification
     * @param nodeId
     * @param eventType
     * @param eventTime
     */
    <N extends Notification<N> & DataObject> void sendNotification(N notification, NodeId nodeId, QName eventType, DateAndTime eventTime);

    /**
     * Send notification via Websocket to the connected clients.
     * @param notification
     * @param nodeId
     * @param eventType
     */
    void sendNotification(DOMNotification notification, NodeId nodeId, QName eventType);
    /**
     * Send notification via Websocket to the connected clients.
     * @param notification
     * @param nodeId
     * @param eventType
     * @param eventTime
     */
    void sendNotification(DOMNotification notification, NodeId nodeId, QName eventType, DateAndTime eventTime);



}
