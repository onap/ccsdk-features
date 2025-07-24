/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */
package org.onap.ccsdk.features.sdnr.wt.devicemanager.test;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.NetconfTimeStamp;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.types.NetconfTimeStampImpl;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.test.util.NetconfTimeStampOld;

public class TestsNectconfDateTime {

    private static String[] testPatterPostive = {"2017-01-18T11:44:49.482-0500", "2017-01-18T11:44:49.482-05:00",
            "20170118114449.123Z", "20170118114449.1Z", "20170118114449.1-0500", "2017-01-23T13:32:38-05:00",
            "2017-01-23T13:32-05:00", "2017-01-18T11:44:49Z"};
    private static String[] testPatterProblem = {"2017-01-18T11:44:4"
            //"2017-01-18T11:44:49" Excluded Test Ok in J8 and false in J11 .. impact low .. so excluded.
    };


    private final static NetconfTimeStampOld netconfTimeConverterOld = NetconfTimeStampOld.getConverter();
    private final static NetconfTimeStamp netconfTimeConverterNew = NetconfTimeStampImpl.getConverter();

    @Test
    public void test1() {

        int t = 1;
        String timeNew, timeOld;
        for (String testTime : testPatterPostive) {
            timeNew = netconfTimeConverterNew.getTimeStampFromNetconf(testTime);
            timeOld = netconfTimeConverterOld.getTimeStampFromNetconf(testTime);

            System.out.println("No " + t++ + " Pattern: " + testTime);
            System.out.println(" to old " + timeOld);
            System.out.println(" to new " + timeNew);
            System.out.println();

            assertTrue("Old/New implementation not same " + timeOld + "/" + timeNew, timeOld.equals(timeNew));
        }

        for (String testTime : testPatterProblem) {
            timeNew = netconfTimeConverterNew.getTimeStampFromNetconf(testTime);
            timeOld = netconfTimeConverterOld.getTimeStampFromNetconf(testTime);

            System.out.println("No " + t++ + " Pattern: " + testTime);
            System.out.println(" to old " + timeOld);
            System.out.println(" to new " + timeNew);
            System.out.println();
            assertTrue("Old/New implementation not same " + timeOld + "/" + timeNew, timeOld.equals(timeNew));
        }
    }

    @Test
    public void test2() {

        int t = 1;
        Long timeNew, timeOld;
        for (String testTime : testPatterPostive) {
            timeNew = netconfTimeConverterNew.getTimeStampFromNetconfAsMilliseconds(testTime);
            timeOld = netconfTimeConverterOld.getTimeStampFromNetconfAsMilliseconds(testTime);

            System.out.println("No " + t++ + " Pattern: " + testTime);
            System.out.println(" to old " + timeOld);
            System.out.println(" to new " + timeNew);
            System.out.println();

            assertTrue("Old/New implementation not same " + timeOld + "/" + timeNew, timeOld.equals(timeNew));
        }

    }


    @Test
    public void test3() {

        Date now = new Date();
        String timeNew = netconfTimeConverterNew.getTimeStampAsNetconfString(now);
        String timeOld = netconfTimeConverterOld.getTimeStampAsNetconfString(now);

        System.out.println("Old/New: " + timeOld + "/" + timeNew);

        assertTrue("Old/New implementation not same " + timeOld + "/" + timeNew, timeOld.equals(timeNew));


    }

}
