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

import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VESFaultFieldsPOJO {

    //fault domain Fields
    private String alarmCondition = "";
    private String alarmInterfaceA = "";
    private String eventCategory = "";
    private String eventSeverity = "";
    private String eventSourceType = "";
    private String faultFieldsVersion = "4.0";
    private String specificProblem = "";
    private String vfStatus = "";
    private HashMap<String, Object> alarmAdditionalInformation = new HashMap<String, Object>();

}
