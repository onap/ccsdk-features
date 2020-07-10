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

import org.onap.ccsdk.features.sdnr.wt.common.database.SearchHit;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.elalto.ElAltoReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.frankfurt.FrankfurtReleaseInformation;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.setup.guilin.GuilinReleaseInformation;

public interface SearchHitConverter {

    /**
     * convert single entry of database
     * 
     * @param source
     * @return
     */
    public SearchHit convert(SearchHit source);

    /**
     * @param component destination component
     * @param container source data
     * @return data for destination component
     */
    public ComponentData convert(DataContainer container);


    public static class Factory {
        public static SearchHitConverter getInstance(Release src, Release dst, ComponentName component) {
            switch (src) {
                case EL_ALTO:
                    return new ElAltoReleaseInformation().getConverter(dst, component);
                case FRANKFURT_R1:
                    return new FrankfurtReleaseInformation().getConverter(dst, component);
                case GUILIN:
                    return new GuilinReleaseInformation().getConverter(dst, component);
                default:
                    return null;

            }
        }
    }



}
