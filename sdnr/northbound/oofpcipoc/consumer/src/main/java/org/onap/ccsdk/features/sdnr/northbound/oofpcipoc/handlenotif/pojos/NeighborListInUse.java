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

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "LTECellNumberOfEntries",
    "LTENeighborListInUseLTECell"
})
public class NeighborListInUse {

    @JsonProperty("LTECellNumberOfEntries")
    private String lTECellNumberOfEntries;
    @JsonProperty("LTENeighborListInUseLTECell")
    private List<LTENeighborListInUseLTECell> lTENeighborListInUseLTECell = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NeighborListInUse() {
    }

    /**
     * 
     * @param lTENeighborListInUseLTECell
     * @param lTECellNumberOfEntries
     */
    public NeighborListInUse(String lTECellNumberOfEntries, List<LTENeighborListInUseLTECell> lTENeighborListInUseLTECell) {
        super();
        this.lTECellNumberOfEntries = lTECellNumberOfEntries;
        this.lTENeighborListInUseLTECell = lTENeighborListInUseLTECell;
    }

    @JsonProperty("LTECellNumberOfEntries")
    public String getLTECellNumberOfEntries() {
        return lTECellNumberOfEntries;
    }

    @JsonProperty("LTECellNumberOfEntries")
    public void setLTECellNumberOfEntries(String lTECellNumberOfEntries) {
        this.lTECellNumberOfEntries = lTECellNumberOfEntries;
    }

    @JsonProperty("LTENeighborListInUseLTECell")
    public List<LTENeighborListInUseLTECell> getLTENeighborListInUseLTECell() {
        return lTENeighborListInUseLTECell;
    }

    @JsonProperty("LTENeighborListInUseLTECell")
    public void setLTENeighborListInUseLTECell(List<LTENeighborListInUseLTECell> lTENeighborListInUseLTECell) {
        this.lTENeighborListInUseLTECell = lTENeighborListInUseLTECell;
    }

	@Override
	public String toString() {
		return "NeighborListInUse [lTECellNumberOfEntries=" + lTECellNumberOfEntries + ", lTENeighborListInUseLTECell="
				+ lTENeighborListInUseLTECell + "]";
	}
}
