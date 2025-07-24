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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.vesmapper;

import java.time.Instant;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanRegistrationToVESpnfRegistrationMapper {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ORanRegistrationToVESpnfRegistrationMapper.class);
    //CommonEventHeader fields
    private static final String VES_EVENT_DOMAIN = "pnfRegistration";
    private static final String VES_EVENTTYPE = "NetConf Callhome Registration";
    private static final String VES_EVENT_PRIORITY = "Normal";

    private final VESCollectorService vesProvider;
    private final NetconfAccessor netconfAccessor;

    private Integer sequenceNo;


    public ORanRegistrationToVESpnfRegistrationMapper(NetconfAccessor netconfAccessor,
            VESCollectorService vesCollectorService) {
        this.netconfAccessor = netconfAccessor;
        this.vesProvider = vesCollectorService;

        this.sequenceNo = 0;
    }

    public VESCommonEventHeaderPOJO mapCommonEventHeader(MapEntryNode component) {
        VESCommonEventHeaderPOJO vesCEH = new VESCommonEventHeaderPOJO();
        vesCEH.setDomain(VES_EVENT_DOMAIN);
        vesCEH.setEventId(netconfAccessor.getNodeId().getValue());
        vesCEH.setEventName(netconfAccessor.getNodeId().getValue());
        vesCEH.setEventType(VES_EVENTTYPE);
        vesCEH.setPriority(VES_EVENT_PRIORITY);

        vesCEH.setStartEpochMicrosec(Instant.now().toEpochMilli() * 1000);
        vesCEH.setLastEpochMicrosec(Instant.now().toEpochMilli() * 1000);
        vesCEH.setNfVendorName(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME));
        vesCEH.setReportingEntityId(vesProvider.getConfig().getReportingEntityId());
        vesCEH.setReportingEntityName(vesProvider.getConfig().getReportingEntityName());
        vesCEH.setSequence(sequenceNo++);
        vesCEH.setSourceId(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID) != null
                        ? ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID)
                        : netconfAccessor.getNodeId().getValue());
        vesCEH.setSourceName(netconfAccessor.getNodeId().getValue());

        return vesCEH;
    }

    public VESPNFRegistrationFieldsPOJO mapPNFRegistrationFields(MapEntryNode component) {
        VESPNFRegistrationFieldsPOJO vesPnfFields = new VESPNFRegistrationFieldsPOJO();

        vesPnfFields.setModelNumber(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME));
        vesPnfFields
                .setOamV4IpAddress(netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv4Address() != null
                        ? netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv4Address().getValue()
                        : null);
        vesPnfFields
                .setOamV6IpAddress(netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv6Address() != null
                        ? netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv6Address().getValue()
                        : null);
        vesPnfFields.setSerialNumber(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_SER_NUM));
        vesPnfFields.setVendorName(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME));
        vesPnfFields.setSoftwareVersion(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_SW_REV));
        vesPnfFields.setUnitType(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_ALIAS));
        vesPnfFields.setUnitFamily(
                ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_CLASS));
        vesPnfFields
                .setManufactureDate(
                        ORanDMDOMUtility.getLeafValue(component,
                                ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_DATE) != null
                                        ? ORanDMDOMUtility.getLeafValue(component,
                                                ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_DATE)
                                        : "Unknown");
        //vesPnfFields.setLastServiceDate(component.getLastChange());

        return vesPnfFields;
    }
}
