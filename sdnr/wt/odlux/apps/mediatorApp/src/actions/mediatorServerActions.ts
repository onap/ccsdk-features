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
import { Action } from '../../../../framework/src/flux/action';
import { Dispatch } from '../../../../framework/src/flux/store';

import { MediatorServerVersionInfo, MediatorConfig, MediatorConfigResponse, MediatorServerDevice } from '../models/mediatorServer';
import mediatorService from '../services/mediatorService';
import { AddSnackbarNotification } from '../../../../framework/src/actions/snackbarActions';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

/** Represents the base action. */
export class BaseAction extends Action { }

export class SetMediatorServerBusy extends BaseAction {
  constructor(public isBusy: boolean) {
    super();
  }
}

export class SetMediatorServerInfo extends BaseAction {
  /**
   * Initializes a new instance of this class.
   */
  constructor(public id: string | null, public name: string | null, public url: string | null) {
    super();

  }
}

export class SetMediatorServerVersion extends BaseAction {
  /**
   * Initializes a new instance of this class.
   */
  constructor(public versionInfo: MediatorServerVersionInfo | null) {
    super();

  }
}

export class SetAllMediatorServerConfigurations extends BaseAction {
  /**
   * Initializes a new instance of this class.
   */
  constructor(public allConfigurations: MediatorConfigResponse[] | null) {
    super();

  }
}

export class SetMediatorServerSupportedDevices extends BaseAction {
  /**
   * Initializes a new instance of this class.
   */
  constructor(public devices: MediatorServerDevice[] | null) {
    super();

  }
}

export class SetMediatorServerReachable extends BaseAction {
  constructor(public isReachable: boolean) {
    super();
  }
}

export const initializeMediatorServerAsyncActionCreator = (serverId: string) => (dispatch: Dispatch) => {
  dispatch(new SetMediatorServerBusy(true));
  mediatorService.getMediatorServerById(serverId).then(mediatorServer => {
    if (!mediatorServer) {
      dispatch(new SetMediatorServerBusy(false));
      dispatch(new AddSnackbarNotification({ message: `Error loading mediator server [${serverId}]`, options: { variant: 'error' } }));
      dispatch(new NavigateToApplication("mediator"));
      return;
    }

    dispatch(new SetMediatorServerInfo(mediatorServer.id, mediatorServer.name, mediatorServer.url));

    Promise.all([
      mediatorService.getMediatorServerAllConfigs(mediatorServer.id),
      mediatorService.getMediatorServerSupportedDevices(mediatorServer.id),
      mediatorService.getMediatorServerVersion(mediatorServer.id)
    ]).then(([configurations, supportedDevices, versionInfo]) => {
      if (configurations === null && supportedDevices === null && versionInfo === null) {
        dispatch(new SetMediatorServerReachable(false));
      } else {
        dispatch(new SetMediatorServerReachable(true));
      }
      dispatch(new SetAllMediatorServerConfigurations(configurations));
      dispatch(new SetMediatorServerSupportedDevices(supportedDevices));
      dispatch(new SetMediatorServerVersion(versionInfo));
      dispatch(new SetMediatorServerBusy(false));
    }).catch(error => {
      dispatch(new SetMediatorServerReachable(false));
      dispatch(new SetMediatorServerBusy(false));
    });
  });
};

