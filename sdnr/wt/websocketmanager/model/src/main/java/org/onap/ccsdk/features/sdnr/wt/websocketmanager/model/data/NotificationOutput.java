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
package org.onap.ccsdk.features.sdnr.wt.websocketmanager.model.data;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;

public class NotificationOutput implements INotificationOutput{

    private DateAndTime eventTime;
    private Notification data;
    private String nodeId;
    private ReducedSchemaInfo type;


    public NotificationOutput() {

    }

    @Override
    public DateAndTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(DateAndTime eventTime) {
        this.eventTime = eventTime;
    }

    public Notification getData() {
        return data;
    }

    @Override
    public String getNodeId() {
        return this.nodeId;
    }

    @Override
    public ReducedSchemaInfo getType() {
        return this.type;
    }

    public void setData(Notification data) {
        this.data = data;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public void setType(ReducedSchemaInfo type) {
        this.type = type;
    }

    public NotificationOutput(Notification notification, String nodeId, QName type, DateAndTime eventTime) {
        this.data = notification;
        this.nodeId = nodeId;
        this.eventTime = eventTime;
        this.type = new ReducedSchemaInfo(type);
    }

}
