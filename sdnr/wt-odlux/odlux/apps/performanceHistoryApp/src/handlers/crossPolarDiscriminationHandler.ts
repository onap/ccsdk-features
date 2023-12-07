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
import { createExternal, IExternalTableState } from '../../../../framework/src/components/material-table/utilities';
import { createSearchDataHandler } from '../../../../framework/src/utilities/elasticSearch';

import { CrossPolarDiscriminationDataType } from '../models/crossPolarDiscriminationDataType';
import { getFilter } from '../utils/tableUtils';

export interface ICrossPolarDiscriminationState extends IExternalTableState<CrossPolarDiscriminationDataType> { }

/**
 * Creates elastic search material data fetch handler for CPD from historicalperformance database.
 */
const crossPolarDiscriminationSearchHandler = createSearchDataHandler<CrossPolarDiscriminationDataType>(getFilter, false, null);

export const {
  actionHandler: crossPolarDiscriminationActionHandler,
  createActions: createCrossPolarDiscriminationActions,
  createProperties: createCrossPolarDiscriminationProperties,
  createPreActions: createCrossPolarDiscriminationPreActions,
  reloadAction: crossPolarDiscriminationReloadAction,
} = createExternal<CrossPolarDiscriminationDataType>(crossPolarDiscriminationSearchHandler, appState => appState.performanceHistory.crossPolarDiscrimination);

