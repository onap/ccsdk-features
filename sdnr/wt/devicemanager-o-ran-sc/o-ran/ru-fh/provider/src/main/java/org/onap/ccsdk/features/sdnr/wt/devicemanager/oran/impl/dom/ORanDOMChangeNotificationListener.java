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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESNotificationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.SourceType;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnkeyedListNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMChangeNotificationListener implements DOMNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ORanDOMChangeNotificationListener.class);

    private final NetconfDomAccessor netconfDomAccessor;
    private final DataProvider databaseService;
    private @NonNull VESCollectorService vesCollectorService;
    private final DOMNotificationToXPath domNotificationXPath;
    private ORanDOMNotifToVESEventAssembly mapper = null;
    private static int sequenceNo = 0;

    public ORanDOMChangeNotificationListener(NetconfDomAccessor netconfDomAccessor,
            @NonNull VESCollectorService vesCollectorService, DataProvider databaseService) {
        this.netconfDomAccessor = netconfDomAccessor;
        this.databaseService = databaseService;
        this.vesCollectorService = vesCollectorService;
        domNotificationXPath = new DOMNotificationToXPath();
    }

    @Override
    public void onNotification(@NonNull DOMNotification domNotification) {
        if (domNotification.getType()
                .equals(Absolute.of(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE))) {
            handleNetconfConfigChange(domNotification);
        }
    }

    private void handleNetconfConfigChange(@NonNull DOMNotification domNotification) {
        DateAndTime eventTime;
        Instant notificationEventTime = null;
        if (domNotification instanceof DOMEvent) {
            notificationEventTime = ((DOMEvent) domNotification).getEventInstant();
            eventTime = NetconfTimeStampImpl.getConverter().getTimeStamp(notificationEventTime.toString());
        } else {
            eventTime = NetconfTimeStampImpl.getConverter().getTimeStamp();
        }

        ContainerNode cn = domNotification.getBody();

        // Process the changed-by child
        //		ContainerNode changedByContainerNode = (ContainerNode) cn
        //				.childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_CHANGEDBY));
        //		ChoiceNode serverOrUserIDVal = (ChoiceNode) changedByContainerNode
        //				.childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_SERVERORUSER));
        //		@SuppressWarnings("unused")
        //		String userIDValue = serverOrUserIDVal
        //				.childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_USERNAME)).body()
        //				.toString();
        //		@SuppressWarnings("unused")
        //		Integer sessionIDVal = Integer.valueOf(serverOrUserIDVal
        //				.childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_SESSIONID)).body()
        //				.toString());
        //
        //		// Process the datastore child
        //		@SuppressWarnings("unused")
        //		String datastoreValue = cn
        //				.childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_DATASTORE)).body()
        //				.toString();

        // Process the edit child
        UnkeyedListNode editList = (UnkeyedListNode) cn
                .childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_EDITNODE));
        if (editList != null) {
            for (int listCnt = 0; listCnt < editList.size(); listCnt++) {
                String operationValue = editList.childAt(listCnt)
                        .childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_OPERATION))
                        .body().toString();
                String targetValue = editList.childAt(listCnt)
                        .childByArg(new NodeIdentifier(ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_TARGET))
                        .body().toString();

                EventlogEntity eventLogEntity1 = new EventlogBuilder()
                        .setNodeId(netconfDomAccessor.getNodeId().getValue()).setCounter(sequenceNo++)
                        .setTimestamp(eventTime).setObjectId(targetValue).setAttributeName("N.A")
                        .setSourceType(SourceType.Netconf).setNewValue(String.valueOf(operationValue)).build();
                databaseService.writeEventLog(eventLogEntity1);
            }
        }

        if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
            if (mapper == null) {
                this.mapper = new ORanDOMNotifToVESEventAssembly(netconfDomAccessor, vesCollectorService);
            }
            VESCommonEventHeaderPOJO header =
                    mapper.createVESCommonEventHeader(domNotificationXPath.getTime(domNotification),
                            ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE.getLocalName(),
                            sequenceNo);
            VESNotificationFieldsPOJO body =
                    mapper.createVESNotificationFields(domNotificationXPath.convertDomNotifToXPath(domNotification),
                            ORanDeviceManagerQNames.IETF_NETCONF_NOTIFICATIONS_NETCONF_CONFIG_CHANGE.getLocalName());
            log.debug("domNotification in XPath format = {}",
                    domNotificationXPath.convertDomNotifToXPath(domNotification));
            try {
                vesCollectorService.publishVESMessage(vesCollectorService.generateVESEvent(header, body));
            } catch (JsonProcessingException e) {
                log.warn("Exception while generating JSON object ", e);

            }
        }

    }
}
