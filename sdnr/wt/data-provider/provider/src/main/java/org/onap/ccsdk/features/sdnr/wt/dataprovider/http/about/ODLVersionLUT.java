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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.http.about;

import java.util.HashMap;
import java.util.Map;

public class ODLVersionLUT {

    private static Map<String,String> odlMdsalVersionLUT=null;

    public static String getONAPReleaseName(String onapCCSDKVersion, String def) {
        if (onapCCSDKVersion == null) {
            return def;
        }
        if (onapCCSDKVersion.startsWith("2.1.")) {
            return "ONAP Honolulu";
        }
        if (onapCCSDKVersion.startsWith("2.0.")) {
            return "ONAP Guilin";
        }
        if (onapCCSDKVersion.startsWith("1.5.")) {
            return "ONAP Frankfurt";
        }
        if (onapCCSDKVersion.startsWith("1.4.")) {
            return "ONAP El Alto";
        }
        if (onapCCSDKVersion.startsWith("1.3.")) {
            return "ONAP El Alto";
        }
        if (onapCCSDKVersion.startsWith("1.2.")) {
            return "ONAP Guilin";
        }
        return def;
    }

    public static String getOdlVersion(String mdsalVersion, String def) {

        if (mdsalVersion == null) {
            return def;
        }
        if(odlMdsalVersionLUT==null) {
            odlMdsalVersionLUT = new HashMap<>();
            odlMdsalVersionLUT.put("5.0.14","magnesium-SR2 (0.12.2)");
            odlMdsalVersionLUT.put("5.0.10","magnesium-SR1 (0.12.1)");
            odlMdsalVersionLUT.put("5.0.9","magnesium-SR0 (0.12.0)");
            odlMdsalVersionLUT.put("4.0.17","sodium-SR4 (0.11.4)");
            odlMdsalVersionLUT.put("4.0.14","sodium-SR3 (0.11.3)");
            odlMdsalVersionLUT.put("4.0.11","sodium-SR2 (0.11.2)");
            odlMdsalVersionLUT.put("4.0.6","sodium-SR1 (0.11.1)");
            odlMdsalVersionLUT.put("4.0.4","sodium-SR0 (0.11.0)");
            odlMdsalVersionLUT.put("3.0.13","neon-SR3 (0.10.3)");
            odlMdsalVersionLUT.put("3.0.10","neon-SR2 (0.10.2)");
            odlMdsalVersionLUT.put("3.0.8","neon-SR1 (0.10.1)");
            odlMdsalVersionLUT.put("3.0.6","neon-SR0 (0.10.0)");
        }

        return odlMdsalVersionLUT.getOrDefault(mdsalVersion, def);
    }
}
