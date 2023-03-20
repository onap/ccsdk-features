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
import { Result } from '../../../../framework/src/models/elasticSearch';
import { requestRest } from '../../../../framework/src/services/restService';

import { convertPropertyNames, replaceUpperCase } from '../../../../framework/src/utilities/yangHelper';
import { LtpIds } from '../models/availableLtps';
import { DeviceListType } from '../models/deviceListType';

/** 
 * Represents a web api accessor service for Network elements actions.
 */
class PerformanceService {

  /**
   * Get distinct ltps based on the selected network element and time period from the historicalperformance15min database table.
   */
  public async getDistinctLtpsFromDatabase(networkElement: string, selectedTimePeriod: string): Promise<LtpIds[] | null> {
    let path;
    const query = {
      'filter': [{
        'property': 'node-name',
        'filtervalue': networkElement,
      }],
      'sortorder': [],
      'pagination': {
        'size': 20,
        'page': 1,
      },
    };


    if (selectedTimePeriod === '15min') {
      path = '/rests/operations/data-provider:read-pmdata-15m-ltp-list';
    } else {
      path = '/rests/operations/data-provider:read-pmdata-24h-ltp-list';
    }

    const result = await requestRest<Result<string>>(path, { method: 'POST', body: JSON.stringify(convertPropertyNames({ input: query }, replaceUpperCase)) });
    return result && result['data-provider:output'] && result['data-provider:output'].data && result['data-provider:output'].data.map(ne => ({ key: ne })) || null;
  }



  /**
  * Gets all devices from the performanceHistory 15min backend.
  */
  public async getDeviceListfromPerf15minHistory(): Promise<(DeviceListType)[] | null> {
    const path = '/rests/operations/data-provider:read-pmdata-15m-device-list';
    const query = {
      'data-provider:input': {
        'filter': [],
        'sortorder': [],
        'pagination': {
          'size': 20,
          'page': 1,
        },
      },
    };

    const result = await requestRest<Result<string>>(path, { method: 'POST', body: JSON.stringify(query) });
    return result && result['data-provider:output'] && result['data-provider:output'].data && result['data-provider:output'].data.map(ne => ({
      nodeId: ne,
    })) || null;
  }

  /**
   * Gets all devices from the performanceHistory 24h backend.
   */
  public async getDeviceListfromPerf24hHistory(): Promise<(DeviceListType)[] | null> {
    const path = '/rests/operations/data-provider:read-pmdata-24h-device-list';
    const query = {
      'data-provider:input': {
        'filter': [],
        'sortorder': [],
        'pagination': {
          'size': 20,
          'page': 1,
        },
      },
    };

    const result = await requestRest<Result<string>>(path, { method: 'POST', body: JSON.stringify(query) });
    return result && result['data-provider:output'] && result['data-provider:output'].data && result['data-provider:output'].data.map(ne => ({
      nodeId: ne,
    })) || null;
  }
}


export const PerformanceHistoryService = new PerformanceService();
export default PerformanceHistoryService;
