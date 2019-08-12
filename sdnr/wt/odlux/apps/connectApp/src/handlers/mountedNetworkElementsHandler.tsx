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
import { IActionHandler } from '../../../../framework/src/flux/action';

import {
  AddOrUpdateMountedNetworkElement,
  AllMountedNetworkElementsLoadedAction,
  LoadAllMountedNetworkElementsAction,
  RemoveMountedNetworkElement,
  UpdateConnectionStateMountedNetworkElement,
  UpdateRequiredMountedNetworkElement
} from '../actions/mountedNetworkElementsActions';

import { MountedNetworkElementType } from '../models/mountedNetworkElements';

export interface IMountedNetworkElementsState {
  elements: MountedNetworkElementType[];
  busy: boolean;
}

const mountedNetworkElementsStateInit: IMountedNetworkElementsState = {
  elements: [],
  busy: false
};

export const mountedNetworkElementsActionHandler: IActionHandler<IMountedNetworkElementsState> = (state = mountedNetworkElementsStateInit, action) => {
  if (action instanceof LoadAllMountedNetworkElementsAction) {

    state = {
      ...state,
      busy: true
    };

  } else if (action instanceof AllMountedNetworkElementsLoadedAction) {
    if (!action.error && action.mountedNetworkElements) {
      state = {
        ...state,
        elements: action.mountedNetworkElements,
        busy: false
      };
    } else {
      state = {
        ...state,
        busy: false
      };
    }
  } else if (action instanceof AddOrUpdateMountedNetworkElement) {
    if (!action.mountedNetworkElement) return state; // should handle error here
    const index = state.elements.findIndex(el => el.mountId === (action.mountedNetworkElement && action.mountedNetworkElement.mountId));
    if (index > -1) {
      state = {
        ...state,
        elements: [
          ...state.elements.slice(0, index),
          action.mountedNetworkElement,
          ...state.elements.slice(index + 1)
        ]
      }
    } else {
      state = {
        ...state,
        elements: [...state.elements, action.mountedNetworkElement],
      }
    };
  } else if (action instanceof RemoveMountedNetworkElement) {
    state = {
      ...state,
      elements: state.elements.filter(e => e.mountId !== action.mountId),
    };
  } else if (action instanceof UpdateConnectionStateMountedNetworkElement) {
    const index = state.elements.findIndex(el => el.mountId === action.mountId);
    if (index > -1) {
      state = {
        ...state,
        elements: [
          ...state.elements.slice(0, index),
          { ...state.elements[index], connectionStatus: action.mountId },
          ...state.elements.slice(index + 1)
        ]
      }
    }
  } else if (action instanceof UpdateRequiredMountedNetworkElement) {
    const index = state.elements.findIndex(el => el.mountId === action.mountId);
    if (index > -1 && (state.elements[index].required !== action.required)) {
      state = {
        ...state,
        elements: [
          ...state.elements.slice(0, index),
          { ...state.elements[index], required: action.required },
          ...state.elements.slice(index + 1)
        ]
      }
    }
  };
  return state;
};