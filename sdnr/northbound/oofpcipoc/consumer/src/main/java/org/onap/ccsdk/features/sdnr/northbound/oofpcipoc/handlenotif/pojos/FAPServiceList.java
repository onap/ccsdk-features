
package org.onap.ccsdk.features.sdnr.northbound.oofpcipoc.handlenotif.pojos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "alias",
    "X0005b9Lte",
    "CellConfig"
})
public class FAPServiceList {

    @JsonProperty("alias")
    private String alias;
    @JsonProperty("X0005b9Lte")
    private X0005b9Lte x0005b9Lte;
    @JsonProperty("CellConfig")
    private CellConfig cellConfig;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FAPServiceList() {
    }

    /**
     * 
     * @param alias
     * @param cellConfig
     * @param x0005b9Lte
     */
    public FAPServiceList(String alias, X0005b9Lte x0005b9Lte, CellConfig cellConfig) {
        super();
        this.alias = alias;
        this.x0005b9Lte = x0005b9Lte;
        this.cellConfig = cellConfig;
    }

    @JsonProperty("alias")
    public String getAlias() {
        return alias;
    }

    @JsonProperty("alias")
    public void setAlias(String alias) {
        this.alias = alias;
    }

    @JsonProperty("X0005b9Lte")
    public X0005b9Lte getX0005b9Lte() {
        return x0005b9Lte;
    }

    @JsonProperty("X0005b9Lte")
    public void setX0005b9Lte(X0005b9Lte x0005b9Lte) {
        this.x0005b9Lte = x0005b9Lte;
    }

    @JsonProperty("CellConfig")
    public CellConfig getCellConfig() {
        return cellConfig;
    }

    @JsonProperty("CellConfig")
    public void setCellConfig(CellConfig cellConfig) {
        this.cellConfig = cellConfig;
    }

	@Override
	public String toString() {
		return "FAPServiceList [alias=" + alias + ", x0005b9Lte=" + x0005b9Lte + ", cellConfig=" + cellConfig + "]";
	}
}
