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

import { TransmissionPowerDataType } from '../models/transmissionPowerDataType';
import { getFilter } from '../utils/tableUtils';

export interface ITransmissionPowerState extends IExternalTableState<TransmissionPowerDataType> { }

/**
 * Creates elastic search material data fetch handler for Transmission power from historicalperformance database.
 */
const transmissionPowerSearchHandler = createSearchDataHandler<TransmissionPowerDataType>(getFilter, false, null)

export const {
    actionHandler: transmissionPowerActionHandler,
    createActions: createTransmissionPowerActions,
    createProperties: createTransmissionPowerProperties,
    createPreActions: createTransmissionPowerPreActions,
    reloadAction: transmissionPowerReloadAction,
} = createExternal<TransmissionPowerDataType>(transmissionPowerSearchHandler, appState => appState.performanceHistory.transmissionPower);

