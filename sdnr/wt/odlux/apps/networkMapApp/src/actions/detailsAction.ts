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

import { Action } from '../../../../framework/src/flux/action';
import { requestRest } from '../../../../framework/src/services/restService';


import { Site, Device } from "../model/site";
import { link } from '../model/link';
import { HistoryEntry } from "../model/historyEntry";
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { Dispatch } from '../../../../framework/src/flux/store';
import { SITEDOC_URL } from '../config';

export class SelectSiteAction extends Action {
  constructor(public site: Site){
    super()
  }
}

export class SelectLinkAction extends Action {
 constructor(public link: link){
   super();
 }
}

export class ClearDetailsAction extends Action{
  constructor(){
    super();
  }
}

export class AddToHistoryAction extends Action {
 constructor(public entry: HistoryEntry){
   super();
 }
}

export class ClearHistoryAction extends Action {
  constructor(){
    super();
  }
}

export class IsBusyCheckingDeviceListAction extends Action{
  constructor(public isBusy: boolean){
    super();
  }
}

export class FinishedLoadingDeviceListAction extends Action{
  constructor(public devices: Device[]){
    super();
  }
}

export class ClearLoadedDevicesAction extends Action{
  constructor(){
    super();
  }
}

export class InitializeLoadedDevicesAction extends Action{
  constructor(public devices: Device[]){
    super();
  }
}

export class IsSitedocReachableAction extends Action{
  constructor(public isReachable: boolean){
    super();
  }
}

let running=false;

export const UpdateDetailsView = (nodeId: string) =>(dispatcher: Dispatch, getState: () => IApplicationStoreState) =>{
  const {network:{details:{checkedDevices}}} = getState();
  if(checkedDevices!==null){
    const index = checkedDevices.findIndex(item=>item.name===nodeId)
    if(index!==-1)
     requestRest<any>("/rests/operational/network-topology:network-topology/topology/topology-netconf/node/"+nodeId, { method: "GET" })
     .then(result =>{
      if(result!==null){
        checkedDevices[index].status = result.node[0]["netconf-node-topology:connection-status"];

      }else{
        checkedDevices[index].status = "Not connected";
      }
      dispatcher(new FinishedLoadingDeviceListAction(checkedDevices));

     });
  }
}

export const CheckDeviceList = (list: Device[]) => async (dispatcher: Dispatch, getState: () => IApplicationStoreState) =>{
if(running) return;
running=true;
  dispatcher(new IsBusyCheckingDeviceListAction(true));

  const promises = list.map((device)=>{
    if(device.name){
      return requestRest<any>("/rests/data/network-topology:network-topology/topology=topology-netconf/node="+device.name, { method: "GET" })
    }else{
      return device;
    }

  })

  Promise.all(promises).then((result)=>{
    running=false;
    

    result.forEach((res: any, index)=>{
     if(res !==null && res["network-topology:node"]){
      list[index].status = res["network-topology:node"][0]["netconf-node-topology:connection-status"];
     }else{
      list[index].status = "Not connected";
     }
    });

    dispatcher(new FinishedLoadingDeviceListAction(list));
    dispatcher(new IsBusyCheckingDeviceListAction(false));

  })
  .catch(err=>{
    console.error(err);

  dispatcher(new IsBusyCheckingDeviceListAction(false));

  });
}

export const checkSitedockReachablity = () => async (dispatcher: Dispatch, getState: () => IApplicationStoreState) =>{
  console.log("searching for sitedoc server...")
  requestRest<any>(SITEDOC_URL+'/app/versioninfo').then(response =>{
    console.log(response);
    if(response){
     
        dispatcher(new IsSitedocReachableAction(true));
      
    }else{
      dispatcher(new IsSitedocReachableAction(false));
    }
  })
}