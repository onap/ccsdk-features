package org.onap.ccsdk.features.sdnr.wt.dataprovider.daexim.data;

public enum Release {
	
	EL_ALTO("el alto"),
	FRANKFURT("frankfurt"),
	GUILIN("guilin");
	private final String value;

	private Release(String s) {
		this.value = s;
	}
	@Override
	public String toString() {
		return this.value;
	}
	public String getValue() {
		return value;
	}
	public static Release getValueOf(String s) throws Exception  {
		s = s.toLowerCase();
		for(Release p:Release.values()) {
			if(p.value.equals(s)) {
				return p;
			}
		}
		throw new Exception("value not found");
	}
}
