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
// main state handler

import { combineActionHandler } from '../../../../framework/src/flux/middleware';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { IAvaliableMediatorServersState, avaliableMediatorServersActionHandler } from './avaliableMediatorServersHandler' ;
import { MediatorServerState, mediatorServerHandler } from './mediatorServerHandler';

export interface IMediatorAppStoreState {
  avaliableMediatorServers: IAvaliableMediatorServersState,
  mediatorServerState: MediatorServerState,
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    mediator: IMediatorAppStoreState
  }
}

const actionHandlers = {
  avaliableMediatorServers: avaliableMediatorServersActionHandler,
  mediatorServerState: mediatorServerHandler,
};

export const mediatorAppRootHandler = combineActionHandler<IMediatorAppStoreState>(actionHandlers);
export default mediatorAppRootHandler;
