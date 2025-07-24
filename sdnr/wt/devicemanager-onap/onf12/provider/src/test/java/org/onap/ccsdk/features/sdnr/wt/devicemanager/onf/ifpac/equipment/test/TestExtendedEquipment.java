/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.test;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.equipment.ExtendedEquipment;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.EquipmentBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ContainedHolderBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ManufacturedThing;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.equipment.g.ManufacturedThingBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentInstance;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentInstanceBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentType;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.EquipmentTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.ManufacturerProperties;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.manufactured.thing.g.ManufacturerPropertiesBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestExtendedEquipment {

    UniversalId id = new UniversalId("1234567");
    private static final String NODEID = "NODE1";
    private static final Logger LOG = LoggerFactory.getLogger(TestExtendedEquipment.class);

    @Test
    public void test() {
        final @NonNull List<ContainedHolder> holder = Arrays.asList(new ContainedHolderBuilder().setUuid(id).build());
        final @NonNull ManufacturerProperties manufacturerProps = new ManufacturerPropertiesBuilder()
                .setManufacturerIdentifier("NOK").setManufacturerName("Nokia").build();

        final @NonNull EquipmentType eqptType =
                new EquipmentTypeBuilder().setDescription("5G RAN Equipment").setModelIdentifier("NOK-987-1T")
                        .setPartTypeIdentifier("ABCDEF").setTypeName("12345").setVersion("5T9V4567").build();

        final @NonNull EquipmentInstance eqptInstance =
                new EquipmentInstanceBuilder().setAssetInstanceIdentifier("NOK1234-ABCD")
                        .setManufactureDate("2020-02-11").setSerialNumber("123456ABCD").build();

        final @NonNull ManufacturedThing manufacturedThing =
                new ManufacturedThingBuilder().setManufacturerProperties(manufacturerProps).setEquipmentType(eqptType)
                        .setEquipmentInstance(eqptInstance).build();

        Equipment equipment = new EquipmentBuilder().setUuid(id).setContainedHolder(YangToolsMapperHelper.toMap(holder))
                .setManufacturedThing(manufacturedThing).build();

        ExtendedEquipment extEqpt = new ExtendedEquipment(NODEID, "1234567890", equipment, "/var/opt", 3);
        assertEquals(extEqpt.getNodeId(), NODEID);
        assertEquals(extEqpt.getParentUuid(), "1234567890");
        assertEquals(extEqpt.getEquipment(), equipment);
        LOG.info(extEqpt.toString());
        extEqpt.getCreateInventoryInput();

    }

}
