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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification;

import com.google.common.base.VerifyException;
import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMNotificationToXPath {
    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMNotificationToXPath.class);

    public HashMap<String, String> convertDomNotifToXPath(@NonNull DOMNotification domNotification) {
        @NonNull
        ContainerNode notifContainer = domNotification.getBody();
        HashMap<String, String> xPathData = new HashMap<String, String>();

        Collection<DataContainerChild> data = notifContainer.body();
        for (DataContainerChild data1 : data) {
            String namePath = "";
            recurseDOMData(notifContainer, data1, notifContainer, xPathData, namePath);
        }
        LOG.debug("XPath Data = {}", xPathData);
        return xPathData;

    }

    private void recurseDOMData(@NonNull ContainerNode notifContainer, DataContainerChild domData, DataContainerNode cn,
            HashMap<String, String> result, String namePath) {
        PathArgument pa1 = domData.getIdentifier();
        namePath += "/" + pa1.getNodeType().getLocalName();
        if (domData.getClass().getSimpleName().equals("ImmutableContainerNode")) {
            try {
                ContainerNode cn1 = (ContainerNode) cn.getChildByArg(pa1);
                for (DataContainerChild data1 : cn1.body()) {
                    recurseDOMData(notifContainer, data1, cn1, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (domData.getClass().getSimpleName().equals("ImmutableChoiceNode")) {
            try {
                ChoiceNode cn1 = (ChoiceNode) cn.getChildByArg(pa1);
                for (DataContainerChild data1 : cn1.body()) {
                    // recurseChoiceData(data1, cn1, namePath);
                    recurseDOMData(notifContainer, data1, cn1, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (domData.getClass().getSimpleName().equals("ImmutableUnkeyedListNode")) {
            try {
                UnkeyedListNode cn1 = (UnkeyedListNode) cn.getChildByArg(pa1);
                for (UnkeyedListEntryNode data1 : cn1.body()) {
                    recurseUnkeyedListEntryNodeData(data1, cn1, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (domData.getClass().getSimpleName().equals("ImmutableMapNode")) {
            try {
                MapNode cn1 = (MapNode) cn.getChildByArg(pa1);
                for (MapEntryNode data1 : cn1.body()) {
                    recurseMapEntryNodeData(notifContainer, data1, cn1, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (domData.getClass().getSimpleName().equals("ImmutableLeafSetNode")) {
            try {
                LeafSetNode<?> cn1 = (LeafSetNode<?>) cn.getChildByArg(pa1);
                for (LeafSetEntryNode<?> data1 : cn1.body()) {
                    recurseLeafSetEntryNodeData(data1, cn1, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (domData.getClass().getSimpleName().equals("ImmutableLeafNode")) {
            recurseLeafNode(domData, result, namePath);
        }
    }

    private void recurseLeafSetEntryNodeData(LeafSetEntryNode<?> data, LeafSetNode<?> cn1,
            HashMap<String, String> result, String namePath) {
        PathArgument pa1 = data.getIdentifier();
        namePath += "/" + pa1.getNodeType().getLocalName();

        if (data.getClass().getSimpleName().equals("ImmutableLeafSetEntryNode")) {
            LOG.debug("{}={}", namePath, data.body());
            result.put(namePath, data.body().toString());
        }
    }

    private void recurseMapEntryNodeData(@NonNull ContainerNode notifContainer, MapEntryNode data, MapNode cn1,
            HashMap<String, String> result, String namePath) {
        PathArgument pa1 = data.getIdentifier();
        NodeIdentifierWithPredicates ni = data.getIdentifier();

        for (QName qn : ni.keySet()) {
            namePath += "/" + ni.getValue(qn);
        }

        if (data.getClass().getSimpleName().equals("ImmutableMapEntryNode")) {
            for (DataContainerChild data1 : data.body()) {
                if (data1.getClass().getSimpleName().equals("ImmutableLeafSetNode")) {
                    try {
                        LeafSetNode<?> cn2 = (LeafSetNode<?>) data.getChildByArg(data1.getIdentifier());
                        for (LeafSetEntryNode<?> data2 : cn2.body()) {
                            recurseLeafSetEntryNodeData(data2, cn2, result, namePath);
                        }
                    } catch (VerifyException ve) {
                        LOG.debug("{} does not exist", data1.getIdentifier());
                    }
                } else {
                    recurseLeafNode(data1, result, namePath);
                }
            }
        }

        if (data.getClass().getSimpleName().equals("ImmutableLeafSetNode")) {
            try {
                LeafSetNode<?> cn2 = (LeafSetNode<?>) notifContainer.getChildByArg(pa1);
                for (LeafSetEntryNode<?> data1 : cn2.body()) {
                    recurseLeafSetEntryNodeData(data1, cn2, result, namePath);
                }
            } catch (VerifyException ve) {
                LOG.debug("{} does not exist", pa1);
            }
        }

        if (data.getClass().getSimpleName().equals("ImmutableLeafNode")) {
            LOG.debug("{}={}", namePath, data.body());
            result.put(namePath, data.body().toString());
        }
    }

    private void recurseUnkeyedListEntryNodeData(UnkeyedListEntryNode data, UnkeyedListNode cn1,
            HashMap<String, String> result, String namePath) {
        PathArgument pa1 = data.getIdentifier();
        namePath += "/" + pa1.getNodeType().getLocalName();

        if (data.getClass().getSimpleName().equals("ImmutableUnkeyedListEntryNode")) {
            for (DataContainerChild data1 : data.body()) {
                recurseLeafNode(data1, result, namePath);
            }
        }

        if (data.getClass().getSimpleName().equals("ImmutableLeafNode")) {
            LOG.debug("{}={}", namePath, data.body());
            result.put(namePath, data.body().toString());
        }
    }

    public void recurseLeafNode(DataContainerChild data, HashMap<String, String> result, String namePath) {
        PathArgument pa1 = data.getIdentifier();
        if (!(data.getClass().getSimpleName().equals("ImmutableAugmentationNode")))
            namePath += "/" + pa1.getNodeType().getLocalName();
        if (data.getClass().getSimpleName().equals("ImmutableLeafNode")) {
            LOG.debug("{}={}", namePath, data.body());
            result.put(namePath, data.body().toString());
        }
    }

    public void recurseChoiceData(HashMap<String, String> result, DataContainerChild data, ChoiceNode cn,
            String namePath) {
        PathArgument pa1 = data.getIdentifier();
        namePath += "/" + pa1.getNodeType().getLocalName();
        // NodeIdentifier nodeId = new NodeIdentifier(pa1.getNodeType());
        if (data.getClass().getSimpleName().equals("ImmutableLeafNode")) {
            LOG.debug("{}={}", namePath, data.body());
            result.put(namePath, data.body().toString());
        }
    }

    public Instant getTime(@NonNull DOMNotification domNotification) {
        @NonNull
        Instant eventTime;
        if (domNotification instanceof DOMEvent) {
            eventTime = ((DOMEvent) domNotification).getEventInstant();
            LOG.debug("Event time {}", eventTime);
        } else {
            eventTime = Instant.now();
            LOG.debug("Defaulting to actual time of processing the notification - {}", eventTime);
        }
        return eventTime;
    }
}
