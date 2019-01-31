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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.extension.g.Extension;

/**
 * ValueNameList is an access Wrapper to NETCONF Extension lists
 * Class is a specialized HashMap.
 */
public class ValueNameList extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	/**
	 * Create ValueNameList for NETCONF extensions
	 * @param extensionList Parameters as received from device. Could be null.
	 */
	public void put(@Nullable List<Extension> extensionList) {

		if (extensionList != null) {
			String name;
			String value;

			for (Extension e : extensionList) {
				name = e.getValueName();
				value = e.getValue();
				if (name != null && value != null) {
					put(name, value);
				}
			}
		}
	}

	/**
	 * Return value or null
	 * @param name key for element
	 * @return value if key exists; if not nul
	 */
	public String getOrNull(String name) {
		return containsKey(name) ? get(name) : null;
	}

	/**
	 * Get element as id list
	 * @param name key of element
	 * @param topLevelEqUuidList as input to add elements
	 * @return List<UniversalId>
	 */
	public  @Nonnull List<UniversalId> getAsUniversalIdList(String name, List<UniversalId> topLevelEqUuidList) {
		if (containsKey(name)) {
			String[] result = get(name).split(",\\s*");
			if (result.length > 0) {
				for (String e : result) {
					topLevelEqUuidList.add(new UniversalId(e));
				}
			}
		}
		return topLevelEqUuidList;
	}

}
