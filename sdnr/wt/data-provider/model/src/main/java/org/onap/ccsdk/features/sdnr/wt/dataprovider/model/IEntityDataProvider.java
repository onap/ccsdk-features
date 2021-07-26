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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.model;

import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.StatusChangedHandler.StatusKey;

public interface IEntityDataProvider {

    /** Get provider for database read/write operations **/
    public DataProvider getDataProvider();

    /** Get provider to access read/write operations for maintenance **/
    public HtDatabaseMaintenance getHtDatabaseMaintenance();

    public HtUserdataManager getHtDatabaseUserManager();

    /** Set some static status information after startup */
    public void setStatus(StatusKey key, String value);

    /** Database configuration information **/
    public IEsConfig getEsConfig();

    /** Provide NetconfTimeStamp handler **/
    public NetconfTimeStamp getConverter();
}
