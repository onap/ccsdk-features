package org.onap.ccsdk.features.lib.doorman.util;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.onap.ccsdk.features.lib.doorman.testutil.FileUtil;
import org.onap.ccsdk.features.lib.doorman.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestJsonUtil {

    private static final Logger log = LoggerFactory.getLogger(TestJsonUtil.class);

    @SuppressWarnings("unchecked")
    @Test
    public void testJsonConversion() throws Exception {
        String json1 = FileUtil.read("/test1.json");
        Map<String, Object> data1 = (Map<String, Object>) JsonUtil.jsonToData(json1);
        String convJson1 = JsonUtil.dataToJson(data1);
        log.info("Converted JSON:\n" + convJson1);
        Map<String, Object> convData1 = (Map<String, Object>) JsonUtil.jsonToData(convJson1);
        Assert.assertEquals(data1, convData1);
    }
}
