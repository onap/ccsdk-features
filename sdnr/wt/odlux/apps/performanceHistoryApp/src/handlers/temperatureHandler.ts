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

import { TemperatureDataType, TemperatureResult } from '../models/temperatureDataType';
import { getFilter } from '../utils/tableUtils';

export interface ITemperatureState extends IExternalTableState<TemperatureDataType> { }

/**
 * Creates elastic search material data fetch handler for Temperature from historicalperformance database.
 */
const temperatureSearchHandler = createSearchDataHandler<TemperatureResult, TemperatureDataType>(
    getFilter,
    null,
    (hit) => ({
        _id: hit._id,
        radioSignalId: hit._source["radio-signal-id"],
        scannerId: hit._source["scanner-id"],
        utcTimeStamp: hit._source["time-stamp"],
        suspectIntervalFlag: hit._source["suspect-interval-flag"],
        rfTempMin: hit._source["performance-data"]["rf-temp-min"],
        rfTempAvg: hit._source["performance-data"]["rf-temp-avg"],
        rfTempMax: hit._source["performance-data"]["rf-temp-max"],
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
    actionHandler: temperatureActionHandler,
    createActions: createTemperatureActions,
    createProperties: createTemperatureProperties,
    createPreActions: createTemperaturePreActions,
    reloadAction: temperatureReloadAction,
} = createExternal<TemperatureDataType>(temperatureSearchHandler, appState => appState.performanceHistory.temperature);

