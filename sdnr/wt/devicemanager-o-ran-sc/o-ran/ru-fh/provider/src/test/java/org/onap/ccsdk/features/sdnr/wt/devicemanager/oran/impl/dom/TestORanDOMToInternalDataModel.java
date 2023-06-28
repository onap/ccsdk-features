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

import static org.junit.Assert.assertEquals;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom.util.TestYangParserUtil;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.FaultlogEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Guicutthrough;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.xml.sax.SAXException;

public class TestORanDOMToInternalDataModel {

    private static final QNameModule IETF_HARDWARE_MODULE =
            QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-hardware"), Revision.of("2018-03-13"));
    private static final QName HW_CONTAINER = QName.create(IETF_HARDWARE_MODULE, "hardware");

    private static final QNameModule IETF_SYSTEM_MODULE =
            QNameModule.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-system"), Revision.of("2014-08-06"));
    private static final QName IETF_CONTAINER = QName.create(IETF_SYSTEM_MODULE, "system");

    private static EffectiveModelContext schemaContext;
    private static Inference hwContainerSchema;
    private static Inference systemSchema;

    private static final NodeId nodeId = new NodeId("nSky");

    @BeforeClass
    public static void setup() throws IOException {
        schemaContext = TestYangParserUtil.parseYangResourceDirectory("/");
        hwContainerSchema = Inference.ofDataTreePath(schemaContext, HW_CONTAINER);
        systemSchema = Inference.ofDataTreePath(schemaContext, IETF_CONTAINER);
    }

    @AfterClass
    public static void cleanup() {
        schemaContext = null;
        hwContainerSchema = null;
        systemSchema = null;
    }

    @Test
    public void testIetfHardwareFromXML() throws XMLStreamException, URISyntaxException, IOException, SAXException {

        final InputStream resourceAsStream = TestORanDOMToInternalDataModel.class.getResourceAsStream("/ietf-hardware.xml");

        /*
         * final XMLInputFactory factory = XMLInputFactory.newInstance();
         * XMLStreamReader reader = factory.createXMLStreamReader(resourceAsStream);
         */
        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, hwContainerSchema);
        xmlParser.parse(reader);

        xmlParser.flush();
        xmlParser.close();

        NormalizedNode transformedInput = result.getResult();

        List<Inventory> inventoryList = ORanDOMToInternalDataModel.getInventoryList(nodeId, transformedInput);
        assertEquals("All elements", 27, inventoryList.size());
        assertEquals("Treelevel always there", 0,
                inventoryList.stream().filter(inventory -> inventory.getTreeLevel() == null).count());
    }

    @Ignore //TODO
    @Test
    public void testIetfSystemFromXML() throws XMLStreamException, URISyntaxException, IOException, SAXException {

        final InputStream resourceAsStream = TestORanDOMToInternalDataModel.class.getResourceAsStream("/onap-system.xml");

        final XMLStreamReader reader = UntrustedXML.createXMLStreamReader(resourceAsStream);

        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final XmlParserStream xmlParser = XmlParserStream.create(streamWriter, systemSchema);
        xmlParser.parse(reader);

        xmlParser.flush();
        xmlParser.close();

        NormalizedNode transformedInput = result.getResult();
        ContainerNode cn = (ContainerNode) transformedInput;
        AugmentationIdentifier onapSystemIID = YangInstanceIdentifier.AugmentationIdentifier.create(
                Sets.newHashSet(ORanDeviceManagerQNames.ONAP_SYSTEM_NAME, ORanDeviceManagerQNames.ONAP_SYSTEM_WEB_UI));
        Optional<Guicutthrough> gc = ORanDOMToInternalDataModel.getGuicutthrough(cn.getChildByArg(onapSystemIID));
        assertEquals(gc.isPresent(), true);

    }

    @Test
    public void testORANFault() {
        ContainerNode cn = createORANDOMFault();
        NetconfDeviceNotification faultNotif = new NetconfDeviceNotification(cn, Instant.now());
        FaultlogEntity fle = ORanDOMToInternalDataModel.getFaultLog(faultNotif, nodeId, 1);
        assertEquals(fle.getId(), "47");
    }

    public static ContainerNode createORANDOMFault() {
        final QName fault_id = QName.create(ORanDeviceManagerQNames.ORAN_FM_FAULT_ID, "fault-id");
        final QName fault_source = QName.create(ORanDeviceManagerQNames.ORAN_FM_FAULT_SOURCE, "fault-source");
        final QName fault_severity = QName.create(ORanDeviceManagerQNames.ORAN_FM_FAULT_SEVERITY, "fault-severity");
        final QName is_cleared = QName.create(ORanDeviceManagerQNames.ORAN_FM_FAULT_IS_CLEARED, "is-cleared");
        final QName fault_text = QName.create(ORanDeviceManagerQNames.ORAN_FM_FAULT_TEXT, "fault-text");
        return Builders.containerBuilder()
                .withNodeIdentifier(NodeIdentifier.create(ORanDeviceManagerQNames.ORAN_FM_ALARM_NOTIF))
                .withChild(ImmutableNodes.leafNode(fault_id, "47"))
                .withChild(ImmutableNodes.leafNode(fault_source, "Slot-2-Port-B"))
                .withChild(ImmutableNodes.leafNode(fault_severity, "MAJOR"))
                .withChild(ImmutableNodes.leafNode(is_cleared, "true"))
                .withChild(ImmutableNodes.leafNode(fault_text, "CPRI Port Down")).build();
    }

    public static class NetconfDeviceNotification implements DOMNotification, DOMEvent {
        private final ContainerNode content;
        private final Absolute schemaPath;
        private final Instant eventTime;

        NetconfDeviceNotification(final ContainerNode content, final Instant eventTime) {
            this.content = content;
            this.eventTime = eventTime;
            this.schemaPath = Absolute.of(content.getIdentifier().getNodeType());
        }

        NetconfDeviceNotification(final ContainerNode content, final Absolute schemaPath, final Instant eventTime) {
            this.content = content;
            this.eventTime = eventTime;
            this.schemaPath = schemaPath;
        }

        @Override
        public Absolute getType() {
            return schemaPath;
        }

        @Override
        public ContainerNode getBody() {
            return content;
        }

        @Override
        public Instant getEventInstant() {
            return eventTime;
        }
    }
}
