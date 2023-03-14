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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import java.util.HashMap;

@JsonPropertyOrder({"arrayOfNamedHashMap", "changeContact", "changeIdentifier", "changeType", "newState", "oldState",
        "notificationFieldsVersion"})

public class VESNotificationFieldsPOJO {

    private ArrayList<HashMap<String, Object>> arrayOfNamedHashMap = new ArrayList<HashMap<String, Object>>();
    @JsonIgnore
    private HashMap<String, Object> namedHashMap = new HashMap<String, Object>();
    @JsonIgnore
    private HashMap<String, String> hashMap = new HashMap<String, String>();
    @JsonIgnore
    private String changeContact = "";
    private String changeIdentifier = "";
    private String changeType = "";
//    @JsonIgnore
    private String newState = "";
    @JsonIgnore
    private String oldState = "";
    @JsonIgnore
    private String stateInterface = "";
    private String notificationFieldsVersion = "2.0";

    public ArrayList<HashMap<String, Object>> getArrayOfNamedHashMap() {
        return arrayOfNamedHashMap;
    }

    public void setArrayOfNamedHashMap(ArrayList<HashMap<String, Object>> arrayOfNamedHashMap) {
        this.arrayOfNamedHashMap = arrayOfNamedHashMap;
    }

    public String getChangeContact() {
        return changeContact;
    }

    public void setChangeContact(String changeContact) {
        this.changeContact = changeContact;
    }

    public String getChangeIdentifier() {
        return changeIdentifier;
    }

    public void setChangeIdentifier(String changeIdentifier) {
        this.changeIdentifier = changeIdentifier;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getNewState() {
        return newState;
    }

    public void setNewState(String newState) {
        this.newState = newState;
    }

    public String getOldState() {
        return oldState;
    }

    public void setOldState(String oldState) {
        this.oldState = oldState;
    }

    public String getStateInterface() {
        return stateInterface;
    }

    public void setStateInterface(String stateInterface) {
        this.stateInterface = stateInterface;
    }

    public String getNotificationFieldsVersion() {
        return notificationFieldsVersion;
    }

    public void setNotificationFieldsVersion(String notificationFieldsVersion) {
        this.notificationFieldsVersion = notificationFieldsVersion;
    }


}