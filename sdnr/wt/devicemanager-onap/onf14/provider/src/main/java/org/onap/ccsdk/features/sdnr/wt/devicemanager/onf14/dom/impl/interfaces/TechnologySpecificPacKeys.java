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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.dom.impl.interfaces;


/**
 * Defining a structure that can map the LP local-id and its corresponding LTP uuid
 */
public class TechnologySpecificPacKeys {

    private String ltpUuid;
    private String localId;

    public TechnologySpecificPacKeys(String uuid, String lId) {
        this.ltpUuid = uuid;
        this.localId = lId;
    }

    public String getLtpUuid() {
        return ltpUuid;
    }

    public String getLocalId() {
        return localId;
    }

    public void setLtpUuid(String uuid) {
        this.ltpUuid = uuid;
    }

    public void setLocalId(String lId) {
        this.localId = lId;
    }
}
