package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Map;

public class MessageData {

	public Map<String, Object> param;
	public String body;

	@Override
	public String toString() {
		StringBuilder ss = new StringBuilder();
		ss.append(param);
		String b = body;
		if (b != null && b.length() > 20) {
			b = b.substring(0, 20) + "...";
		}
		ss.append(b);
		return ss.toString();
	}
}
