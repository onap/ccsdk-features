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

import javax.xml.bind.annotation.XmlRootElement;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.SourceType;

@XmlRootElement(name = "ObjectDeletionNotification")
public class ObjectDeletionNotificationXml extends MwtNotificationBase implements GetEventType {

    private static String EVENTTYPE = "ObjectDeletionNotification";
    private static String ACTION = "deletion";

    public ObjectDeletionNotificationXml() {

    }

    /**
     * Normalized notification
     * 
     * @param nodeName name of mountpoint or instance that owns the problem
     * @param counter of notification
     * @param timeStamp from ne
     * @param objectIdRef from ne
     */
    public ObjectDeletionNotificationXml(String nodeName, Integer counter, InternalDateAndTime timeStamp,
            String objectIdRef) {
        super(nodeName, counter, timeStamp, objectIdRef);
    }

    public ObjectDeletionNotificationXml(EventlogEntity el) {
        this(el.getNodeId(), el.getCounter(), InternalDateAndTime.valueOf(el.getTimestamp()), el.getObjectId());
    }

    @Override
    public String getEventType() {
        return EVENTTYPE;
    }

    public EventlogEntity getEventlogEntity() {
        return new EventlogBuilder().setAttributeName(ACTION).setNewValue(ACTION)
                .setCounter(Integer.valueOf(this.getCounter())).setNodeId(this.getNodeName())
                .setObjectId(this.getObjectId()).setTimestamp(new DateAndTime(this.getTimeStamp()))
                .setSourceType(SourceType.Netconf).build();
    }

}
