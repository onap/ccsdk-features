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
import { IActionHandler } from '../flux/action';
import { SetTitleAction } from '../actions/titleActions';

import { AddSnackbarNotification, RemoveSnackbarNotification } from '../actions/snackbarActions';
import { AddErrorInfoAction, RemoveErrorInfoAction, ClearErrorInfoAction } from '../actions/errorActions';
import { MenuAction, MenuClosedByUser } from '../actions/menuAction'
import { IconType } from '../models/iconDefinition';

import { ErrorInfo } from '../models/errorInfo';
import { SnackbarItem } from '../models/snackbarItem';
import { SetWebsocketAction } from '../actions/websocketAction';

export interface IApplicationState {
  title: string;
  appId?: string;
  icon?: IconType;
  isMenuOpen: boolean;
  isMenuClosedByUser: boolean;
  errors: ErrorInfo[];
  snackBars: SnackbarItem[];
  isWebsocketAvailable: boolean | undefined;
}

const applicationStateInit: IApplicationState = { title: "Loading ...", errors: [], snackBars: [], isMenuOpen: true, isMenuClosedByUser: false, isWebsocketAvailable: undefined };

export const applicationStateHandler: IActionHandler<IApplicationState> = (state = applicationStateInit, action) => {
  if (action instanceof SetTitleAction) {
    state = {
      ...state,
      title: action.title,
      icon: action.icon,
      appId: action.appId
    };
  } else if (action instanceof AddErrorInfoAction) {
    state = {
      ...state,
      errors: [
        ...state.errors,
        action.errorInfo
      ]
    };
  } else if (action instanceof RemoveErrorInfoAction) {
    const index = state.errors.indexOf(action.errorInfo);
    if (index > -1) {
      state = {
        ...state,
        errors: [
          ...state.errors.slice(0, index),
          ...state.errors.slice(index + 1)
        ]
      };
    }
  } else if (action instanceof ClearErrorInfoAction) {
    if (state.errors && state.errors.length) {
      state = {
        ...state,
        errors: []
      };
    }
  } else if (action instanceof AddSnackbarNotification) {
    state = {
      ...state,
      snackBars: [
        ...state.snackBars,
        action.notification
      ]
    };
  } else if (action instanceof RemoveSnackbarNotification) {
    state = {
      ...state,
      snackBars: state.snackBars.filter(s => s.key !== action.key)
    };
  } else if (action instanceof MenuAction) {
    state = {
      ...state,
      isMenuOpen: action.isOpen
    }
  } else if (action instanceof MenuClosedByUser) {
    state = {
      ...state,
      isMenuClosedByUser: action.isClosed
    }
  }
  else if (action instanceof SetWebsocketAction) {
    state = {
      ...state,
      isWebsocketAvailable: action.isConnected
    }
  }
  return state;
};
