
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
