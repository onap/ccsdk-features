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
 
import { Module, TopologyNode } from '../models/topologyNetconf';
import { connectService } from '../services/connectService';
 
/** 
  * Represents the base action. 
  */
export class BaseAction extends Action { }
 
/** 
  * Represents an action causing the store to load all element Yang capabilities.
  */
export class LoadAllElementInfoAction extends BaseAction { }
 
/** 
  * Represents an action causing the store to update element Yang capabilities. 
  */
export class AllElementInfoLoadedAction extends BaseAction {
  /**
    * Initialize this instance.
    * @param elementInfo The information of the element which is returned.
    */
  constructor(public elementInfo: TopologyNode | null, public error?: string) {
    super();
  }
}
 
/** 
  * Represents an action causing the store to update element Yang capabilities Module Features. 
  */
export class AllElementInfoFeatureLoadedAction extends BaseAction {
  /**
    * Initialize this instance.
    * @param elementFeatureInfo The information of the element which is returned.
    */
  constructor(public elementFeatureInfo: Module[] | null | undefined, public error?: string) {
    super();
  }
}
 
/** 
  * Represents an asynchronous thunk  action to load all yang capabilities. 
  */
export const loadAllInfoElementAsync = (nodeId: string) => (dispatch: Dispatch) => {
  dispatch(new LoadAllElementInfoAction());
  connectService.infoNetworkElement(nodeId).then(info => {
    dispatch(new AllElementInfoLoadedAction(info));
  }, error => {
    dispatch(new AllElementInfoLoadedAction(null, error));
  });
}; 
 
/** 
  * Represents an asynchronous thunk  action to load all yang features. 
  */
export const loadAllInfoElementFeaturesAsync = (nodeId: string) => (dispatch: Dispatch) => {
  dispatch(new LoadAllElementInfoAction());
  connectService.infoNetworkElementFeatures(nodeId).then(infoFeatures => {
    dispatch(new AllElementInfoFeatureLoadedAction(infoFeatures));
  }, error => {
    dispatch(new AllElementInfoFeatureLoadedAction(null, error));
  });
}; 