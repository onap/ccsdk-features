/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;

public class TestNetconfTimestamp {


    private static final String regex = "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\\.?[0-9]?Z";
    private static final String NETCONF_DEF_MILLIS_STRING = "2020-01-09T05:40:41.1Z";
    private static final String NETCONF_DEF_MILLIS_STRING_MALFORMAED = "2020-01-09T05:40:41:0Z";
    private static final String NETCONF_DEF2_MILLIS_STRING = "2020-01-09T05:40:41.111Z";
    private static final String NETCONF_DEF3_MILLIS_STRING = "2020-01-09T05:40:41Z";
    private static final String NETCONF_DEF3_MILLIS_STRING_CORRECTED = "2020-01-09T05:40:41.0Z";
    private static final String NETCONF_DEF4_MILLIS_STRING_MALFORMAED = "2020-01-09T05:40:41.0000Z";
    private static final long NETCONF_DEF_MILLIS_LONG = 1578548441100L;

    private static NetconfTimeStamp converter = NetconfTimeStampImpl.getConverter();

    @Test
    public void test() {
        String ts = converter.getTimeStampAsNetconfString();
        final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        final Matcher matcher = pattern.matcher(ts);
        assertTrue(matcher.find());
    }

    @Test
    public void testMillis() {
        long millis = converter.getTimeStampFromNetconfAsMilliseconds(NETCONF_DEF_MILLIS_STRING);
        assertEquals(NETCONF_DEF_MILLIS_LONG, millis);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testMillisMalformed() {
        converter.getTimeStampFromNetconfAsMilliseconds(NETCONF_DEF_MILLIS_STRING_MALFORMAED);

    }

    @Test
    public void test2() {
        assertEquals(NETCONF_DEF_MILLIS_STRING, converter.getTimeStampFromNetconf(NETCONF_DEF2_MILLIS_STRING));
        assertEquals(NETCONF_DEF3_MILLIS_STRING_CORRECTED,
                converter.getTimeStampFromNetconf(NETCONF_DEF3_MILLIS_STRING));
        assertTrue(converter.getTimeStampFromNetconf(NETCONF_DEF4_MILLIS_STRING_MALFORMAED).startsWith("Malformed"));
    }

    @Test
    public void test3() {
        Date dt = converter.getDateFromNetconf(NETCONF_DEF_MILLIS_STRING);
        final Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        c.setTime(dt);
        assertEquals(2020, c.get(Calendar.YEAR));
        assertEquals(0, c.get(Calendar.MONTH));
        assertEquals(9, c.get(Calendar.DAY_OF_MONTH));
        assertEquals(5, c.get(Calendar.HOUR_OF_DAY));
        assertEquals(40, c.get(Calendar.MINUTE));
        assertEquals(41, c.get(Calendar.SECOND));
        assertEquals(100, c.get(Calendar.MILLISECOND));


        assertEquals(NETCONF_DEF_MILLIS_STRING, converter.getTimeStampAsNetconfString(dt));
    }
}
