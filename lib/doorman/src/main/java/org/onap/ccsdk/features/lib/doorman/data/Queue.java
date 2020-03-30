package org.onap.ccsdk.features.lib.doorman.data;

public class Queue {

	public String type;
	public String id;

	@Override
	public String toString() {
		return type + "::" + id;
	}
}
