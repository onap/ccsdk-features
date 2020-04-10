package org.onap.ccsdk.features.lib.doorman.it.tc2;

import java.util.HashMap;
import java.util.Map;
import org.onap.ccsdk.features.lib.doorman.data.Message;
import org.onap.ccsdk.features.lib.doorman.data.MessageData;
import org.onap.ccsdk.features.lib.doorman.impl.MessageHandlerBaseImpl;
import org.onap.ccsdk.features.lib.doorman.util.JsonUtil;

public class Handler extends MessageHandlerBaseImpl {

	public Handler() {
		setMaxParallelCount(100);
		setUpdateWaitTime(20);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String determineUpdateGroup(Message msg) {
		if (msg.request.body != null) {
			Map<String, Object> body = (Map<String, Object>) JsonUtil.jsonToData(msg.request.body);
			String op = (String) body.get("operation");
			String entityId = (String) body.get("entity_id");
			if ("update".equals(op)) {
				return entityId;
			}
		}
		return super.determineUpdateGroup(msg);
	}

	@Override
	protected long determineUpdateSequence(Message msg) {
		if (msg.request.param != null) {
			Long n = (Long) msg.request.param.get("sequence_number");
			if (n != null) {
				return n;
			}
		}
		return super.determineUpdateSequence(msg);
	}

	@Override
	protected MessageData completeResponse(Resolution r, Message msg) {
		if (r == Resolution.SKIPPED) {
			MessageData response = new MessageData();
			response.param = new HashMap<>();
			response.param.put("http_code", 200L);
			response.body = "{ \"status\": \"success\" }";
			return response;
		}
		return super.completeResponse(r, msg);
	}
}
