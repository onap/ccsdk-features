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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.AlarmNotif;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.alarm.AffectedObjects;
import org.opendaylight.yang.gen.v1.urn.o.ran.fm._1._0.rev190204.alarm.AffectedObjectsKey;
import org.opendaylight.yangtools.yang.binding.Augmentation;
import org.opendaylight.yangtools.yang.common.Uint16;

public class TestAlarmNotif implements AlarmNotif {

    private static final Uint16 FAULT_ID = Uint16.valueOf(123);

    @Override
    public <A extends Augmentation<AlarmNotif>> @Nullable A augmentation(Class<A> augmentationType) {
        return null;
    }

    @Override
    public @Nullable Uint16 getFaultId() {
        return FAULT_ID;
    }

    @Override
    public @Nullable String getFaultSource() {
        return "ORAN-RU-FH";
    }

    @Override
    public @Nullable Map<AffectedObjectsKey, AffectedObjects> getAffectedObjects() {
        return null;
    }

    @Override
    public @Nullable FaultSeverity getFaultSeverity() {
        return FaultSeverity.CRITICAL;
    }

    public @Nullable Boolean isIsCleared() {
        return true;
    }

    @Override
    public @Nullable String getFaultText() {
        return "CPRI Port Down";
    }

    @Override
    public @Nullable DateAndTime getEventTime() {
        return new DateAndTime("2021-03-23T18:19:42.326144Z");
    }

    @Override
    public @NonNull Map<Class<? extends Augmentation<AlarmNotif>>, Augmentation<AlarmNotif>> augmentations() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Boolean getIsCleared() {
        return true;
    }

}
