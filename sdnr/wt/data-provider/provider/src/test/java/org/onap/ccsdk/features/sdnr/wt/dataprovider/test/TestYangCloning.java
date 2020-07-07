/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

public class TestYangCloning {
    /*
    @Test
    public void testEquipment() {
        EquipmentBuilder equipmentBuilder = new EquipmentBuilder();
        equipmentBuilder.setUuid( new UniversalId("EquipmentId"));
        equipmentBuilder.setContainedHolder(Arrays.asList(new ContainedHolderBuilder()
                .setUuid(new UniversalId("HolderId"))
                .setAdministrativeState(AdministrativeState.Locked)
                .setSupportedEquipment(Arrays.asList("eq1"))
                .build()));
        Equipment equipment = equipmentBuilder.build();
        InventoryEntity output = YangToolsCloner.instance().cloneToBuilder(equipment,  new InventoryBuilder())
                .setNodeId("node1").setUuid("a.a.a").setId("node1"+"/"+"a.a.a").build();
    
    
        System.out.println("source:");
        System.out.println(equipment);
        System.out.println("result:");
        System.out.println(output);
    
    
    }
    @Test
    public void testFaultCurrent() {
        ProblemNotificationXml source = new ProblemNotificationXml("node", "uuid", "problem", InternalSeverity.Critical,54,InternalDateAndTime.getTestpattern());
        FaultcurrentEntity output = YangToolsCloner.instance().clone(source,Faultcurrent.class);
    
          System.out.println("source:");
            System.out.println(source);
            System.out.println("result:");
            System.out.println(output);
    }
    */
}
