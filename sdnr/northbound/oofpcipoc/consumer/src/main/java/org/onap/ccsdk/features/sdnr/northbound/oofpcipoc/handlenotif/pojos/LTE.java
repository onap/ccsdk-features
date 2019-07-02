
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "RAN"
})
public class LTE {

    @JsonProperty("RAN")
    private RAN rAN;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LTE() {
    }

    /**
     * 
     * @param rAN
     */
    public LTE(RAN rAN) {
        super();
        this.rAN = rAN;
    }

    @JsonProperty("RAN")
    public RAN getRAN() {
        return rAN;
    }

    @JsonProperty("RAN")
    public void setRAN(RAN rAN) {
        this.rAN = rAN;
    }

	@Override
	public String toString() {
		return "LTE [rAN=" + rAN + "]";
	}

}
