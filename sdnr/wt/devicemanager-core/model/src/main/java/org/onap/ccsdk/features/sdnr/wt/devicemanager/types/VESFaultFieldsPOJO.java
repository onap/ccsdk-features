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

    public String getAlarmCondition() {
        return alarmCondition;
    }

    public void setAlarmCondition(String alarmCondition) {
        this.alarmCondition = alarmCondition;
    }

    public String getAlarmInterfaceA() {
        return alarmInterfaceA;
    }

    public void setAlarmInterfaceA(String alarmInterfaceA) {
        this.alarmInterfaceA = alarmInterfaceA;
    }

    public String getEventCategory() {
        return eventCategory;
    }

    public void setEventCategory(String eventCategory) {
        this.eventCategory = eventCategory;
    }

    public String getEventSeverity() {
        return eventSeverity;
    }

    public void setEventSeverity(String eventSeverity) {
        this.eventSeverity = eventSeverity;
    }

    public String getEventSourceType() {
        return eventSourceType;
    }

    public void setEventSourceType(String eventSourceType) {
        this.eventSourceType = eventSourceType;
    }

    public String getFaultFieldsVersion() {
        return faultFieldsVersion;
    }

    public void setFaultFieldsVersion(String faultFieldsVersion) {
        this.faultFieldsVersion = faultFieldsVersion;
    }

    public String getSpecificProblem() {
        return specificProblem;
    }

    public void setSpecificProblem(String specificProblem) {
        this.specificProblem = specificProblem;
    }

    public String getVfStatus() {
        return vfStatus;
    }

    public void setVfStatus(String vfStatus) {
        this.vfStatus = vfStatus;
    }

    public HashMap<String, Object> getAlarmAdditionalInformation() {
        return alarmAdditionalInformation;
    }

    public void setAlarmAdditionalInformation(HashMap<String, Object> alarmAdditionalInformation) {
        this.alarmAdditionalInformation = alarmAdditionalInformation;
    }

}
