/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import java.math.BigInteger;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "phyCellIdInUse",
    "pnfName"
})
public class X0005b9Lte {

    @JsonProperty("phyCellIdInUse")
    private String phyCellIdInUse;
    @JsonProperty("pnfName")
    private String pnfName;

    /**
     * No args constructor for use in serialization
     *
     */
    public X0005b9Lte() {
    }

    /**
     *
     * @param bigInteger
     * @param pnfName
     */
    public X0005b9Lte(String phyCellIdInUse, String pnfName) {
        super();
        this.phyCellIdInUse = phyCellIdInUse;
        this.pnfName = pnfName;
    }

    @JsonProperty("phyCellIdInUse")
    public String getPhyCellIdInUse() {
        return phyCellIdInUse;
    }

    @JsonProperty("phyCellIdInUse")
    public void setPhyCellIdInUse(String phyCellIdInUse) {
        this.phyCellIdInUse = phyCellIdInUse;
    }

    @JsonProperty("pnfName")
    public String getPnfName() {
        return pnfName;
    }

    @JsonProperty("pnfName")
    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

	@Override
	public String toString() {
		return "X0005b9Lte [phyCellIdInUse=" + phyCellIdInUse + ", pnfName=" + pnfName + "]";
	}

}
