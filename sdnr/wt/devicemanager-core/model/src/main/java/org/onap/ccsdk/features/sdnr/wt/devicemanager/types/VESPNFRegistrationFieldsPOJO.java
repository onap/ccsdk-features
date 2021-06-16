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
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
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

    public Map<String, String> getAdditionalFields() {
        return additionalFields;
    }

    public void setAdditionalFields(Map<String, String> additionalFields) {
        this.additionalFields = additionalFields;
    }

    public String getLastServiceDate() {
        return lastServiceDate;
    }

    public void setLastServiceDate(String lastServiceDate) {
        this.lastServiceDate = lastServiceDate;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    public String getModelNumber() {
        return modelNumber;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public String getOamV4IpAddress() {
        return oamV4IpAddress;
    }

    public void setOamV4IpAddress(String oamV4IpAddress) {
        this.oamV4IpAddress = oamV4IpAddress;
    }

    public String getOamV6IpAddress() {
        return oamV6IpAddress;
    }

    public void setOamV6IpAddress(String oamV6IpAddress) {
        this.oamV6IpAddress = oamV6IpAddress;
    }

    public String getPnfRegistrationFieldsVersion() {
        return pnfRegistrationFieldsVersion;
    }

    public void setPnfRegistrationFieldsVersion(String pnfRegistrationFieldsVersion) {
        this.pnfRegistrationFieldsVersion = pnfRegistrationFieldsVersion;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getUnitFamily() {
        return unitFamily;
    }

    public void setUnitFamily(String unitFamily) {
        this.unitFamily = unitFamily;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;

    }

}
