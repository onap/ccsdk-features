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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;

@XmlRootElement(name = "AttributeValueChangedNotification")
public class AttributeValueChangedNotificationXml extends MwtNotificationBase implements GetEventType {

    private static String EVENTTYPE = "AttributeValueChangedNotification";

    @XmlElement(name = "attributeName")
    private String attributeName;

    @XmlElement(name = "newValue")
    private String newValue;

    public AttributeValueChangedNotificationXml() {

    }

    /**
     * Normalized notification
     * 
     * @param nodeName name of mountpoint
     * @param counter of notification
     * @param timeStamp from ne
     * @param objectIdRef from ne
     * @param attributeName from ne
     * @param newValue from ne
     */
    public AttributeValueChangedNotificationXml(String nodeName, Integer counter, InternalDateAndTime timeStamp,
            String objectIdRef, String attributeName, String newValue) {
        super(nodeName, counter, timeStamp, objectIdRef);
        this.attributeName = attributeName;
        this.newValue = newValue;
    }

    public AttributeValueChangedNotificationXml(EventlogEntity eventlogEntitiy) {
        this(eventlogEntitiy.getNodeId(), eventlogEntitiy.getCounter(),
                InternalDateAndTime.valueOf(eventlogEntitiy.getTimestamp()), eventlogEntitiy.getObjectId(),
                eventlogEntitiy.getAttributeName(), eventlogEntitiy.getNewValue());
    }


    public String getAttributeName() {
        return attributeName;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public String getEventType() {
        return EVENTTYPE;
    }

    public EventlogEntity getEventlogEntity() {
        return new EventlogBuilder().setAttributeName(attributeName).setNewValue(newValue)
                .setCounter(Integer.valueOf(this.getCounter())).setNodeId(this.getNodeName())
                .setObjectId(this.getObjectId()).setTimestamp(new DateAndTime(this.getTimeStamp()))
                .setSourceType(SourceType.Netconf).build();
    }

}
