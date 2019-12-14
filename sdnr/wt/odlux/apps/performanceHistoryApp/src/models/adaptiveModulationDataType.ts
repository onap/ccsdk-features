/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2019 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the License); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */
export { HitEntry, Result } from '../../../../framework/src/models';

/**
 * Represents Adaptive Modulation data fields of the performance history table.
 */
export type AdaptiveModulationDatabaseDataType = {
  _id: string ;
  time2StatesS: number;
  time2States: number;
  time2StatesL: number;
  time4StatesS: number;
  time4States: number;
  time4StatesL: number;
  time16StatesS: number;
  time16States: number;
  time16StatesL: number;
  time32StatesS: number;
  time32States: number;
  time32StatesL: number;
  time64StatesS: number;
  time64States: number;
  time64StatesL: number;
  time128StatesS: number;
  time128States: number;
  time128StatesL: number;
  time256StatesS: number;
  time256States: number;
  time256StatesL: number;
  time512StatesS: number;
  time512States: number;
  time512StatesL: number;
  time1024StatesS: number;
  time1024States: number;
  time1024StatesL: number;
  time2048StatesS: number;
  time2048States: number;
  time2048StatesL: number;
  time4096StatesS: number;
  time4096States: number;
  time4096StatesL: number;
  time8192StatesS: number;
  time8192States: number;
  time8192StatesL: number;
};


/**
 * Internally used type to provide table and chart data
 */
export type AdaptiveModulationDataType = {
  performanceData: AdaptiveModulationDatabaseDataType;
  radioSignalId: string;
  scannerId: string;
  timeStamp: string;
  suspectIntervalFlag: boolean;
  time2StatesS: number;
  time2States: number;
  time2StatesL: number;
  time4StatesS: number;
  time4States: number;
  time4StatesL: number;
  time16StatesS: number;
  time16States: number;
  time16StatesL: number;
  time32StatesS: number;
  time32States: number;
  time32StatesL: number;
  time64StatesS: number;
  time64States: number;
  time64StatesL: number;
  time128StatesS: number;
  time128States: number;
  time128StatesL: number;
  time256StatesS: number;
  time256States: number;
  time256StatesL: number;
  time512StatesS: number;
  time512States: number;
  time512StatesL: number;
  time1024StatesS: number;
  time1024States: number;
  time1024StatesL: number;
  time2048StatesS: number;
  time2048States: number;
  time2048StatesL: number;
  time4096StatesS: number;
  time4096States: number;
  time4096StatesL: number;
  time8192StatesS: number;
  time8192States: number;
  time8192StatesL: number;
} & { _id: string };