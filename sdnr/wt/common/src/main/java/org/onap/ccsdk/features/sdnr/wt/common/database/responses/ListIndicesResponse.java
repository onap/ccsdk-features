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
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.IndicesEntryList;

@Deprecated
public class ListIndicesResponse extends BaseResponse {

    /*
     * yellow open inventoryequipment-v1         5nNPRbJ3T9arMxqxBbJKyQ 5 1 0 0 1.2kb 1.2kb
     * yellow open eventlog-v1                   8lkfX97iT86dZdUlgVAktg 5 1 0 0 1.2kb 1.2kb
     * yellow open mediator-server-v1            8T4hNo61SgulupKntivY4Q 5 1 0 0 1.2kb 1.2kb
     * yellow open historicalperformance24h-v1   fRCGb7JYRdiry23HKWg0Hw 5 1 0 0 1.2kb 1.2kb
     * yellow open faultlog-v1                   kjsb50boTPOAzXMYdnfv4A 5 1 0 0 1.2kb 1.2kb
     * yellow open maintenancemode-v1            Q9ZsCgW0Q9m6nk49iOFNhA 5 1 0 0 1.2kb 1.2kb
     * yellow open historicalperformance15min-v1 BdEOe7X2RK2o5yTwNH5QQg 5 1 0 0 1.2kb 1.2kb
     * yellow open faultcurrent-v1               BdikWk9HQtS5aFpYEAac2g 5 1 0 0 1.2kb 1.2kb
     * yellow open networkelement-connection-v1  YT3lj0AKRoOmtN30Zbdfqw 5 1 0 0 1.2kb 1.2kb
     * yellow open connectionlog-v1              7yrVaaM1QjyO5eMsCUHNHQ 5 1 0 0 1.2kb 1.2kb
     */
    private final IndicesEntryList entries;

    public ListIndicesResponse(Response response) throws ParseException {
        super(response);
        List<String> lines = this.getLines(response);
        this.entries = new IndicesEntryList();
        if (lines != null) {
            for (String line : lines) {
                this.entries.add(new IndicesEntry(line));
            }
        }

    }

    /**
     * @return
     */
    public IndicesEntryList getEntries() {
        return this.entries;
    }



}
