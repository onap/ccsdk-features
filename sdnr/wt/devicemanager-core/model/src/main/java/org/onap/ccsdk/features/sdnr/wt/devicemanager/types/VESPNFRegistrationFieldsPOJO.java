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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@JsonInclude(Include.NON_NULL)
@Getter
@Setter
public class VESPNFRegistrationFieldsPOJO {

    private Map<String, String> additionalFields = new HashMap<String, String>();
    private String lastServiceDate;
    private String macAddress;
    private String manufactureDate;
    private String modelNumber;
    private String oamV4IpAddress;
    private String oamV6IpAddress;
    private String pnfRegistrationFieldsVersion = "2.0"; // This is the only mandatory field as per the VES Event schema definition
    private String serialNumber;
    private String softwareVersion;
    private String unitFamily;
    private String unitType;
    private String vendorName;

}
