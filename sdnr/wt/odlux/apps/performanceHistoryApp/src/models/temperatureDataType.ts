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
export { HitEntry, Result } from '../../../../framework/src/models';


/**
 * Represents Receive level data fields of the performance history table.
 */
export type TemperatureDatabaseDataType = {
  "rf-temp-min": number;
  "rf-temp-avg": number;
  "rf-temp-max": number;
};

/**
 * Represents Result type of database query
 */
export type TemperatureResult = {
  "performance-data": TemperatureDatabaseDataType
  "radio-signal-id": string;
  "scanner-id": string;
  "suspect-interval-flag": boolean;
  "time-stamp": string;
};

/**
 * Internally used type to provide table and chart data
 */
export type TemperatureDataType = {
  radioSignalId: string;
  scannerId: string;
  utcTimeStamp: string;
  suspectIntervalFlag: boolean;
  rfTempMin: number;
  rfTempAvg: number;
  rfTempMax: number;
} & { _id: string };


