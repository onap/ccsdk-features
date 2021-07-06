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

import { requestRest } from "../../../../framework/src/services/restService";
import { Result } from "../../../../framework/src/models/elasticSearch";
import { ConnectionStatusCountType, ConnectionStatusCount } from "../models/connectionStatusCount";



export const getConnectionStatusCountStateFromDatabase = async (): Promise<ConnectionStatusCountType | null> => {
  const path = 'rests/operations/data-provider:read-status';
  const result = await requestRest<Result<ConnectionStatusCount>>(path, { method: "POST" });
  let connectionStatusCountType: ConnectionStatusCountType = {
    Connected: 0,
    Connecting: 0,
    Disconnected: 0,
    Mounted: 0,
    UnableToConnect: 0,
    Undefined: 0,
    Unmounted: 0,
    total: 0
  }
  let connectionStatusCount: ConnectionStatusCount[] | null = null;

  if (result && result["data-provider:output"] && result["data-provider:output"].data) {
    connectionStatusCount = result["data-provider:output"].data;
    connectionStatusCountType = {
      Connected: connectionStatusCount[0]["network-element-connections"].Connected,
      Connecting: connectionStatusCount[0]["network-element-connections"].Connecting,
      Disconnected: connectionStatusCount[0]["network-element-connections"].Disconnected,
      Mounted: connectionStatusCount[0]["network-element-connections"].Mounted,
      UnableToConnect: connectionStatusCount[0]["network-element-connections"].UnableToConnect,
      Undefined: connectionStatusCount[0]["network-element-connections"].Undefined,
      Unmounted: connectionStatusCount[0]["network-element-connections"].Unmounted,
      total: connectionStatusCount[0]["network-element-connections"].total,
    }
  }
  return connectionStatusCountType;
}
