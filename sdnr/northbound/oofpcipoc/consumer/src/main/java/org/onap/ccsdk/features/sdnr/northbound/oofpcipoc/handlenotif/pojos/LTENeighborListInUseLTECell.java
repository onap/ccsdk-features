
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pnfName",
    "enable",
    "alias",
    "mustInclude",
    "plmnid",
    "cid",
    "phyCellId",
    "blacklisted"
})
public class LTENeighborListInUseLTECell {

    @JsonProperty("pnfName")
    private String pnfName;
    @JsonProperty("enable")
    private String enable;
    @JsonProperty("alias")
    private String alias;
    @JsonProperty("mustInclude")
    private String mustInclude;
    @JsonProperty("plmnid")
    private String plmnid;
    @JsonProperty("cid")
    private String cid;
    @JsonProperty("phyCellId")
    private String phyCellId;
    @JsonProperty("blacklisted")
    private String blacklisted;

    /**
     * No args constructor for use in serialization
     * 
     */
    public LTENeighborListInUseLTECell() {
    }

    /**
     * 
     * @param mustInclude
     * @param phyCellId
     * @param alias
     * @param enable
     * @param blacklisted
     * @param cid
     * @param pnfName
     * @param plmnid
     */
    public LTENeighborListInUseLTECell(String pnfName, String enable, String alias, String mustInclude, String plmnid, String cid, String phyCellId, String blacklisted) {
        super();
        this.pnfName = pnfName;
        this.enable = enable;
        this.alias = alias;
        this.mustInclude = mustInclude;
        this.plmnid = plmnid;
        this.cid = cid;
        this.phyCellId = phyCellId;
        this.blacklisted = blacklisted;
    }

    @JsonProperty("pnfName")
    public String getPnfName() {
        return pnfName;
    }

    @JsonProperty("pnfName")
    public void setPnfName(String pnfName) {
        this.pnfName = pnfName;
    }

    @JsonProperty("enable")
    public String getEnable() {
        return enable;
    }

    @JsonProperty("enable")
    public void setEnable(String enable) {
        this.enable = enable;
    }

    @JsonProperty("alias")
    public String getAlias() {
        return alias;
    }

    @JsonProperty("alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @JsonProperty("mustInclude")
    public String getMustInclude() {
        return mustInclude;
    }

    @JsonProperty("mustInclude")
    public void setMustInclude(String mustInclude) {
        this.mustInclude = mustInclude;
    }

    @JsonProperty("plmnid")
    public String getPlmnid() {
        return plmnid;
    }

    @JsonProperty("plmnid")
    public void setPlmnid(String plmnid) {
        this.plmnid = plmnid;
    }

    @JsonProperty("cid")
    public String getCid() {
        return cid;
    }

    @JsonProperty("cid")
    public void setCid(String cid) {
        this.cid = cid;
    }

    @JsonProperty("phyCellId")
    public String getPhyCellId() {
        return phyCellId;
    }

    @JsonProperty("phyCellId")
    public void setPhyCellId(String phyCellId) {
        this.phyCellId = phyCellId;
    }

    @JsonProperty("blacklisted")
    public String getBlacklisted() {
        return blacklisted;
    }

    @JsonProperty("blacklisted")
    public void setBlacklisted(String blacklisted) {
        this.blacklisted = blacklisted;
    }

	@Override
	public String toString() {
		return "LTENeighborListInUseLTECell [pnfName=" + pnfName + ", enable=" + enable + ", alias=" + alias
				+ ", mustInclude=" + mustInclude + ", plmnid=" + plmnid + ", cid=" + cid + ", phyCellId=" + phyCellId
				+ ", blacklisted=" + blacklisted + "]";
	}

}
