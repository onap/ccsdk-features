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

import java.util.ArrayList;
import java.util.List;
import org.onap.ccsdk.features.sdnr.wt.common.database.data.DatabaseVersion;


/**
 * @author Michael Dürre
 *
 */
public enum ReleaseGroup {

    EL_ALTO(Release.EL_ALTO), FRANKFURT(Release.FRANKFURT_R1, Release.FRANKFURT_R2), GUILIN(
            Release.GUILIN_R1), HONOLULU(Release.HONOLULU_R1), ISTANBUL(Release.ISTANBUL_R1),
            JAKARTA(Release.JAKARTA_R1);

    public static final ReleaseGroup CURRENT_RELEASE = JAKARTA;

    private final List<Release> releases;

    ReleaseGroup(Release... values) {
        this.releases = new ArrayList<Release>();
        if (values != null) {
            for (Release r : values) {
                this.releases.add(r);
            }
        }
    }

    /**
     * @param dbVersion
     * @return
     */
    public Release getLatestCompatibleRelease(DatabaseVersion dbVersion) {
        Release match = null;
        for (Release r : this.releases) {
            if (r.isDbInRange(dbVersion)) {
                match = r;
            }
        }
        return match;
    }


}
