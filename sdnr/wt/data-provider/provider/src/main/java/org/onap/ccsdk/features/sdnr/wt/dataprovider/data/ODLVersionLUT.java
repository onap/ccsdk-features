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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.data;

public class ODLVersionLUT {

	public static String getONAPReleaseName(String onapCCSDKVersion,String def) {
		if(onapCCSDKVersion==null) {
			return def;
		}
		if(onapCCSDKVersion.startsWith("1.6.")) {
			return "ONAP Guillin";
		}
		if(onapCCSDKVersion.startsWith("1.5.")) {
			return "ONAP Frankfurt";
		}
		if(onapCCSDKVersion.startsWith("1.4.")) {
			return "ONAP El Alto";
		}
		if(onapCCSDKVersion.startsWith("1.3.")) {
			return "ONAP El Alto";
		}
		return def;
	}
	public static String getOdlVersion(String onapCCSDKVersion,String def) {
		
		if(onapCCSDKVersion==null) {
			return def;
		}
		if(onapCCSDKVersion.startsWith("1.6.")) {
			return "sodium-SRX (0.11.X)";
		}
		if(onapCCSDKVersion.startsWith("1.5.")) {
			return "neon-SR1 (0.10.1)";
		}
		if(onapCCSDKVersion.startsWith("1.4.")) {
			return "neon-SR1 (0.10.1)";
		}
		if(onapCCSDKVersion.startsWith("1.3.")) {
			return "fluorine-SR2 (0.9.2)";
		}
		return def;
	}
}
