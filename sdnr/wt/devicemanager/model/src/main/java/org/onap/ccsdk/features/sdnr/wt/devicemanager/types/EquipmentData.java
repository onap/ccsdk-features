/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.Inventory;

/**
 * Inventory means here equipment related information. This could be card, subrack.
 *
 */
public class EquipmentData {

    private final @NonNull List<Inventory> equipmentList;

    public EquipmentData() {
        equipmentList = new ArrayList<>();
    }

    public void clear() {
        equipmentList.clear();
    }

    /**
     * @param entity with one elements data
     */
    public void add(Inventory entity) {
        equipmentList.add(entity);
    }

    /**
     * @return true for empty list or false if elements in the list.
     */
    public boolean isEmpty() {
        return equipmentList.isEmpty();
    }

    /**
     * @param i index
     * @return Inventory from index
     */
    public Inventory get(int i) {
        return equipmentList.get(i);
    }

    /**
     * Get list with all equipment
     * 
     * @return list with equipment
     */
    public List<Inventory> getList() {
        return equipmentList;
    }

}
