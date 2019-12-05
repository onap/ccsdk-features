package org.onap.ccsdk.features.sdnr.wt.dataprovider.daexim.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataContainer {

	private final Release release;
	private final Date created;
	private final List<ComponenentData> components;

	public DataContainer(Release release) {
		this(release,new Date());
	}
	public DataContainer(Release release,Date dt) {
		this.release = release;
		this.created = dt;
		this.components = new ArrayList<>();
	}

	
}