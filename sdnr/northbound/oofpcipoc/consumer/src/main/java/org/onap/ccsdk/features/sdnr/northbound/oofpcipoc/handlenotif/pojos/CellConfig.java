
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
