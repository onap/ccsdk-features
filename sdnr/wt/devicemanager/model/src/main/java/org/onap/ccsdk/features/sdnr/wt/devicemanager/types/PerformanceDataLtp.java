/*******************************************************************************
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import java.util.ArrayList;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntity;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.PmdataEntityBuilder;

/**
 * @author herbert
 *
 */
public class PerformanceDataLtp {

    private final List<PmdataEntity> pmList = new ArrayList<>();

    public void add(PmdataEntity entry) {
        pmList.add(entry);
    }

    public void add(PmdataEntityBuilder entryBuilder) {
        pmList.add(entryBuilder.build());
    }

    /**
     * @return int number of elements
     */
    public int size() {
        return pmList.size();
    }

    public List<PmdataEntity> getList() {
        return pmList;
    }

}
