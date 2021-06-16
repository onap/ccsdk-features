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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl;

import java.time.Instant;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESPNFRegistrationFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.hardware.rev180313.hardware.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanRegistrationToVESpnfRegistrationMapper {

    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(ORanFaultToVESFaultMapper.class);
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

    public VESCommonEventHeaderPOJO mapCommonEventHeader(Component component) {
        VESCommonEventHeaderPOJO vesCEH = new VESCommonEventHeaderPOJO();
        vesCEH.setDomain(VES_EVENT_DOMAIN);
        vesCEH.setEventId(netconfAccessor.getNodeId().getValue());
        vesCEH.setEventName(netconfAccessor.getNodeId().getValue());
        vesCEH.setEventType(VES_EVENTTYPE);
        vesCEH.setPriority(VES_EVENT_PRIORITY);

        vesCEH.setStartEpochMicrosec(Instant.now().toEpochMilli() * 1000);
        vesCEH.setLastEpochMicrosec(Instant.now().toEpochMilli() * 1000);
        vesCEH.setNfVendorName(component.getMfgName());
        vesCEH.setReportingEntityName(vesProvider.getConfig().getReportingEntityName());
        vesCEH.setSequence(sequenceNo++);
        vesCEH.setSourceId(component.getUuid() != null ? component.getUuid().toString():netconfAccessor.getNodeId().getValue());
        vesCEH.setSourceName(netconfAccessor.getNodeId().getValue());

        return vesCEH;
    }

    public VESPNFRegistrationFieldsPOJO mapPNFRegistrationFields(Component component) {
        VESPNFRegistrationFieldsPOJO vesPnfFields = new VESPNFRegistrationFieldsPOJO();
        vesPnfFields.setModelNumber(component.getModelName());
        vesPnfFields.setOamV4IpAddress(netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv4Address()!=null?netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv4Address().getValue():null);
        vesPnfFields.setOamV6IpAddress(netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv6Address()!=null?netconfAccessor.getNetconfNode().getHost().getIpAddress().getIpv6Address().getValue():null);
        vesPnfFields.setSerialNumber(component.getSerialNum());
        vesPnfFields.setVendorName(component.getMfgName());
        vesPnfFields.setSoftwareVersion(component.getSoftwareRev());
        vesPnfFields.setUnitType(component.getAlias());
        vesPnfFields.setUnitFamily(component.getXmlClass().toString());
        vesPnfFields.setManufactureDate(component.getMfgDate()!=null?component.getMfgDate().toString():"Unknown");
        //vesPnfFields.setLastServiceDate(component.getLastChange());

        return vesPnfFields;
    }
}
