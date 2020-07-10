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

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nonnull;
import javax.xml.bind.annotation.XmlElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.netconf.node.topology.rev150114.NetconfNodeConnectionStatus.ConnectionStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionLogStatus;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.ConnectionlogEntity;

public class MwtNotificationBase {

    private static String EMPTY = "empty";

    private String nodeName;
    private String counter;
    private String timeStamp;
    private @Nonnull String objectId;

    public MwtNotificationBase() {
        // For Jaxb
        this.objectId = EMPTY;
    }

    public MwtNotificationBase(String nodeName, Integer counter, InternalDateAndTime timeStamp, String objectId) {
        this.nodeName = nodeName;
        this.counter = String.valueOf(counter);
        this.timeStamp = timeStamp.getValue();
        this.objectId = objectId;
        if (this.objectId == null) {
            this.objectId = EMPTY;
        }
    }

    @XmlElement(name = "nodeName")
    public String getNodeName() {
        return nodeName;
    }

    @XmlElement(name = "counter")
    public String getCounter() {
        return counter;
    }

    @XmlElement(name = "timeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }

    @XmlElement(name = "objectId")
    public String getObjectId() {
        return objectId;
    }

    /**
     * Provide ConnectionlogEntity type
     * 
     * @return ConnectionlogEntity
     */
    public ConnectionlogEntity getConnectionlogEntity() {
        return new ConnectionlogBuilder().setNodeId(objectId).setStatus(getStatus())
                .setTimestamp(new DateAndTime(timeStamp)).build();
    }

    /**
     * Provide connection status for mountpoint log. TODO Add status disconnected if mountpoint is required, but does
     * not exists.
     * 
     * @return
     */
    private ConnectionLogStatus getStatus() {

        if (this instanceof ObjectCreationNotificationXml) {
            return ConnectionLogStatus.Mounted;

        } else if (this instanceof ObjectDeletionNotificationXml) {
            return ConnectionLogStatus.Unmounted;

        } else if (this instanceof AttributeValueChangedNotificationXml) {
            String pnx = ((AttributeValueChangedNotificationXml) this).getNewValue();
            if (pnx.equals(ConnectionStatus.Connected.getName())) {
                return ConnectionLogStatus.Connected;

            } else if (pnx.equals(ConnectionStatus.Connecting.getName())) {
                return ConnectionLogStatus.Connecting;

            } else if (pnx.equals(ConnectionStatus.UnableToConnect.getName())) {
                return ConnectionLogStatus.UnableToConnect;
            }
        }
        return ConnectionLogStatus.Undefined;
    }

    /**
     * Type for the Database to document the the same name that is used in the websockets.
     * 
     * @return String with type name of child class
     */
    @JsonProperty("type")
    public String getType() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return "MwtNotificationBase [getType()=" + getType() + ", nodeName=" + nodeName + ", counter=" + counter
                + ", timeStamp=" + timeStamp + ", objectId=" + objectId + "]";
    }



}
