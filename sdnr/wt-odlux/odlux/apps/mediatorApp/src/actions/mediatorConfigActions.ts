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

import { AddSnackbarNotification } from '../../../../framework/src/actions/snackbarActions';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import mediatorService from '../services/mediatorService';
import { MediatorConfig, MediatorConfigResponse } from '../models/mediatorServer';

/** Represents the base action. */
export class BaseAction extends Action { }

export class SetMediatorBusyByName extends BaseAction {
  constructor(public name: string, public isBusy: boolean) {
    super();
  }
}

export class AddMediatorConfig extends BaseAction {
  constructor(public mediatorConfig: MediatorConfigResponse) {
    super();
  }
}

export class UpdateMediatorConfig extends BaseAction {
  constructor(public name: string, public mediatorConfig: MediatorConfigResponse) {
    super();
  }
}

export class RemoveMediatorConfig extends BaseAction {
  constructor(public name: string) {
    super();
  }
}


export const startMediatorByNameAsyncActionCreator = (name: string) => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  dispatch(new SetMediatorBusyByName(name, true));
  const { mediator: { mediatorServerState: { id } } } = getState();
  if (id) {
    mediatorService.startMediatorByName(id, name).then(msg => {
      dispatch(new AddSnackbarNotification({ message: msg + ' ' + name, options: { variant: 'info' } }));
      // since there is no notification, a timeout will be need here
      window.setTimeout(() => {
        mediatorService.getMediatorServerConfigByName(id, name).then(config => {
          if (config) {
            dispatch(new UpdateMediatorConfig(name, config));
          } else {
            dispatch(new AddSnackbarNotification({ message: `Error: reading mediator config for ${name}.`, options: { variant: 'error' } }));
          }
          dispatch(new SetMediatorBusyByName(name, false));
        });
      }, 2100);
    });
  } else {
    dispatch(new AddSnackbarNotification({ message: `Error: currently no mediator server selected.`, options: { variant: 'error' } }));
    dispatch(new SetMediatorBusyByName(name, false));
  }
};

export const stopMediatorByNameAsyncActionCreator = (name: string) => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  dispatch(new SetMediatorBusyByName(name, true));
  const { mediator: { mediatorServerState: { id } } } = getState();
  if (id) {
    mediatorService.stopMediatorByName(id, name).then(msg => {
      dispatch(new AddSnackbarNotification({ message: msg + ' ' + name, options: { variant: 'info' } }));
      // since there is no notification, a timeout will be need here
      window.setTimeout(() => {
        mediatorService.getMediatorServerConfigByName(id, name).then(config => {
          if (config) {
            dispatch(new UpdateMediatorConfig(name, config));
          } else {
            dispatch(new AddSnackbarNotification({ message: `Error: reading mediator config for ${name}.`, options: { variant: 'error' } }));
          }
          dispatch(new SetMediatorBusyByName(name, false));
        });
      }, 2100);
    });
  } else {
    dispatch(new AddSnackbarNotification({ message: `Error: currently no mediator server selected.`, options: { variant: 'error' } }));
    dispatch(new SetMediatorBusyByName(name, false));
  }
};

export const addMediatorConfigAsyncActionCreator = (config: MediatorConfig) => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const { Name: name } = config;
  const { mediator: { mediatorServerState: { id } } } = getState();
  if (id) {
    mediatorService.createMediatorConfig(id, config).then(msg => {
      dispatch(new AddSnackbarNotification({ message: msg + ' ' + name, options: { variant: 'info' } }));
      // since there is no notification, a timeout will be need here
      window.setTimeout(() => {
        mediatorService.getMediatorServerConfigByName(id, name).then(config => {
          if (config) {
            dispatch(new AddMediatorConfig(config));
          } else {
            dispatch(new AddSnackbarNotification({ message: `Error: reading mediator config for ${name}.`, options: { variant: 'error' } }));
          }
        });
      }, 2100);
    });
  } else {
    dispatch(new AddSnackbarNotification({ message: `Error: currently no mediator server selected.`, options: { variant: 'error' } }));
  }
};

export const updateMediatorConfigAsyncActionCreator = (config: MediatorConfig) => (dispatch: Dispatch) => {
  // currently not supported be backend
};

export const removeMediatorConfigAsyncActionCreator = (config: MediatorConfig) => (dispatch: Dispatch, getState: () => IApplicationStoreState) => {
  const { Name: name } = config;
  const { mediator: { mediatorServerState: { id } } } = getState();
  if (id) {
    mediatorService.deleteMediatorConfigByName(id, name).then(msg => {
      dispatch(new AddSnackbarNotification({ message: msg + ' ' + name, options: { variant: 'info' } }));
      // since there is no notification, a timeout will be need here
      window.setTimeout(() => {
        mediatorService.getMediatorServerConfigByName(id, config.Name).then(config => {
          if (!config) {
            dispatch(new RemoveMediatorConfig(name));
          } else {
            dispatch(new AddSnackbarNotification({ message: `Error: deleting mediator config for ${name}.`, options: { variant: 'error' } }));
          }
        });
      }, 2100);
    });
  } else {
    dispatch(new AddSnackbarNotification({ message: `Error: currently no mediator server selected.`, options: { variant: 'error' } }));
    dispatch(new SetMediatorBusyByName(name, false));
  }
};



