/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=======================================================
 *
 */

package org.onap.ccsdk.features.lib.npm;

public class NpmConstants {
    public static final String PROPERTY_ENV_TYPE = "Env_Type";
    public static final String PROPERTY_ENV_PROD = "field";
    public static final String PROPERTY_ENV_SOLO = "solo";

    public static final String MDC_REQUEST_ID = "RequestID";

    public static final String SDNC_CONFIG_DIR = "SDNC_CONFIG_DIR";
    public static final String DEFAULT_SDNC_CONFIG_DIR = "/opt/sdnc/data/properties";
    public static final String NPM_CONFIG_PROPERTIES_FILE_NAME = "npm-config.properties";

    private NpmConstants() {}
}
