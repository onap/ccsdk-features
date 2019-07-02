
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "FAPServiceNumberOfEntries",
    "FAPServiceList"
})
public class RadioAccess {

    @JsonProperty("FAPServiceNumberOfEntries")
    private String fAPServiceNumberOfEntries;
    @JsonProperty("FAPServiceList")
    private List<FAPServiceList> fAPServiceList = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public RadioAccess() {
    }

    /**
     * 
     * @param fAPServiceList
     * @param fAPServiceNumberOfEntries
     */
    public RadioAccess(String fAPServiceNumberOfEntries, List<FAPServiceList> fAPServiceList) {
        super();
        this.fAPServiceNumberOfEntries = fAPServiceNumberOfEntries;
        this.fAPServiceList = fAPServiceList;
    }

    @JsonProperty("FAPServiceNumberOfEntries")
    public String getFAPServiceNumberOfEntries() {
        return fAPServiceNumberOfEntries;
    }

    @JsonProperty("FAPServiceNumberOfEntries")
    public void setFAPServiceNumberOfEntries(String fAPServiceNumberOfEntries) {
        this.fAPServiceNumberOfEntries = fAPServiceNumberOfEntries;
    }

    @JsonProperty("FAPServiceList")
    public List<FAPServiceList> getFAPServiceList() {
        return fAPServiceList;
    }

    @JsonProperty("FAPServiceList")
    public void setFAPServiceList(List<FAPServiceList> fAPServiceList) {
        this.fAPServiceList = fAPServiceList;
    }

	@Override
	public String toString() {
		return "RadioAccess [fAPServiceNumberOfEntries=" + fAPServiceNumberOfEntries + ", fAPServiceList="
				+ fAPServiceList + "]";
	}

}
