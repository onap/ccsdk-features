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
export type Fault = {
  id: string;
  nodeId: string;
  counter: number;
  timestamp: string;
  objectId: string;
  problem: string;
  severity: null | 'Warning' | 'Minor' | 'Major' | 'Critical' | 'NonAlarmed';
  type: string;
  sourceType?: string;
};

export type FaultAlarmNotification = {
  id: string;
  timeStamp: string;
  nodeName: string;
  counter: number;
  objectId: string;
  problem: string;
  severity: string;
};

export type FaultAlarmNotificationWS = {
  'node-id': string;
  'data': {
    'counter': number;
    'time-stamp': string;
    'object-id-ref': string;
    'problem': string;
    'severity': null | 'Warning' | 'Minor' | 'Major' | 'Critical' | 'NonAlarmed';
  };
  'type': {
    'namespace': string;
    'revision': string;
    'type': string;
  };
  'event-time': string;
};

/**
 * Fault status return type
 */
export type FaultsReturnType = {
  criticals: number;
  majors: number;
  minors: number;
  warnings: number;
  Connected: number;
  Connecting: number;
  Disconnected: number;
  Mounted: number;
  UnableToConnect: number;
  Undefined: number;
  Unmounted: number;
  total: number;
};

export type FaultType = {
  Critical: number;
  Major: number;
  Minor: number;
  Warning: number;
  Connected: number;
  Connecting: number;
  Disconnected: number;
  Mounted: number;
  UnableToConnect: number;
  Undefined: number;
  Unmounted: number;
  total: number;
};

export type Faults = {
  faults: FaultsReturnType;
  'network-element-connections': FaultsReturnType;
};

export type DeletedStuckAlarms = {
  nodenames: string[];
};