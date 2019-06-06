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