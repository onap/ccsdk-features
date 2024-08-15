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
package org.onap.ccsdk.features.sdnr.wt.common.database.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Dürre
 *
 */
@Deprecated
public class AliasesEntryList extends ArrayList<AliasesEntry> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param alias
     * @return
     */
    public AliasesEntry findByAlias(String alias) {
        for (AliasesEntry e : this) {
            if (e.getAlias().equals(alias)) {
                return e;
            }
        }
        return null;
    }

    /**
     * @return
     */
    public List<String> getLinkedIndices() {
        List<String> list = new ArrayList<String>();
        for (AliasesEntry e : this) {
            list.add(e.getIndex());
        }
        return list;
    }

}
