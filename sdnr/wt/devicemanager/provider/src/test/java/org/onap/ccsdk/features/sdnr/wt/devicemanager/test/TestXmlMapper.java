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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.fail;
import javax.xml.bind.JAXBException;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util.InternalDateAndTime;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.AttributeValueChangedNotificationXml;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.xml.XmlMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.EventlogNotificationBuilder;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Eventlog;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

@SuppressWarnings("deprecation")
public class TestXmlMapper {

    @Test
    public void test() {
        XmlMapper xmlMapper = new XmlMapper();
        AttributeValueChangedNotificationXml event = getChangeNotification(new NodeId("NodeTest1"), 2, InternalDateAndTime.getTestpatternDateAndTime(), "ObjTest1",
                "AtrributeTest1", "NewTest1");

        try {
            String result = xmlMapper.getXmlString(event);
            System.out.println("Mappingresult = "+result);
        } catch (JAXBException e) {
            e.printStackTrace();
            fail("Problem with xml mapping.");
        }

    }

    public AttributeValueChangedNotificationXml getChangeNotification(NodeId nodeId, @Nullable Integer counter, @Nullable DateAndTime timeStamp,
            @Nullable String objectId, @Nullable String attributeName, @Nullable String newValue) {
        Eventlog eventlogEntity = new EventlogNotificationBuilder(nodeId, counter, timeStamp, objectId, attributeName, newValue).build();
        return new AttributeValueChangedNotificationXml(eventlogEntity);
    }

}
