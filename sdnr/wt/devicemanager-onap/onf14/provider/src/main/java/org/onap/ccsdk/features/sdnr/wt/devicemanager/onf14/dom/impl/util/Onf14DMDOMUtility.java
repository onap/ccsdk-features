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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jdt.annotation.Nullable;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.dataprovider.InternalDataModelSeverity;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SeverityType;
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

    /*
     * /(urn:onf:yang:core-model-1-4?revision=2019-11-27)control-construct/logical-
     * termination-point/logical-termination-point[{(urn:onf:yang:core-model-1-4?
     * revision=2019-11-27)uuid=10004041-0000-0001-0001-3c4cd0db3b20}]
     */
    private static final Pattern ALARM_RESOURCE_PATTERN =
            Pattern.compile(".*uuid=([0-9a-z]*-[0-9a-z]*-[0-9a-z]*-[0-9a-z]*-[0-9a-z]*).*", Pattern.MULTILINE);

    private Onf14DMDOMUtility() {
    }

    private static String getLeafValueX(DataContainerNode componentEntry, QName leafQName) {
        NodeIdentifier leafNodeIdentifier = new NodeIdentifier(leafQName);
        LeafNode<?> optLeafNode = (LeafNode<?>) componentEntry.getChildByArg(leafNodeIdentifier);
        if (optLeafNode.body() instanceof QName) {
            LOG.debug("Leaf is of type QName"); // Ex:
            // ImmutableLeafNode{identifier=(urn:onf:yang:air-interface-2-0?revision=2020-01-21)severity,
            // body=(urn:onf:yang:air-interface-2-0?revision=2020-01-21)SEVERITY_TYPE_MAJOR}
            String severity_ = optLeafNode.body().toString();
            return severity_.substring(severity_.indexOf(')') + 1); // Any other solution??
        }
        return optLeafNode.body().toString();
    }

    /**
     * Return value as String
     *
     * @param componentEntry Container node with data
     * @param leafQName      Leaf to be converted
     * @return String or null
     */
    public static String getLeafValue(DataContainerNode componentEntry, QName leafQName) {
        try {
            return getLeafValueX(componentEntry, leafQName);
        } catch (VerifyException ve) {
            LOG.debug("Leaf with QName {} not found", leafQName);
            return null;
        }
    }

    /**
     * Return value as Integer
     *
     * @param componentEntry Container node with data
     * @param leafQName      Leaf to be converted
     * @return Integer with value
     * @throws IllegalArgumentException, VerifyException
     */
    public static Integer getLeafValueInt(DataContainerNode componentEntry, QName leafQName) {
        String val = getLeafValueX(componentEntry, leafQName);
        if (val == null || val.isEmpty()) {
            throw new IllegalArgumentException("Value should not be null or empty");
        }
        return Integer.parseInt(val);
    }

    /**
     * Return value as DateAndTime
     *
     * @param componentEntry Container node with data
     * @param leafQName      Leaf to be converted
     * @return DateAndTime value
     * @throws IllegalArgumentException, VerifyException
     */
    public static DateAndTime getLeafValueDateAndTime(DataContainerNode componentEntry, QName leafQName) {
        return new DateAndTime(getLeafValueX(componentEntry, leafQName));
    }

    /**
     * return string with Uuid
     *
     * @param componentEntry Container node with data
     * @param resource       Leaf to be converted
     * @return Uuid
     */
    public static @Nullable String getLeafValueUuid(DataContainerNode componentEntry, QName resource) {
        return extractUuid(getLeafValue(componentEntry, resource));
    }

    /**
     * return internal severity
     *
     * @param componentEntry Container node with data
     * @param resource       Leaf to be converted
     * @return Internal SeverityType
     */
    public static @Nullable SeverityType getLeafValueInternalSeverity(DataContainerNode componentEntry,
            QName resource) {
        return InternalDataModelSeverity
                .mapSeverity(Onf14DMDOMUtility.getLeafValue(componentEntry, resource));
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

    public static String getUuidFromEquipment(MapEntryNode equipment, QName qName) {
        LOG.debug("Equipment Identifier is {}", equipment.name());
        NodeIdentifierWithPredicates componentKey = equipment.name(); // list key
        final var value = componentKey.getValue(qName);
        LOG.debug("Key Name is - {}", componentKey.keySet());
        LOG.debug("Key Value is - {}", value);
        return value != null ? value.toString() : null;
    }

    public static Instant getNotificationInstant(DOMNotification notification) {
        if (notification instanceof DOMEvent) {
            return ((DOMEvent) notification).getEventInstant();
        } else {
            return Instant.now();
        }
    }

    private static String extractUuid(String leafValue) {
        String uuid;

        Matcher matcher = ALARM_RESOURCE_PATTERN.matcher(leafValue);
        if (matcher.matches() && matcher.groupCount() == 1) {
            uuid = matcher.group(1);
        } else {
            uuid = leafValue;
        }
        return uuid;
    }

}
