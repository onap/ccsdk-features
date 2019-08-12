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

export const getFaultStateFromDatabase = async (): Promise < { [key: string]: number } | null > => {
  const path = 'database/sdnevents/faultcurrent/_search';
  const query = {
    "size": 0,
    "aggregations": {
      "severity": {
        "terms": {
          "field": "faultCurrent.severity"
        }
      }
    }
  };

  const result = await requestRest<Result<{ severity: { buckets: { key: string, doc_count: number }[] } }>>(path, { method: "POST", body: JSON.stringify(query) });
  return result && result.aggregations && result.aggregations["severity"].buckets.reduce<{ [key: string]: number }>((acc, cur) => {
    acc[cur.key] = cur.doc_count;
    return acc;
  }, {}) || null;
}