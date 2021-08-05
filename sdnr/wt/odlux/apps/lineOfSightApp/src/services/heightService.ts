/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { GPSProfileResult } from "../model/GPSProfileResult";
import { TERRAIN_URL } from "../config";
import { LatLon } from "../model/LatLon";

export const apiUrlBase="api/Query";

export const getGPSProfile = async (start: LatLon, end: LatLon) => {
  const url = `${TERRAIN_URL}/${apiUrlBase}/GPSProfileRecords`;

  const result = await fetch(url, {
    method: "POST",
    body: JSON.stringify({ start, end }),
    headers: {
      'Content-Type': 'application/json'
      // 'Content-Type': 'application/x-www-form-urlencoded',
    },
  });
  if (result.ok) {
    const data = await result.json() as GPSProfileResult[];
    return data;
  }

  return Number.NaN;
}

export const getGPSHeight = async (gpsCoord: LatLon) => {
  const url = `${TERRAIN_URL}/${apiUrlBase}/GPSHeight`;

  const result = await fetch(url, {
    method: "POST",
    body: JSON.stringify(gpsCoord),
    headers: {
      'Content-Type': 'application/json'
      // 'Content-Type': 'application/x-www-form-urlencoded',
    },
  });
  if (result.ok) {
    const data = await result.json() as { height: number };
    return data.height;
  }else{
    return undefined;
  }
}

