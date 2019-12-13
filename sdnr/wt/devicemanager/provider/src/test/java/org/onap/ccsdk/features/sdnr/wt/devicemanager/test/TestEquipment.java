/*******************************************************************************
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.onfcore.container.ExtendedEquipment;
import org.onap.ccsdk.features.sdnr.wt.yangtools.YangToolsMapper;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.EquipmentBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolderBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolderKey;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.ExtensionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.InventoryEntity;

import com.fasterxml.jackson.core.JsonProcessingException;

public class TestEquipment extends Mockito {

	YangToolsMapper yangtoolsMapper = new YangToolsMapper();

    @Test
    public void test1() {

    	ExtendedEquipment extendedEquipment = getTestEquipment(1);
        String extendedEquipmentString = extendedEquipment.toString();
        System.out.println(extendedEquipmentString);
    }

	@Test
	public void test2() {

		List<ExtendedEquipment> equipmentList = new ArrayList<>();
		equipmentList.add(getTestEquipment(2));
		equipmentList.add(getTestEquipment(3));

		InventoryEntity esEquipment;
		for (ExtendedEquipment equipment1 : equipmentList) {
			esEquipment = equipment1.getCreateInventoryInput();
			try {
				String json = yangtoolsMapper.writeValueAsString(esEquipment);
				System.out.println("JSON: "+json);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
			// eventRWEquipment.write(esEquipment,equipment1.getNodeId()+"/"+equipment1.getParentUuid());
		}
	}

	private ExtendedEquipment getTestEquipment(int number) {
		// Move to InventoryEntity
		EquipmentBuilder equipmentBuilder = new EquipmentBuilder();
		equipmentBuilder.setUuid(new UniversalId("EquipmentId"));
		ContainedHolderBuilder containedHolderBuilder = new ContainedHolderBuilder();
		List<Extension> xy = new ArrayList<>();
		xy.add(new ExtensionBuilder().setValueName("Test1").setValue("TestValue").build());
		containedHolderBuilder.setExtension(xy);
		containedHolderBuilder.withKey(new ContainedHolderKey(new UniversalId("MyKey" + number)));
		List<ContainedHolder> z = new ArrayList<>();
		z.add(containedHolderBuilder.build());
		equipmentBuilder.setContainedHolder(z);

		ExtendedEquipment extendedEquipment = new ExtendedEquipment("node" + number, "Parent", equipmentBuilder.build(),
				"X/" + number, number);
		return extendedEquipment;
	}
}
