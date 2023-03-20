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
import { ColumnModel, ColumnType } from '../../../../framework/src/components/material-table';

import { PmDataInterval } from '../models/performanceDataType';
import { getPmDataInterval } from '../pluginPerformance';

export const addColumnLabels = <T>(name: string, title: string, disableFilter = true, disableSorting = true): ColumnModel<T> => {
  return { property: name as keyof T, title: title, type: ColumnType.text, disableFilter: disableFilter, disableSorting: disableSorting };
};

export function getFilter(): string {
  switch (getPmDataInterval()) {
    case PmDataInterval.pmInterval15Min:
      return 'pmdata-15m';
    case PmDataInterval.pmInterval24Hours:
      return 'pmdata-24h';
    default:
      throw new Error('Unknown time intervall');
  }
}