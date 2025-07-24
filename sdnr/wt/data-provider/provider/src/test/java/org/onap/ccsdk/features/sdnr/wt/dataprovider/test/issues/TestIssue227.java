/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.test.issues;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.Uint32;

public class TestIssue227 extends Mockito {

    static String inputJsonString = "{\"value1\":\"forty-two\", \"value2\":\"forty-three\"}";
    static String inputJsonNumber = "{\"value1\":42, \"value2\":43}";

    @Test(expected = IOException.class)
    public void testWithException() throws IOException {
        String inputJson = inputJsonNumber;
        System.out.println("Input " + inputJson);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        doMapping(mapper, inputJson);
        fail("should have thrown an exception");
    }

    @Test
    public void testWithMixin() {
        String inputJson = inputJsonNumber;
        System.out.println("Input " + inputJson);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.addMixIn(TestBuilder.class, IgnoreFooSetValueIntMixIn.class);

        try {
            doMapping(mapper, inputJson);
        } catch (IOException e) {
            fail(e);
        }
    }


    private void doMapping(ObjectMapper mapper, String json) throws IOException {
        TestBuilder foo;
            foo = mapper.readValue(json.getBytes(), TestBuilder.class);
            System.out.println("Foo " + foo);
            System.out.println(mapper.writeValueAsString(foo));

    }

    static class TestBuilder {

        private String value1;
        private String value2;

        String getValue1() {
            return value1;
        }

        public void setValue1(String value) {
            this.value1 = value;
        }

        public void setValue1(int value) {
            this.value1 = String.valueOf(value);
        }

        public void setValue1(long value) {
            this.value1 = String.valueOf(value);
        }

        public void setValue1(Uint32 value) {
            this.value1 = String.valueOf(value);
        }

        public String getValue2() {
            return value2;
        }

        public void setValue2(String value) {
            this.value2 = value;
        }

        public void setValue2(int value) {
            this.value2 = String.valueOf(value);
        }

        @Override
        public String toString() {
            return "Foo [value1=" + value1 + ", value2=" + value2 + "]";
        }

    }


    private abstract class IgnoreFooSetValueIntMixIn {
        @JsonProperty
        public abstract void setValue1(String value);

        @JsonProperty
        public abstract void setValue2(String value);
    }
}


