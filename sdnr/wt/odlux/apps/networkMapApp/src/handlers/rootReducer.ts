/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { combineActionHandler } from '../../../../framework/src/flux/middleware';

import { DetailsReducer, DetailsStoreState } from "./detailsReducer";
import { PopupsReducer, popupStoreState } from "./popupReducer";
import { MapReducer, mapState } from "./mapReducer";
import { SearchReducer, searchState } from "./searchReducer";
import { connectivityState, ConnectivityReducer } from './connectivityReducer';
import { SettingsReducer, SettingsState } from './settingsReducer';

export interface INetworkAppStoreState{
    details: DetailsStoreState,
    popup: popupStoreState,
    map: mapState,
    search: searchState,
    connectivity: connectivityState,
    settings: SettingsState
}

declare module '../../../../framework/src/store/applicationStore' {
    interface IApplicationStoreState {
      network: INetworkAppStoreState
    }
  }

const appHandler = {
    details: DetailsReducer, 
    popup: PopupsReducer, 
    map: MapReducer, 
    search: SearchReducer,
    connectivity: ConnectivityReducer,
    settings: SettingsReducer};

export const networkmapRootHandler = combineActionHandler<INetworkAppStoreState>(appHandler)

export default networkmapRootHandler;