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
 * Represents Adaptive Modulation data fields of the performance history table.
 */
export type AdaptiveModulationDatabaseDataType = {
  "time2-states-s": number;
  "time2-states": number;
  "time2-states-l": number;
  "time4-states-s": number;
  "time4-states": number;
  "time4-states-l": number;
  "time16-states-s": number;
  "time16-states": number;
  "time16-states-l": number;
  "time32-states-s": number;
  "time32-states": number;
  "time32-states-l": number;
  "time64-states-s": number;
  "time64-states": number;
  "time64-states-l": number;
  "time128-states-s": number;
  "time128-states": number;
  "time128-states-l": number;
  "time256-states-s": number;
  "time256-states": number;
  "time256-states-l": number;
  "time512-states-s": number;
  "time512-states": number;
  "time512-states-l": number;
  "time1024-states-s": number;
  "time1024-states": number;
  "time1024-states-l": number;
  "time2048-states-s": number;
  "time2048-states": number;
  "time2048-states-l": number;
  "time4096-states-s": number;
  "time4096-states": number;
  "time4096-states-l": number;
  "time8192-states-s": number;
  "time8192-states": number;
  "time8192-states-l": number;
};

/**
 * Represents Result type of database query
 */
export type AdaptiveModulationResult = {
  "performance-data": AdaptiveModulationDatabaseDataType;
  "radio-signal-id": string;
  "scanner-id": string;
  "suspect-interval-flag": boolean;
  "time-stamp": string;
};


/**
 * Internally used type to provide table and chart data
 */
export type AdaptiveModulationDataType = {
  radioSignalId: string;
  scannerId: string;
  utcTimeStamp: string;
  suspectIntervalFlag: boolean;
  "time2StatesS": number;
  "time2States": number;
  "time2StatesL": number;
  "time4StatesS": number;
  "time4States": number;
  "time4StatesL": number;
  "time16StatesS": number;
  "time16States": number;
  "time16StatesL": number;
  "time32StatesS": number;
  "time32States": number;
  "time32StatesL": number;
  "time64StatesS": number;
  "time64States": number;
  "time64StatesL": number;
  "time128StatesS": number;
  "time128States": number;
  "time128StatesL": number;
  "time256StatesS": number;
  "time256States": number;
  "time256StatesL": number;
  "time512StatesS": number;
  "time512States": number;
  "time512StatesL": number;
  "time1024StatesS": number;
  "time1024States": number;
  "time1024StatesL": number;
  "time2048StatesS": number;
  "time2048States": number;
  "time2048StatesL": number;
  "time4096StatesS": number;
  "time4096States": number;
  "time4096StatesL": number;
  "time8192StatesS": number;
  "time8192States": number;
  "time8192StatesL": number;
} & { _id: string };