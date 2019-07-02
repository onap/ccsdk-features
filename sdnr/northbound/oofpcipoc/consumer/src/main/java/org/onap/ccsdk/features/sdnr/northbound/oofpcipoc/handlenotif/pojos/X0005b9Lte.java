
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
