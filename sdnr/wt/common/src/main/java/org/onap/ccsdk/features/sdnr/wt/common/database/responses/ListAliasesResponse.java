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

import java.text.ParseException;
import java.util.List;

import org.elasticsearch.client.Response;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntryList;

@Deprecated
public class ListAliasesResponse extends BaseResponse {

    /*
     * eventlog                   eventlog-v1                   - - -
     * faultlog                   faultlog-v1                   - - -
     * inventoryequipment         inventoryequipment-v1         - - -
     * historicalperformance24h   historicalperformance24h-v1   - - -
     * mediator-server            mediator-server-v1            - - -
     * networkelement-connection  networkelement-connection-v1  - - -
     * maintenancemode            maintenancemode-v1            - - -
     * historicalperformance15min historicalperformance15min-v1 - - -
     * faultcurrent               faultcurrent-v1               - - -
     * connectionlog              connectionlog-v1              - - -
     */
    private final AliasesEntryList entries;

    public ListAliasesResponse(Response response) throws ParseException {
        super(response);
        List<String> lines = this.getLines(response);
        this.entries = new AliasesEntryList();
        if (lines != null) {
            for (String line : lines) {
                this.entries.add(new AliasesEntry(line));
            }
        }
    }

    /**
     * 
     * @return null if parsing failed otherwise valid (=>no entries may also be valid)
     */
    public AliasesEntryList getEntries() {
        return this.entries;
    }



}
