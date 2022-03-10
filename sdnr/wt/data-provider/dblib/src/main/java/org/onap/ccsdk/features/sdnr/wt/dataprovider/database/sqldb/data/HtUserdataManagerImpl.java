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

import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBReaderWriterUserdata;

public class HtUserdataManagerImpl extends HtUserdataManagerBase {

    private final SqlDBReaderWriterUserdata rw;

    public HtUserdataManagerImpl(SqlDBReaderWriterUserdata rw) {
        this.rw = rw;
    }

    @Override
    public boolean setUserdata(String username, String data) {
        Userdata o = new UserdataBuilder().setId(username).setValue(data).build();
        String x = this.rw.updateOrInsert(o, username);
        return x!=null;
    }

    @Override
    public boolean removeUserdata(String username) {
        return this.rw.remove(username) > 0;
    }

    @Override
    protected String readUserdata(String username, String defaultValue) {
        Userdata user = this.rw.read(username);
        return user!=null? user.getValue():defaultValue;
    }

}
