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
package org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.data;

import org.onap.ccsdk.features.sdnr.wt.common.database.data.AliasesEntry;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.EsVersion;

public enum Release {
	
	EL_ALTO("el alto","_v1",new EsVersion(2,2,0),new EsVersion(2,2,0)),
	FRANKFURT_R1("frankfurt-R1","-v2",new EsVersion(6,4,3),new EsVersion(6,8,6)),
	FRANKFURT_R2("frankfurt-R2","",new EsVersion(6,4,3),new EsVersion(6,8,6)),
	FRANKFURT_R3("frankfurt-R3","",new EsVersion(6,4,3),new EsVersion(6,8,6)),
	
	GUILIN("guilin","",new EsVersion(6,4,3),new EsVersion(6,8,6));
	
	public static final Release CURRENT_RELEASE = Release.FRANKFURT_R1;
	
	private final String value;
	private final String dbSuffix;
	private final EsVersion minDbVersion;
	private final EsVersion maxDbVersion;
	
	private Release(String s,String dbsuffix,EsVersion minDbVersion,EsVersion maxDbVersion) {
		this.value = s;
		this.dbSuffix=dbsuffix;
		this.minDbVersion = minDbVersion;
		this.maxDbVersion = maxDbVersion;
	}
	@Override
	public String toString() {
		return this.value;
	}
	public String getValue() {
		return value;
	}
	public static Release getValueOf(String s) throws Exception  {
		//s = s.toLowerCase();
		for(Release p:Release.values()) {
			if(p.value.equals(s)) {
				return p;
			}
		}
		throw new Exception("value not found");
	}
	public static Release getValueBySuffix(String suffix) {
		for(Release r:Release.values()) {
			if(r.dbSuffix.equals(suffix))
				return r;
		}
		return null;
	}
	public static String getDbSuffix(AliasesEntry entry) throws Exception {
		ComponentName comp = ComponentName.getValueOf(entry.getAlias());
		if(comp!=null) {
			return entry.getIndex().substring(entry.getAlias().length());
		}
		return null;
	}
	/**
	 * @return
	 */
	public String getDBSuffix() {
		return this.dbSuffix;
	}
	/**
	 * @return
	 */
	public EsVersion getDBVersion() {
		return this.minDbVersion;
	}
	/**
	 * @param dbVersion2
	 * @return
	 */
	public boolean isDbInRange(EsVersion dbVersion) {
		return dbVersion.isNewerOrEqualThan(minDbVersion) && dbVersion.isOlderOrEqualThan(maxDbVersion);
	}
}
