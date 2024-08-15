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
package org.onap.ccsdk.features.sdnr.wt.common.database.responses;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Deprecated
public class AggregationEntries extends LinkedHashMap<String, Long> {

    private static final long serialVersionUID = 1L;

    /**
     * Return page with keys
     * 
     * @param size elements
     * @param offset start position in list.
     * @return List<String> with selected values
     */
    public String[] getKeysAsPagedStringList(long size, long offset) {
        List<String> ltps = new ArrayList<String>();
        String[] keys = keySet().toArray(new String[0]);
        for (long i = offset; i < keys.length && i < offset + size; i++) {
            ltps.add(keys[(int) i]);
        }
        return ltps.toArray(new String[0]);
    }


}
