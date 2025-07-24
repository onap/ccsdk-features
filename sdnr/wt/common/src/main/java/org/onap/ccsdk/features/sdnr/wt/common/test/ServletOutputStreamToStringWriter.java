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
package org.onap.ccsdk.features.sdnr.wt.common.test;

import java.io.IOException;
import java.io.StringWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;

public class ServletOutputStreamToStringWriter extends ServletOutputStream {

    // variables
    private final StringWriter out = new StringWriter();
    // end of variables

    public StringWriter getStringWriter() {
        return out;
    }

    @Override
    public void write(int arg0) throws IOException {
        out.write(arg0);
    }

    @Override
    public String toString() {
        return out.toString();
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
    }


}
