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

import com.google.common.base.VerifyException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.binding.CodeHelpers;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.codec.xml.XmlParserStream;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.util.SchemaContextUtil;
import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference;
//import org.opendaylight.yangtools.yang.model.util.SchemaInferenceStack.Inference; //Yangtool 8.0
import org.xml.sax.SAXException;

public class TestORANReadHardware {

	/*
	 * private static final QNameModule IETF_HARDWARE_MODULE =
	 * QNameModule.create(XMLNamespace.of(
	 * "urn:ietf:params:xml:ns:yang:ietf-hardware"), Revision.of("2018-03-13"));
	 */ // Applicable for Yangtools 8.0
	// private static final URI IETF_HARDWARE_URI =
	// URI.create("urn:ietf:params:xml:ns:yang:ietf-hardware");
	private static final QNameModule IETF_HARDWARE_MODULE = QNameModule
			.create(XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-hardware"), Revision.of("2018-03-13"));
	private static final QName HW_CONTAINER = QName.create(IETF_HARDWARE_MODULE, "hardware");
	private static final QName HW_COMPONENT_LIST = QName.create(IETF_HARDWARE_MODULE, "component");
	private static final QName HW_COMPONENT_LIST_KEY = QName.create(IETF_HARDWARE_MODULE, "name");
	private static final QName HW_COMPONENT_LIST_CLASS = QName.create(IETF_HARDWARE_MODULE, "class");
	private static final QName HW_COMPONENT_LIST_PHYSICAL_INDEX = QName.create(IETF_HARDWARE_MODULE, "physical-index"); // leaf:int32
	private static final QName HW_COMPONENT_LIST_DESC = QName.create(IETF_HARDWARE_MODULE, "description"); // leaf:String
	private static final QName HW_COMPONENT_LIST_PARENT = QName.create(IETF_HARDWARE_MODULE, "parent"); // leaf:leafref
	private static final QName HW_COMPONENT_LIST_PARENT_REL_POS = QName.create(IETF_HARDWARE_MODULE, "parent-rel-pos"); // leaf:int32
	private static final QName HW_COMPONENT_LIST_CONTAINS_CHILD = QName.create(IETF_HARDWARE_MODULE, "contains-child"); // leaf-list:leafref
	private static final QName HW_COMPONENT_LIST_HW_REV = QName.create(IETF_HARDWARE_MODULE, "hardware-rev"); // leaf:String
	private static final QName HW_COMPONENT_LIST_FW_REV = QName.create(IETF_HARDWARE_MODULE, "firmware-rev"); // leaf:String
	private static final QName HW_COMPONENT_LIST_SW_REV = QName.create(IETF_HARDWARE_MODULE, "software-rev"); // leaf:String
	private static final QName HW_COMPONENT_LIST_SER_NUM = QName.create(IETF_HARDWARE_MODULE, "serial-num"); // leaf:String
	private static final QName HW_COMPONENT_LIST_MFG_NAME = QName.create(IETF_HARDWARE_MODULE, "mfg-name"); // leaf:String
	private static final QName HW_COMPONENT_LIST_MODEL_NAME = QName.create(IETF_HARDWARE_MODULE, "model-name"); // leaf:String
	private static final QName HW_COMPONENT_LIST_ALIAS = QName.create(IETF_HARDWARE_MODULE, "alias"); // leaf:String
	private static final QName HW_COMPONENT_LIST_ASSET_ID = QName.create(IETF_HARDWARE_MODULE, "asset-id"); // leaf:String
	private static final QName HW_COMPONENT_LIST_IS_FRU = QName.create(IETF_HARDWARE_MODULE, "is-fru"); // leaf:boolean
	private static final QName HW_COMPONENT_LIST_MFG_DATE = QName.create(IETF_HARDWARE_MODULE, "mfg-date"); // leaf:yang:date-and-time
	private static final QName HW_COMPONENT_LIST_URI = QName.create(IETF_HARDWARE_MODULE, "uri"); // leaf-list:inet:uri
	private static final QName HW_COMPONENT_LIST_UUID = QName.create(IETF_HARDWARE_MODULE, "uuid"); // leaf:yang:uuid
	private static final QName HW_COMPONENT_LIST_STATE = QName.create(IETF_HARDWARE_MODULE, "state"); // leaf:yang:uuid
	private static final QName HW_COMPONENT_LIST_ADMIN_STATE = QName.create(IETF_HARDWARE_MODULE, "admin-state"); // leaf:yang:uuid
	private static final QName HW_COMPONENT_LIST_OPER_STATE = QName.create(IETF_HARDWARE_MODULE, "oper-state"); // leaf:yang:uuid

	private static EffectiveModelContext schemaContext;
	private static Inference hwContainerSchema;

	@BeforeClass
	public static void setup() throws IOException {
		schemaContext = YangParserTestUtils.parseYangResourceDirectory("/");
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
		System.out.println("Hardware Data = " + transformedInput);

		ContainerNode hwContainer = (ContainerNode) transformedInput;
		MapNode containerMap = (MapNode) hwContainer.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST));
		Collection<MapEntryNode> containerMapEntries = containerMap.body();
		for (MapEntryNode mapEntryNode : containerMapEntries) {
			NodeIdentifierWithPredicates componentKey = mapEntryNode.getIdentifier(); // list key
			System.out.println("Key Name is - " + componentKey.keySet());
			System.out.println("Key Value is - " + componentKey.getValue(HW_COMPONENT_LIST_KEY));

			LeafNode<?> classField = (LeafNode<?>) mapEntryNode
					.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_CLASS));
			System.out.println("Class = " + classField.getIdentifier().getNodeType().getLocalName() + " Value = "
					+ classField.body().toString());
			Object obj = classField.body();
			System.out.println(obj.getClass());
			if (obj instanceof QName) {
				System.out.println("This is of type QName");
			}
			LeafNode<?> aliasLeaf = (LeafNode<?>) mapEntryNode
					.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_ALIAS));
			// System.out.println("Alias = " + aliasLeaf.getValue().toString());
			System.out.println("Alias = " + getLeafValue(mapEntryNode, HW_COMPONENT_LIST_ALIAS));

			try {
				DataContainerChild childSet = mapEntryNode
						.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_CONTAINS_CHILD));

				Collection<?> childEntry = (Collection<?>) childSet.body();
				Iterator<?> childEntryItr = childEntry.iterator();
				while (childEntryItr.hasNext()) {
					LeafSetEntryNode<?> childEntryNode = (LeafSetEntryNode<?>) childEntryItr.next();
					System.out.println("Child Node - " + childEntryNode.body());
				}
			} catch (VerifyException ve) {
				// System.out.println("Child not not exist");
			}

			try {
				LeafSetNode<?> containsChildSet = (LeafSetNode<?>) mapEntryNode
						.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_CONTAINS_CHILD));
				Collection<?> containsChildSetEntry = containsChildSet.body();
				Iterator<?> childItr = containsChildSetEntry.iterator();
				while (childItr.hasNext()) {
					LeafSetEntryNode<?> childEntryNode = (LeafSetEntryNode<?>) childItr.next();
					System.out.println("Child Node - " + childEntryNode.body());
				}
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}

			try {
				System.out
						.println(CodeHelpers.nonnull(getLeafListValue(mapEntryNode, HW_COMPONENT_LIST_CONTAINS_CHILD)));
				for (String childUuid : CodeHelpers
						.nonnull(getLeafListValue(mapEntryNode, HW_COMPONENT_LIST_CONTAINS_CHILD))) {
					System.out.println("Calling recursively - " + childUuid);
				}
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String description = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_DESC))
						.body();
				System.out.println("Description = " + description);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String serialNum = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_SER_NUM))
						.body();
				System.out.println("Serial Number = " + serialNum);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String firmwareRev = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_FW_REV))
						.body();
				System.out.println("Firmware Rev = " + firmwareRev);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String swRev = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_SW_REV)).body();
				System.out.println("Software Rev = " + swRev);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String modelName = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_MODEL_NAME))
						.body();
				System.out.println("Model Name = " + modelName);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				Integer parentRelPos = (Integer) mapEntryNode
						.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_PARENT_REL_POS)).body();
				System.out.println("Parent Rel Pos = " + (parentRelPos != null ? parentRelPos.intValue() : null));
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String parent = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_PARENT))
						.body();
				System.out.println("Parent = " + parent);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String hwRev = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_HW_REV)).body();
				System.out.println("Hardware Revision = " + hwRev);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String mfgName = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_MFG_NAME))
						.body();
				System.out.println("Manufacturer Name = " + mfgName);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				DataContainerChild mfgNameOpt = mapEntryNode
						.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_MFG_NAME));
				if (mfgNameOpt != null) {
					System.out.println("Mfg Name - " + (String) mfgNameOpt.body());
				}
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String assetID = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_ASSET_ID))
						.body();
				System.out.println("Asset ID = " + assetID);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String mfgDate = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_MFG_DATE))
						.body();
				System.out.println("Mfg Date = " + mfgDate);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String uri = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_URI)).body();
				System.out.println("URI = " + uri);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				Boolean isFRU = (Boolean) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_IS_FRU))
						.body();
				System.out.println("IS FRU = " + isFRU);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}
			try {
				String uuid = (String) mapEntryNode.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_UUID)).body()
						.toString();
				System.out.println("UUID = " + uuid);
			} catch (VerifyException | NoSuchElementException e) {
				// System.out.println("Child not not exist");
			}

			try {
				ContainerNode state = (ContainerNode) mapEntryNode
						.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_STATE));
				String adminState = (String) state.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_ADMIN_STATE))
						.body().toString();       
				System.out.println("Admin State = " + adminState);
				String operState = (String) state.getChildByArg(new NodeIdentifier(HW_COMPONENT_LIST_OPER_STATE)).body()
						.toString();

				System.out.println("Oper State = " + operState);
			} catch (VerifyException | NoSuchElementException e) {
				System.out.println("Child not not exist");
			}

			System.out.println("********************************************");

		}
		// assertNotNull(transformedInput);

	}

	public static String getLeafValue(DataContainerNode componentEntry, QName leafQName) {
		NodeIdentifier leafNodeIdentifier = new NodeIdentifier(leafQName);
		try {
			LeafNode<?> optLeafNode = (LeafNode<?>) componentEntry.getChildByArg(leafNodeIdentifier);
			if (optLeafNode.body() instanceof QName) {
				System.out.println("Leaf is of type QName");
			}
			return optLeafNode.body().toString();
		} catch (NoSuchElementException nsee) {
			System.out.println("Leaf with QName {} not found" + leafQName.toString());
			return null;
		}
	}

	public static List<String> getLeafListValue(DataContainerNode componentEntry, QName leafListQName) {
		if (componentEntry instanceof MapEntryNode) {
			List<String> containsChildList = new ArrayList<String>();
			DataContainerChild childSet = componentEntry.getChildByArg(new NodeIdentifier(leafListQName));
			if (childSet != null) {
				Collection<?> childEntry = (Collection<?>) childSet.body();
				Iterator<?> childEntryItr = childEntry.iterator();
				while (childEntryItr.hasNext()) {
					LeafSetEntryNode<?> childEntryNode = (LeafSetEntryNode<?>) childEntryItr.next();
					containsChildList.add(childEntryNode.body().toString());
				}
			}
			return containsChildList;
		}
		return null;
	}

	@Test
	public void testIetfHardwareFromNormalizedNode() {
		buildIetfHardwareContainerNode();
	}

	private static NormalizedNode buildIetfHardwareContainerNode() {
		MapNode componentMap = Builders.mapBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST))
				.withChild(Builders.mapEntryBuilder()
						.withNodeIdentifier(
								NodeIdentifierWithPredicates.of(HW_COMPONENT_LIST, HW_COMPONENT_LIST_KEY, "chassis"))
						.withChild(
								Builders.leafBuilder().withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_CLASS))
										.withValue("ianahw:chassis").build())
						.withChild(Builders.leafBuilder()
								.withNodeIdentifier(new NodeIdentifier(HW_COMPONENT_LIST_PHYSICAL_INDEX)).withValue(1)
								.build())
						.build())
				.build();
		return componentMap;
	}

}
