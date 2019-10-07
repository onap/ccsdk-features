/*******************************************************************************
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk feature sdnr wt
 *  ================================================================================
 * Copyright (C) 2019 Nokia Intellectual Property.
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
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class URLParamEncoderTest {

    @Test
    public void shouldEncodeStringsToFormatAcceptableByURL(){
        assertEquals("test", URLParamEncoder.encode("test"));
        assertEquals("test%20str", URLParamEncoder.encode("test str"));
        assertEquals("test%23%24str", URLParamEncoder.encode("test#$str"));
        assertEquals("test%20%25%24%26%2B%2C%2F%3A%3B%3D%3F%40%3C%3E%23%25str", URLParamEncoder.encode("test %$&+,/:;=?@<>#%str"));

    }
}
