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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14;

import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.impl.dataprovider.Onf14ToInternalDataModel;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.Equipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ActualEquipment;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ContainedHolder;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.ContainedHolderKey;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.equipment.detail.ManufacturedThing;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.manufactured.thing.EquipmentInstance;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.manufactured.thing.EquipmentType;
import org.opendaylight.yang.gen.v1.urn.onf.yang.core.model._1._4.rev191127.manufactured.thing.ManufacturerProperties;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;

public class TestOnf14ToInternalDataModel2 extends Mockito {

    private static NodeId nodeId;
    private static Equipment currentEq;
    private static Equipment parentEq;
    private static ActualEquipment component;
    private static ContainedHolder holder;
    private static ManufacturedThing manThing;
    private static ManufacturerProperties manProperties;
    private static EquipmentInstance eqInstance;
    private static EquipmentType eqType;

    @BeforeClass
    public static void init() {
        nodeId = mock(NodeId.class);
        currentEq = mock(Equipment.class);
        parentEq = mock(Equipment.class);
        component = mock(ActualEquipment.class);
        holder = mock(ContainedHolder.class);
        manThing = mock(ManufacturedThing.class);
        manProperties = mock(ManufacturerProperties.class);
        eqInstance = mock(EquipmentInstance.class);
        eqType = mock(EquipmentType.class);

        when(nodeId.getValue()).thenReturn("CoreModel-1-4-node");

        when(component.getLocalId()).thenReturn("actLocalEq");
        when(currentEq.getUuid()).thenReturn(new UniversalId("0Aabcdef-0abc-0cfD-0abC-0123456789AB"));
        when(parentEq.getUuid()).thenReturn(new UniversalId("0Aabcdef-0123-0abc-abcd-0123456789AB"));

        @NonNull Map<ContainedHolderKey, ContainedHolder> containedHolderList = new HashMap<>();
        containedHolderList.put(holder.key(),holder);
        when(currentEq.nonnullContainedHolder()).thenReturn(containedHolderList);

    }

    @Test
    public void test1() {

        Onf14ToInternalDataModel model = new Onf14ToInternalDataModel();

        when(currentEq.getActualEquipment()).thenReturn(component);
        when(holder.getOccupyingFru()).thenReturn(new UniversalId("12345678-0123-0abc-abcd-0123456789AB"));
        when(component.getManufacturedThing()).thenReturn(manThing);
        when(manThing.getManufacturerProperties()).thenReturn(manProperties);
        when(manThing.getEquipmentInstance()).thenReturn(eqInstance);
        when(eqInstance.getManufactureDate()).thenReturn(new DateAndTime("2020-02-05T12:30:45.283Z"));
        when(manThing.getEquipmentType()).thenReturn(eqType);

        model.getInternalEquipment(nodeId, currentEq, parentEq, 0);
        assertEquals(currentEq.getActualEquipment().getLocalId(), "actLocalEq");
    }

    @Test
    public void test2() {

        Onf14ToInternalDataModel model = new Onf14ToInternalDataModel();

        when(holder.getOccupyingFru()).thenReturn(null);
        when(component.getManufacturedThing()).thenReturn(null);
        when(manThing.getManufacturerProperties()).thenReturn(null);
        when(manThing.getEquipmentInstance()).thenReturn(null);
        when(manThing.getEquipmentType()).thenReturn(null);

        model.getInternalEquipment(nodeId, currentEq, null, 0);
    }


    @Test
    public void test3() {
        Onf14ToInternalDataModel model = new Onf14ToInternalDataModel();

        when(component.getManufacturedThing()).thenReturn(manThing);
        when(manThing.getManufacturerProperties()).thenReturn(null);
        when(manThing.getEquipmentInstance()).thenReturn(null);
        when(manThing.getEquipmentType()).thenReturn(null);

        model.getInternalEquipment(nodeId, currentEq, null, 0);
    }

    @Test
    public void test4() {
        Onf14ToInternalDataModel model = new Onf14ToInternalDataModel();

        when(currentEq.getActualEquipment()).thenReturn(null);
        model.getInternalEquipment(nodeId, currentEq, null, 0);
    }
}
