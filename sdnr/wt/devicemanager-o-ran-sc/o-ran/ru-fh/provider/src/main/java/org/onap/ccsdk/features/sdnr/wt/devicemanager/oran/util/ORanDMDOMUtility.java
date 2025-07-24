/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util;

import com.google.common.base.VerifyException;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDMDOMUtility {
    public static final Logger LOG = LoggerFactory.getLogger(ORanDMDOMUtility.class);
    private static final NetconfTimeStamp NETCONFTIME_CONVERTER = NetconfTimeStampImpl.getConverter();

    public static String getKeyValue(MapEntryNode componentEntry) {
        NodeIdentifierWithPredicates componentKey = componentEntry.name(); // list key
        return (String) componentKey.getValue(ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_KEY);
    }

    public static String getLeafValue(DataContainerNode componentEntry, QName leafQName) {
        NodeIdentifier leafNodeIdentifier = new NodeIdentifier(leafQName);
        try {
            LeafNode<?> optLeafNode = (LeafNode<?>) componentEntry.getChildByArg(leafNodeIdentifier);
            if (optLeafNode.body() instanceof QName) {
                LOG.debug("Leaf is of type QName");
            }
            return optLeafNode.body().toString();
        } catch (VerifyException ve) {
            LOG.debug("Leaf with QName {} not found", leafQName.toString());
            return null;
        }
    }

    public static Set<String> getLeafListValue(DataContainerNode componentEntry, QName leafListQName) {
        Set<String> containsChildList = new HashSet<String>();
        try {
            DataContainerChild childSet = componentEntry.getChildByArg(new NodeIdentifier(leafListQName));
            Collection<?> childEntry = (Collection<?>) childSet.body();
            Iterator<?> childEntryItr = childEntry.iterator();
            while (childEntryItr.hasNext()) {
                LeafSetEntryNode<?> childEntryNode = (LeafSetEntryNode<?>) childEntryItr.next();
                containsChildList.add(childEntryNode.body().toString());
            }
        } catch (VerifyException ve) {
            LOG.debug("Child for {} does not exist", leafListQName);
        }
        return containsChildList;
    }

    public static Instant getNotificationInstant(DOMNotification notification) {
        if (notification instanceof DOMEvent) {
            return ((DOMEvent) notification).getEventInstant();
        } else {
            return Instant.now();
        }
    }

    /**
     * Convert Instant to NETCONF DateAndTime
     * @param eventTimeInstant
     * @return DateAndTime
     */
    public static DateAndTime getDateAndTimeOfInstant(Instant eventTimeInstant) {
        Date eventDate = Date.from(eventTimeInstant);
        return new DateAndTime(NETCONFTIME_CONVERTER.getTimeStamp(eventDate));
    }

}
