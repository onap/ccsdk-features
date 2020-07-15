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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.yangschema;

import javax.servlet.http.HttpServletRequest;

public class GetYangSchemaRequest {

    private static final String URI_PREFIX = "/yang-schema/";
    private final String module;
    private final String version;

    public String getModule() {
        return this.module;
    }

    public String getVersion() {
        return this.version;
    }

    public boolean hasVersion() {
        return this.version != null;
    }

    public GetYangSchemaRequest(HttpServletRequest req) throws Exception {
        String uri = req.getRequestURI().substring(URI_PREFIX.length());

        String[] hlp = uri.split("/");
        if (hlp.length < 1) {
            throw new Exception("no module request found");

        } else if (hlp.length == 1) {
            this.module = hlp[0];
            this.version = null;
        } else {
            this.module = hlp[0];
            this.version = hlp[1];
        }

    }

}
