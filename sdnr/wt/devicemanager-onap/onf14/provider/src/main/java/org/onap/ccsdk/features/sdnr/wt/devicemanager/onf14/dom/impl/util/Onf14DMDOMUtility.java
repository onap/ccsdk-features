/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2022 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util;

import com.google.common.base.VerifyException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
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

public class Onf14DMDOMUtility {

    public static final Logger LOG = LoggerFactory.getLogger(Onf14DMDOMUtility.class);

    private Onf14DMDOMUtility() {}

    public static String getLeafValue(DataContainerNode componentEntry, QName leafQName) {
        NodeIdentifier leafNodeIdentifier = new NodeIdentifier(leafQName);
        try {
            LeafNode<?> optLeafNode = (LeafNode<?>) componentEntry.getChildByArg(leafNodeIdentifier);
            if (optLeafNode.body() instanceof QName) {
                LOG.debug("Leaf is of type QName"); // Ex:
                                                    // ImmutableLeafNode{identifier=(urn:onf:yang:air-interface-2-0?revision=2020-01-21)severity,
                                                    // body=(urn:onf:yang:air-interface-2-0?revision=2020-01-21)SEVERITY_TYPE_MAJOR}
                String severity_ = optLeafNode.body().toString();
                return severity_.substring(severity_.indexOf(')') + 1); // Any other solution??
            }
            return optLeafNode.body().toString();
        } catch (VerifyException ve) {
            LOG.debug("Leaf with QName {} not found", leafQName);
            return null;
        }
    }

    public static List<String> getLeafListValue(DataContainerNode componentEntry, QName leafListQName) {
        List<String> containsChildList = new ArrayList<>();
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

    public static String getUuidFromEquipment(MapEntryNode equipment) {
        LOG.debug("Equipment Identifier is {}", equipment.getIdentifier());
        NodeIdentifierWithPredicates componentKey = equipment.getIdentifier(); // list key
        LOG.debug("Key Name is - {}", componentKey.keySet());
        LOG.debug("Key Value is - {}",
                componentKey.getValue(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_GLOBAL_CLASS_UUID));

        return componentKey.getValue(Onf14DevicemanagerQNames.CORE_MODEL_CC_EQPT_GLOBAL_CLASS_UUID).toString();
    }

    public static Instant getNotificationInstant(DOMNotification notification) {
        if (notification instanceof DOMEvent) {
            return ((DOMEvent) notification).getEventInstant();
        } else {
            return Instant.now();
        }
    }

}
