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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.util;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.FaultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {

    private static final Logger log = LoggerFactory.getLogger(Debug.class);
    /**
     * LOG the newly added problems of the interface pac
     *
     * @param idxStart
     * @param uuid
     * @param resultList
     */
    public static void debugResultList(String uuid, FaultData resultList, int idxStart) {
        StringBuilder sb = new StringBuilder();
        int idx = 0;
        for (int t = idxStart; t < resultList.size(); t++) {
            sb.append(idx++);
            sb.append(":{");
            sb.append(resultList.get(t));
            sb.append('}');
        }
        log.debug("Found problems {} {}", uuid, sb);
    }
}
