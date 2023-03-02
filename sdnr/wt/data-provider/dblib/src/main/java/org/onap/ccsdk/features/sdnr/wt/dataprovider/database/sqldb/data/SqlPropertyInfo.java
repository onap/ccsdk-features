/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2023 highstreet technologies GmbH Intellectual Property.
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

import java.lang.reflect.Method;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.database.sqldb.database.SqlDBMapper;

public class SqlPropertyInfo {

    private final Class<?> javaType;
    private final String name;
    private final String sqlType;

    public SqlPropertyInfo(Method method) throws SqlDBMapper.UnableToMapClassException {
        this.name = SqlDBMapper.getColumnName(method);
        this.javaType = method.getReturnType();
        this.sqlType = SqlDBMapper.getDBType(this.javaType);
    }

    public SqlPropertyInfo(String name, Class<?> javaType, String sqlType){
        this.name =name;
        this.javaType = javaType;
        this.sqlType = sqlType;
    }

    public boolean isSqlStringType(){
        return String.class.equals(this.javaType);
    }

    public String getName() {
        return this.name;
    }

    public String getSqlType() {
        return sqlType;
    }
}
