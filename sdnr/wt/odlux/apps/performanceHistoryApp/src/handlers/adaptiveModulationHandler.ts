/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
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
 */
import { createExternal, IExternalTableState } from '../../../../framework/src/components/material-table/utilities';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';

import { AdaptiveModulationDataType, AdaptiveModulationResult } from '../models/adaptiveModulationDataType';
import { getFilter } from '../utils/tableUtils';

export interface IAdaptiveModulationState extends IExternalTableState<AdaptiveModulationDataType> { }

/**
 * Creates elastic search material data fetch handler for Adaptive modulation from historicalperformance database.
 */
const adaptiveModulationSearchHandler = createSearchDataHandler<AdaptiveModulationResult, AdaptiveModulationDataType>(
    getFilter,
    null,
    (hit) => ({
        _id: hit._id,
        radioSignalId: hit._source["radio-signal-id"],
        scannerId: hit._source["scanner-id"],
        utcTimeStamp: hit._source["time-stamp"],
        suspectIntervalFlag: hit._source["suspect-interval-flag"],
        time2StatesS: hit._source["performance-data"]["time2-states-s"],
        time2States: hit._source["performance-data"]["time2-states"],
        time2StatesL: hit._source["performance-data"]["time2-states-l"],
        time4StatesS: hit._source["performance-data"]["time4-states-s"],
        time4States: hit._source["performance-data"]["time4-states"],
        time4StatesL: hit._source["performance-data"]["time4-states-l"],
        time16StatesS: hit._source["performance-data"]["time16-states-s"],
        time16States: hit._source["performance-data"]["time16-states"],
        time16StatesL: hit._source["performance-data"]["time16-states-l"],
        time32StatesS: hit._source["performance-data"]["time32-states-s"],
        time32States: hit._source["performance-data"]["time32-states"],
        time32StatesL: hit._source["performance-data"]["time32-states-l"],
        time64StatesS: hit._source["performance-data"]["time64-states-s"],
        time64States: hit._source["performance-data"]["time64-states"],
        time64StatesL: hit._source["performance-data"]["time64-states-l"],
        time128StatesS: hit._source["performance-data"]["time128-states-s"],
        time128States: hit._source["performance-data"]["time128-states"],
        time128StatesL: hit._source["performance-data"]["time128-states-l"],
        time256StatesS: hit._source["performance-data"]["time256-states-s"],
        time256States: hit._source["performance-data"]["time256-states"],
        time256StatesL: hit._source["performance-data"]["time256-states-l"],
        time512StatesS: hit._source["performance-data"]["time512-states-s"],
        time512States: hit._source["performance-data"]["time512-states"],
        time512StatesL: hit._source["performance-data"]["time512-states-l"],
        time1024StatesS: hit._source["performance-data"]["time1024-states-s"],
        time1024States: hit._source["performance-data"]["time1024-states"],
        time1024StatesL: hit._source["performance-data"]["time1024-states-l"],
        time2048StatesS: hit._source["performance-data"]["time2048-states-s"],
        time2048States: hit._source["performance-data"]["time2048-states"],
        time2048StatesL: hit._source["performance-data"]["time2048-states-l"],
        time4096StatesS: hit._source["performance-data"]["time4096-states-s"],
        time4096States: hit._source["performance-data"]["time4096-states"],
        time4096StatesL: hit._source["performance-data"]["time4096-states-l"],
        time8192StatesS: hit._source["performance-data"]["time8192-states-s"],
        time8192States: hit._source["performance-data"]["time8192-states"],
        time8192StatesL: hit._source["performance-data"]["time8192-states-l"],
    }),
    (pmDataEntry: string) => {
        switch (pmDataEntry) {
            case "radioSignalId":
                return "radio-signal-id";
            case "scannerId":
                return "scanner-id";
            case "utcTimeStamp":
                return "time-stamp"
            case "suspectIntervalFlag":
                return "suspect-interval-flag";
        }
        return pmDataEntry
    });

export const {
    actionHandler: adaptiveModulationActionHandler,
    createActions: createAdaptiveModulationActions,
    createProperties: createAdaptiveModulationProperties,
    createPreActions: createAdaptiveModulationPreActions,
    reloadAction: adaptiveModulationReloadAction,
} = createExternal<AdaptiveModulationDataType>(adaptiveModulationSearchHandler, appState => appState.performanceHistory.adaptiveModulation);

