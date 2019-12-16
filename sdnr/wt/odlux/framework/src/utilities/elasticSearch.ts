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

import { Result } from '../models';
import { DataCallback } from '../components/material-table';

import { requestRest } from '../services/restService';

import { convertPropertyNames, convertPropertyValues, replaceUpperCase, replaceHyphen } from './yangHelper';

type propType = string | number | null | undefined | (string | number)[];
type dataType = { [prop: string]: propType };

/** Represents a fabric for the searchDataHandler used by the internal data api.
 *  @param typeName The name of the entry type to create a searchDataHandler for.
 *  @param additionalFilters Filterproperties and their values to add permanently.
 *  @returns The searchDataHandler callback to be used with the material table.
*/
export function createSearchDataHandler<TResult>(typeName: (() => string) | string, additionalFilters?: {} | null | undefined): DataCallback<(TResult)> {
  const fetchData: DataCallback<(TResult)> = async (pageIndex, rowsPerPage, orderBy, order, filter) => {
    const url = `/restconf/operations/data-provider:read-${typeof typeName === "function" ? typeName(): typeName}-list`;

    filter = { ...filter, ...additionalFilters };

    const filterKeys = filter && Object.keys(filter) || [];

    const query = {
      input: {
        filter: filterKeys.filter(f => filter![f] != null && filter![f] !== "").map(property => ({ property, filtervalue: filter![property]})),
        sortorder: orderBy ? [{ property: orderBy, sortorder: order === "desc" ? "descending" : "ascending" }] : [],
        pagination: { size: rowsPerPage, page: (pageIndex != null && pageIndex > 0 && pageIndex || 0) +1 }
      }
    };
    const result = await requestRest<Result<TResult>>(url, {
      method: "POST",       // *GET, POST, PUT, DELETE, etc.
      mode: "same-origin",  // no-cors, cors, *same-origin
      cache: "no-cache",    // *default, no-cache, reload, force-cache, only-if-cached
      headers: {
        "Content-Type": "application/json",
        // "Content-Type": "application/x-www-form-urlencoded",
      },
      body: JSON.stringify(convertPropertyValues(query, replaceUpperCase)), // body data type must match "Content-Type" header
    });

    if (result) {
      let rows: TResult[] = [];

      if (result && result.output && result.output.data) {
        rows = result.output.data.map(obj => convertPropertyNames(obj, replaceHyphen)) || []
      }

      const data = {
        page: result.output.pagination && result.output.pagination.page != null && result.output.pagination.page - 1  || 0 , total: result.output.pagination && result.output.pagination.total || 0, rows: rows
      };
      return data;
    }

    return { page: 1, total: 0, rows: [] };
  };

  return fetchData;
}

