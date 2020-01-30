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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf.ifpac.microwave;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.LayerProtocolName;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.core.model.rev170320.UniversalId;
import org.opendaylight.yang.gen.v1.urn.onf.params.xml.ns.yang.g._874._1.model.rev170320.GranularityPeriodType;

/**
 * @author herbert
 *
 */
public class Helper {

    private static @NonNull UniversalId DEFAULT_UniversalId = new UniversalId("Default");
    private static @NonNull LayerProtocolName DEFAULT_LayerProtocolName = new LayerProtocolName("default");
    private static @NonNull GranularityPeriodType DEFAULT_GranularityPeriodType = GranularityPeriodType.Unknown;
    private static @NonNull String DEFAULT_String = "";

    public static @NonNull UniversalId nnGetUniversalId(@Nullable UniversalId x) {
        return x == null ? DEFAULT_UniversalId : x;
    }

    public static @NonNull LayerProtocolName nnGetLayerProtocolName(@Nullable LayerProtocolName x) {
        return x == null ? DEFAULT_LayerProtocolName : x;
    }

    public static @NonNull GranularityPeriodType nnGetGranularityPeriodType(@Nullable GranularityPeriodType x) {
        return x == null ? DEFAULT_GranularityPeriodType : x;
    }

    public static @NonNull String nnGetString(@Nullable String x) {
        return x == null ? DEFAULT_String : x;
    }

}
