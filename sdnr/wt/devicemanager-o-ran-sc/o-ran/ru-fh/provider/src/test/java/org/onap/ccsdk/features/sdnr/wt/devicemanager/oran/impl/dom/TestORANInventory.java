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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.dom.util.TestYangParserUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.InventoryBuilder;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

public class TestORANInventory {

	private static final Logger LOG = LoggerFactory.getLogger(TestORANInventory.class);
	private static final QNameModule IETF_HARDWARE_MODULE = QNameModule
			.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-hardware"), Revision.of("2018-03-13"));
	private static final QName HW_CONTAINER = QName.create(IETF_HARDWARE_MODULE, "hardware");
	private static final QName HW_COMPONENT_LIST = QName.create(IETF_HARDWARE_MODULE, "component");
	private static final QName HW_COMPONENT_LIST_KEY = QName.create(IETF_HARDWARE_MODULE, "name");
	private static final QName HW_COMPONENT_LIST_CLASS = QName.create(IETF_HARDWARE_MODULE, "class");
	private static final QName HW_COMPONENT_LIST_DESC = QName.create(IETF_HARDWARE_MODULE, "description"); // leaf:String
	private static final QName HW_COMPONENT_LIST_PARENT = QName.create(IETF_HARDWARE_MODULE, "parent"); // leaf:leafref
	private static final QName HW_COMPONENT_LIST_CONTAINS_CHILD = QName.create(IETF_HARDWARE_MODULE, "contains-child"); // leaf-list:leafref
	private static final QName HW_COMPONENT_LIST_HW_REV = QName.create(IETF_HARDWARE_MODULE, "hardware-rev"); // leaf:String
	private static final QName HW_COMPONENT_LIST_SER_NUM = QName.create(IETF_HARDWARE_MODULE, "serial-num"); // leaf:String
	private static final QName HW_COMPONENT_LIST_MFG_NAME = QName.create(IETF_HARDWARE_MODULE, "mfg-name"); // leaf:String
	private static final QName HW_COMPONENT_LIST_MODEL_NAME = QName.create(IETF_HARDWARE_MODULE, "model-name"); // leaf:String
	private static final QName HW_COMPONENT_LIST_MFG_DATE = QName.create(IETF_HARDWARE_MODULE, "mfg-date"); // leaf:yang:date-and-time

	private static EffectiveModelContext schemaContext;
	private static @NonNull Inference hwContainerSchema;

	@BeforeClass
	public static void setup() throws IOException {
		schemaContext = TestYangParserUtil.parseYangResourceDirectory("/");
		hwContainerSchema = Inference.ofDataTreePath(schemaContext, HW_CONTAINER);
		System.out.println("URL is - " + TestORANReadHardware.class.getResource("/"));
	}

	@AfterClass
	public static void cleanup() {
		schemaContext = null;
		hwContainerSchema = null;
	}

	@Test
	public void testIetfHardwareFromXML() throws XMLStreamException, URISyntaxException, IOException, SAXException {

		final InputStream resourceAsStream = TestORANReadHardware.class.getResourceAsStream("/ietf-hardware.xml");

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
		NodeId nodeId = new NodeId("nSky");
		getInventoryList(nodeId, transformedInput);
	}

	public static List<Inventory> getInventoryList(NodeId nodeId, NormalizedNode hwData) {

		List<Inventory> inventoryResultList = new ArrayList<Inventory>();
		ContainerNode hwContainer = (ContainerNode) hwData;
		MapNode componentMap = (MapNode) hwContainer.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST));
		Collection<MapEntryNode> componentMapEntries = componentMap.body();

		for (MapEntryNode componentMapEntryNode : getRootComponents(componentMapEntries)) {
			inventoryResultList = recurseGetInventory(nodeId, componentMapEntryNode, componentMapEntries, 0,
					inventoryResultList);
		}
		// Verify if result is complete
		if (componentMapEntries.size() != inventoryResultList.size()) {
			LOG.warn(
					"Not all data were written to the Inventory. Potential entries with missing "
							+ "contained-child. Node Id = {}, Components Found = {}, Entries written to Database = {}",
					nodeId.getValue(), componentMapEntries.size(), inventoryResultList.size());
		}
		return inventoryResultList;
	}

	private static List<Inventory> recurseGetInventory(NodeId nodeId, MapEntryNode component,
			Collection<MapEntryNode> componentList, int treeLevel, List<Inventory> inventoryResultList) {
		LOG.info("Tree level = {}", treeLevel);
		// Add element to list, if conversion successfull
		Optional<Inventory> oInventory = getInternalEquipment(nodeId, component, treeLevel);
		if (oInventory.isPresent()) {
			inventoryResultList.add(oInventory.get());
		}
		// Walk through list of child keys and add to list
		for (String childUuid : CodeHelpers
				.nonnull(ORanDMDOMUtility.getLeafListValue(component, HW_COMPONENT_LIST_CONTAINS_CHILD))) {
			LOG.info("Calling recursively- component is {}", childUuid);
			for (MapEntryNode c : getComponentsByName(childUuid, componentList)) {
				inventoryResultList = recurseGetInventory(nodeId, c, componentList, treeLevel + 1, inventoryResultList);
			}
		}
		return inventoryResultList;
	}

	public static List<MapEntryNode> getRootComponents(Collection<MapEntryNode> componentMapEntries) {
		List<MapEntryNode> resultList = new ArrayList<>();
		for (MapEntryNode componentMapEntryNode : componentMapEntries) {
			if (ORanDMDOMUtility.getLeafValue(componentMapEntryNode, HW_COMPONENT_LIST_PARENT) == null) { // Root
																											// elements
																											// do not
																											// have a
																											// parent
				resultList.add(componentMapEntryNode);
			}
		}
		return resultList;
	}

	private static List<MapEntryNode> getComponentsByName(String name, Collection<MapEntryNode> componentList) {
		List<MapEntryNode> resultList = new ArrayList<>();
		for (MapEntryNode c : componentList) {
			if (name.equals(ORanDMDOMUtility.getKeyValue(c))) { // <-- Component list is flat search for child's of name
				resultList.add(c);
			}
		}
		return resultList;
	}

	public static Optional<Inventory> getInternalEquipment(NodeId nodeId, MapEntryNode component, int treeLevel) {

		// Make sure that expected data are not null
		Objects.requireNonNull(nodeId);
		Objects.requireNonNull(component);

		// Read manadatory data

		@Nullable
		String nodeIdString = nodeId.getValue();
		@Nullable
		String uuid = ORanDMDOMUtility.getKeyValue(component);
		@Nullable
		String idParent = ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_PARENT);
		@Nullable
		String uuidParent = idParent != null ? idParent : uuid; // <- Passt nicht
		LOG.info("Dump = {}, {}, {}, {}", uuidParent, uuid, treeLevel, nodeIdString);
		// do consistency check if all mandatory parameters are there
		if (treeLevel >= 0 && nodeIdString != null && uuid != null && uuidParent != null) {
			LOG.info("Creating new instance of Inventory");
			// Build output data

			InventoryBuilder inventoryBuilder = new InventoryBuilder();

			// General assumed as mandatory
			inventoryBuilder.setNodeId(nodeIdString);
			inventoryBuilder.setUuid(uuid);
			inventoryBuilder.setParentUuid(uuidParent);
			inventoryBuilder.setTreeLevel(Uint32.valueOf(treeLevel));

			// -- String list with ids of holders (optional)
			inventoryBuilder
					.setContainedHolder(ORanDMDOMUtility.getLeafListValue(component, HW_COMPONENT_LIST_CONTAINS_CHILD));

			// -- Manufacturer related things (optional)
			@Nullable
			String mfgName = ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_MFG_NAME);
			inventoryBuilder.setManufacturerName(mfgName);
			inventoryBuilder.setManufacturerIdentifier(mfgName);

			// Equipment type (optional)
			inventoryBuilder.setDescription(ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_DESC));
			inventoryBuilder.setModelIdentifier(ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_MODEL_NAME));

			inventoryBuilder.setPartTypeId(ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_CLASS));

			inventoryBuilder.setTypeName(ORanDMDOMUtility.getKeyValue(component));
			inventoryBuilder.setVersion(ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_HW_REV));

			// Equipment instance (optional)
			@Nullable
			String mfgDate = ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_MFG_DATE);
			if (mfgDate != null) {
				inventoryBuilder.setDate(mfgDate);
			}
			inventoryBuilder.setSerial(ORanDMDOMUtility.getLeafValue(component, HW_COMPONENT_LIST_SER_NUM));

			return Optional.of(inventoryBuilder.build());
		}
		return Optional.empty();
	}

}
