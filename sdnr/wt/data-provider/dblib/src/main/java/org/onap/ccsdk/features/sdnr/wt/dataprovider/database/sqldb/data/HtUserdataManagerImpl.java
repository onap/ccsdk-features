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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data;

import java.util.Arrays;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.data.rpctypehelper.QueryResult;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterUserdata;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EntityInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.ReadFaultcurrentListInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.FilterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.entity.input.PaginationBuilder;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;

public class HtUserdataManagerImpl extends HtUserdataManagerBase {

    private final SqlDBReaderWriterUserdata rw;

    public HtUserdataManagerImpl(SqlDBReaderWriterUserdata rw) {
        this.rw = rw;
    }

    @Override
    public boolean setUserdata(String username, String data) {
        return this.rw.write(new UserdataBuilder().setId(username).setValue(data).build(), username) != null;
    }

    @Override
    public boolean removeUserdata(String username) {
        return this.rw.remove(username) > 0;
    }

    @Override
    protected String readUserdata(String username, String defaultValue) {
        EntityInput input = new ReadFaultcurrentListInputBuilder()
                .setFilter(Arrays.asList(new FilterBuilder().setProperty("id").setFiltervalue(username).build()))
                .setPagination(new PaginationBuilder().setPage(Uint64.valueOf(1)).setSize(Uint32.valueOf(1)).build())
                .build();
        QueryResult<Userdata> result = this.rw.getData(input);
        if (result != null) {
            List<Userdata> data = result.getResult();
            Userdata user = (data != null && !data.isEmpty()) ? data.get(0) : null;
            return user == null ? defaultValue : user.getValue();
        }
        return defaultValue;
    }

}
