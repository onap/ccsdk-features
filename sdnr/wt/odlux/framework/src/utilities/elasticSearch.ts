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

import { DataCallback } from '../components/material-table';
import { Result, HitEntry } from '../models';

import { requestRest } from '../services/restService';

type propType = string | number | null | undefined | (string | number)[];
type dataType = { [prop: string]: propType };

export function createSearchDataHandler<TResult extends {} = dataType>(uri: (() => string) | string, additionalParameters?: {}): DataCallback<(TResult & { _id: string })>;
export function createSearchDataHandler<TResult extends {} = dataType, TData = dataType>(uri: (() => string) | string, additionalParameters: {} | null | undefined, mapResult: (res: HitEntry<TResult>, index: number, arr: HitEntry<TResult>[]) => (TData & { _id: string }), mapRequest?: (name?: string | null) => string): DataCallback<(TData & { _id: string })>
export function createSearchDataHandler<TResult, TData>(uri: (() => string) | string, additionalParameters?: {} | null | undefined, mapResult?: (res: HitEntry<TResult>, index: number, arr: HitEntry<TResult>[]) => (TData & { _id: string }), mapRequest?: (name?: string | null) => string): DataCallback<(TData & { _id: string })> {
  const fetchData: DataCallback<(TData & { _id: string })> = async (page, rowsPerPage, orderBy, order, filter) => {
    const url = `${ window.location.origin }/database/${typeof uri === "function" ? uri(): uri}/_search`;
    const from = rowsPerPage && page != null && !isNaN(+page)
      ? (+page) * rowsPerPage
      : null;

    const filterKeys = filter && Object.keys(filter) || [];

    const query = {
      ...filterKeys.length > 0 ? {
        query: {
          bool: {
            must: filterKeys.reduce((acc, cur) => {
              if (acc && filter && filter[cur]) {
                acc.push({ [filter[cur].indexOf("*") > -1 || filter[cur].indexOf("?") > -1 ? "wildcard" : "term"]: { [mapRequest ? mapRequest(cur) : cur]: filter[cur] } });
              }
              return acc;
            }, [] as any[])
          }
        }
      } : { "query": { "match_all": {} } },
      ...rowsPerPage ? { "size": rowsPerPage } : {},
      ...from ? { "from": from } : {},
      ...orderBy && order ? { "sort": [{ [mapRequest ? mapRequest(orderBy) : orderBy]: order }] } : {},
      ...additionalParameters ? additionalParameters : {}
    };
    const result = await requestRest<Result<TResult & { _id: string }>>(url, {
      method: "POST",       // *GET, POST, PUT, DELETE, etc.
      mode: "no-cors",      // no-cors, cors, *same-origin
      cache: "no-cache",    // *default, no-cache, reload, force-cache, only-if-cached
      headers: {
        "Content-Type": "application/json; charset=utf-8",
        // "Content-Type": "application/x-www-form-urlencoded",
      },
      body: JSON.stringify(query), // body data type must match "Content-Type" header
    });

    if (result) {
      let rows: (TData & { _id: string })[] = [];

      if (result && result.hits && result.hits.hits) {
        rows = result.hits.hits.map( mapResult ? mapResult :  h => (
          { ...(h._source as any as TData), _id: h._id }
        )) || []
      }

      const data = {
        page: Math.min(page || 0, result.hits.total || 0 / (rowsPerPage || 1)), rowCount: result.hits.total, rows: rows
      };
      return data;
    }

    return { page: 0, rowCount: 0, rows: [] };
  };

  return fetchData;
}

