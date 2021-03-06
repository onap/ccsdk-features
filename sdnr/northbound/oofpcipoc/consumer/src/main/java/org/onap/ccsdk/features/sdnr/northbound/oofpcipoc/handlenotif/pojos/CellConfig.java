/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "LTE"
})
public class CellConfig {

    @JsonProperty("LTE")
    private LTE lTE;

    /**
     * No args constructor for use in serialization
     * 
     */
    public CellConfig() {
    }

    /**
     * 
     * @param lTE
     */
    public CellConfig(LTE lTE) {
        super();
        this.lTE = lTE;
    }

    @JsonProperty("LTE")
    public LTE getLTE() {
        return lTE;
    }

    @JsonProperty("LTE")
    public void setLTE(LTE lTE) {
        this.lTE = lTE;
    }

	@Override
	public String toString() {
		return "CellConfig [lTE=" + lTE + "]";
	}

}
