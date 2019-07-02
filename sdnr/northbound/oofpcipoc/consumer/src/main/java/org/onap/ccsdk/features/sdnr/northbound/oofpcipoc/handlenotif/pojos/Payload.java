
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "RadioAccess"
})
public class Payload {

    @JsonProperty("RadioAccess")
    private RadioAccess radioAccess;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Payload() {
    }

    /**
     * 
     * @param radioAccess
     */
    public Payload(RadioAccess radioAccess) {
        super();
        this.radioAccess = radioAccess;
    }

    @JsonProperty("RadioAccess")
    public RadioAccess getRadioAccess() {
        return radioAccess;
    }

    @JsonProperty("RadioAccess")
    public void setRadioAccess(RadioAccess radioAccess) {
        this.radioAccess = radioAccess;
    }

	@Override
	public String toString() {
		return "Payload [radioAccess=" + radioAccess + "]";
	}

}
