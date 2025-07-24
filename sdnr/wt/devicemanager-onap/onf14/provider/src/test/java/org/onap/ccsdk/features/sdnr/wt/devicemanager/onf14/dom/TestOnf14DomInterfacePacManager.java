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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom;

import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.URISyntaxException;
import javax.xml.stream.XMLStreamException;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util.Onf14DomTestUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TestOnf14DomInterfacePacManager {

    private static final Logger log = LoggerFactory.getLogger(TestOnf14DomInterfacePacManager.class);

    @Test
    public void test() throws XMLStreamException, URISyntaxException, IOException, SAXException {
        NetconfDomAccessor netconfDomAccessor = mock(NetconfDomAccessor.class);
        Onf14DomInterfacePacManager interfacePacMgr = mock(Onf14DomInterfacePacManager.class);
        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromXML();
        //        NormalizedNode ltpData = cn.getChildByArg(new NodeIdentifier(qNames.getQName("logical-termination-point")));
        //        when(interfacePacMgr.readLtpData(netconfDomAccessor)).thenReturn(Optional.of(ltpData));
        //        interfacePacMgr.register();
    }

    @Ignore // TODO fix it

    @Test
    public void test1() throws XMLStreamException, URISyntaxException, IOException, SAXException {
        NetconfDomAccessor netconfDomAccessor = mock(NetconfDomAccessor.class);
        Onf14DomInterfacePacManager interfacePacMgr = mock(Onf14DomInterfacePacManager.class);
        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromXML("2022");

//        AugmentationNode cmn = (AugmentationNode) cn
//                .childByArg(new AugmentationIdentifier(Sets.newHashSet(Alarms10.ALARM_PAC)));
//        ContainerNode mn = (ContainerNode) cmn.childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.ALARM_PAC));
//        ContainerNode mn1 = (ContainerNode) mn.childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CURRENT_ALARMS));
//        MapNode mn2 = (MapNode) mn1.childByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CURRENT_ALARM_LIST));
//        log.info("{}", mn2);
//        Collection<MapEntryNode> mne = mn2.body();
//        for (MapEntryNode currentAlarm : mne) {
//            //			resultList.add(netconfDomAccessor.getNodeId(),
//            log.info("{} {} {} {} {}",
//                    Integer.parseInt(Onf14DMDOMUtility.getLeafValue(currentAlarm,
//                            Onf14DevicemanagerQNames.CURRENT_ALARM_IDENTIFIER)),
//                    new DateAndTime(
//                            Onf14DMDOMUtility.getLeafValue(currentAlarm, Onf14DevicemanagerQNames.ALARM_TIMESTAMP)),
//                    Onf14DMDOMUtility.getLeafValue(currentAlarm, Onf14DevicemanagerQNames.RESOURCE),
//                    Onf14DMDOMUtility.getLeafValue(currentAlarm, Onf14DevicemanagerQNames.ALARM_TYPE_QUALIFIER),
//                    InternalDataModelSeverity.mapSeverity(
//                            Onf14DMDOMUtility.getLeafValue(currentAlarm, Onf14DevicemanagerQNames.ALARM_SEVERITY)));
//        }
//        //        NormalizedNode ltpData = cn.getChildByArg(new NodeIdentifier(qNames.getQName("logical-termination-point")));
        //        when(interfacePacMgr.readLtpData(netconfDomAccessor)).thenReturn(Optional.of(ltpData));
        //        interfacePacMgr.register();
    }

}
