
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "Payload"
})
public class PayloadObject {

    @JsonProperty("Payload")
    private Payload payload;

    /**
     * No args constructor for use in serialization
     * 
     */
    public PayloadObject() {
    }

    /**
     * 
     * @param payload
     */
    public PayloadObject(Payload payload) {
        super();
        this.payload = payload;
    }

    @JsonProperty("Payload")
    public Payload getPayload() {
        return payload;
    }

    @JsonProperty("Payload")
    public void setPayload(Payload payload) {
        this.payload = payload;
    }

	@Override
	public String toString() {
		return "PayloadObject [payload=" + payload + "]";
	}

}
