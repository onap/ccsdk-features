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
