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
import { requestRest } from '../../../../framework/src/services/restService';
import { Result } from '../../../../framework/src/models/elasticSearch';

import { DistinctLtp, LtpIds } from '../models/availableLtps';

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
      "size": 0,
      "query": {
        "match": {
          "node-name": networkElement
        }
      },
      "aggs": {
        "uuid-interface": {
          "terms": {
            "field": "uuid-interface"
          }
        }
      }
    };

    if (selectedTimePeriod === "15min") {
      path = 'database/sdnperformance/historicalperformance15min/_search';
    } else {
      path = 'database/sdnperformance/historicalperformance24h/_search';
    }

    const result = await requestRest<Result<DistinctLtp>>(path, { method: "POST", body: JSON.stringify(query) });
    return result && result.aggregations && result.aggregations["uuid-interface"].buckets.map(ne => ({
      key: ne.key
    })) || null;
  }
}

export const PerformanceHistoryService = new PerformanceService();
export default PerformanceHistoryService;
