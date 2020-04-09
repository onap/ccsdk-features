package org.onap.ccsdk.features.lib.doorman.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageData {

    private Map<String, Object> param;
    private String body;

    public MessageData(Map<String, Object> param, String body) {
        this.param = new HashMap<>(param);
        this.body = body;
    }

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

    public Map<String, Object> getParam() {
        return Collections.unmodifiableMap(param);
    }

    public String getBody() {
        return body;
    }
}
