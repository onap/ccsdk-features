
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "CellIdentity",
    "NeighborListInUse"
})
public class RAN {

    @JsonProperty("CellIdentity")
    private String cellIdentity;
    @JsonProperty("NeighborListInUse")
    private NeighborListInUse neighborListInUse;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RAN() {
    }

    /**
     * 
     * @param neighborListInUse
     * @param cellIdentity
     */
    public RAN(String cellIdentity, NeighborListInUse neighborListInUse) {
        super();
        this.cellIdentity = cellIdentity;
        this.neighborListInUse = neighborListInUse;
    }

    @JsonProperty("CellIdentity")
    public String getCellIdentity() {
        return cellIdentity;
    }

    @JsonProperty("CellIdentity")
    public void setCellIdentity(String cellIdentity) {
        this.cellIdentity = cellIdentity;
    }

    @JsonProperty("NeighborListInUse")
    public NeighborListInUse getNeighborListInUse() {
        return neighborListInUse;
    }

    @JsonProperty("NeighborListInUse")
    public void setNeighborListInUse(NeighborListInUse neighborListInUse) {
        this.neighborListInUse = neighborListInUse;
    }

	@Override
	public String toString() {
		return "RAN [cellIdentity=" + cellIdentity + ", neighborListInUse=" + neighborListInUse + "]";
	}
}
