/*******************************************************************************
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 ******************************************************************************/
package org.onap.ccsdk.features.sdnr.wt.devicemanager.aaiconnector.impl;

class URLParamEncoder {

    private URLParamEncoder() {
    }

    private static final String UNSAFE_CHARSET = " %$&+,/:;=?@<>#%";

    public static String encode(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append(escape(ch));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128) {
            return true;
        }
        return UNSAFE_CHARSET.indexOf(ch) >= 0;
    }

    private static String escape(char ch){
        return String.format("%c%c%c", '%', toHex(ch / 16), toHex(ch % 16));
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }
}
