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

//export { HitEntry, Result } from '../../../../framework/src/models';

/**
 * Represents performance data fields of the performance history table as used in the database
 */
export type PerformanceDatabaseDataType = {
  _id: string;
  es: number;
  ses: number;
  unavailability: number;
};

/**
 * Internally used type to provide table and chart data
 */
export type PerformanceDataType = {

  performanceData: PerformanceDatabaseDataType;
  radioSignalId: string;
  scannerId: string;
  timeStamp: string;
  suspectIntervalFlag: boolean;
  es: number;
  ses: number;
  unavailability: number;
} & { _id: string };


/**
 * Represents performance data time interval.
 */
export const enum PmDataInterval {
  pmInterval15Min,
  pmInterval24Hours,
}