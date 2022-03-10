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
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces.Onf14DomInterfacePacManager;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util.Onf14DevicemanagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util.Onf14DomTestUtils;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.xml.sax.SAXException;

public class TestOnf14DomInterfacePacManager {

    @Test
    public void test() throws XMLStreamException, URISyntaxException, IOException, SAXException {
        NetconfDomAccessor netconfDomAccessor = mock(NetconfDomAccessor.class);
        Onf14DomInterfacePacManager interfacePacMgr = mock(Onf14DomInterfacePacManager.class);
        ContainerNode cn = (ContainerNode) Onf14DomTestUtils.getNormalizedNodeFromXML();
        NormalizedNode ltpData = cn.getChildByArg(new NodeIdentifier(Onf14DevicemanagerQNames.CORE_MODEL_CC_LTP));
        when(interfacePacMgr.readLtpData(netconfDomAccessor)).thenReturn(Optional.of(ltpData));
        interfacePacMgr.register();
    }

}
