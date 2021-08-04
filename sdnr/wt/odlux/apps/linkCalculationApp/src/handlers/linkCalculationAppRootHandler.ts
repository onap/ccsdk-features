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

// ** do not remove **
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { IActionHandler } from '../../../../framework/src/flux/action';;
import { UpdateLinkIdAction, UpdateFrequencyAction , UpdateLatLonAction, UpdateRainAttAction, UpdateRainValAction, updateHideForm, UpdateFslCalculation, UpdateSiteAction, UpdateDistanceAction, isCalculationServerReachableAction, UpdatePolAction, updateAltitudeAction, UpdateAbsorptionLossAction, UpdateWorstMonthRainAction, UpdateEIRPAction,  UpdateAntennaAction, UpdateAntennaListAction, UpdateAntennaGainAction, UpdateTxPowerAction, UpdateRxSensitivityAction} from '../actions/commonLinkCalculationActions';

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    linkCalculation: ICalculationsState;
  }
}

type ICalculationsState = {
  calculations:ILinkCalculationAppStateState
}

export type ILinkCalculationAppStateState= {
  linkId: string | null,
  frequency: number,
  formView:boolean,
  fsl:number,
  distance:number,
  Lat1: number,
  Lon1: number,
  Lat2: number, 
  Lon2: number,
  rainVal : number,
  rainAtt : number,
  siteA: string,
  siteB: string,
  reachable: boolean,
  polarization : string | null,
  amslA: number, 
  amslB:number, 
  aglA: number, 
  aglB:number,
  absorptionWater:number,
  absorptionOxygen: number,
  month: string, 
  eirpA: number, 
  eirpB: number, 
  antennaGainA: number,
  antennaGainB :number,
  antennaList:string[],
  antennaGainList:string[],
  antennaA: string,
  antennaB:string,
  systemOperatingMargin : number,
  txPowerA : string,
  txPowerB: string,
  rxSensitivityA : string,
  rxSensitivityB: string
}

const initialState: ILinkCalculationAppStateState ={
  linkId: null,
  frequency: 0,
  Lat1: 0,
  Lon1: 0,
  Lat2: 0,
  Lon2: 0,
  formView : false,
  fsl:0,
  distance:0,
  siteA : '',
  siteB: '',
  rainVal : 0,
  rainAtt: 0,
  reachable : true,
  polarization : 'Horizontal',
  amslA: 0, 
  amslB:0, 
  aglA: 0, 
  aglB:0,
  absorptionWater:0,
  absorptionOxygen: 0,
  month: '', 
  eirpA: 0, 
  eirpB: 0, 
  antennaGainA :0,
  antennaGainB :0,
  antennaList:[],
  antennaGainList:[],
  antennaA: '0',
  antennaB:'0',
  systemOperatingMargin : 0,
  txPowerA : '0',
  txPowerB: '0', 
  rxSensitivityA: '0',
  rxSensitivityB: '0'
}



export const LinkCalculationHandler: IActionHandler<ILinkCalculationAppStateState> = (state=initialState, action) => {
    
  if(action instanceof UpdateLinkIdAction){
      state = Object.assign({}, state, {linkId:action.linkId})
  } 
  else if(action instanceof updateHideForm){

    state = Object.assign({}, state, {formView:action.formView})
  }
  else if (action instanceof UpdateDistanceAction){
    state = Object.assign({}, state, {distance:action.distance})
  }
  else if (action instanceof UpdateFrequencyAction){
    state = Object.assign({}, state, {frequency:action.frequency})
  }
  else if (action instanceof UpdateFslCalculation){
  state = Object.assign({}, state, {fsl:action.fsl})
  }
  else if (action instanceof UpdateLatLonAction){
    state = Object.assign({}, state, {Lat1:action.Lat1, Lon1:action.Lon1, Lat2:action.Lat2, Lon2:action.Lon2})
  }
  else if (action instanceof UpdateRainAttAction){
    state = Object.assign({}, state, {rainAtt:action.rainAtt})
  }
  else if (action instanceof UpdateRainValAction){
    state = Object.assign({}, state, {rainVal:action.rainVal})
  }
  else if (action instanceof UpdateSiteAction){
    state = Object.assign({}, state, {siteA:action.siteA, siteB:action.siteB})
  }
  else if(action instanceof isCalculationServerReachableAction){
    state = Object.assign({}, state, { reachable: action.reachable });
  }
  else if (action instanceof UpdatePolAction){
    state = Object.assign({}, state, {polarization: action.polarization})
  }
  else if (action instanceof updateAltitudeAction){
    state = Object.assign({}, state, {amslA:action.amslA, amslB:action.amslA, aglA:action.aglA, aglB:action.aglB})
  }
  else if (action instanceof UpdateAbsorptionLossAction){
    state = Object.assign({}, state, {absorptionOxygen:action.absorptionOxygen, absorptionWater:action.absorptionWater})
  }
  else if (action instanceof UpdateWorstMonthRainAction){
    state = Object.assign({}, state, {month:action.month})
  }
  else if (action instanceof UpdateEIRPAction){
    state = Object.assign({}, state, {eirpA:action.eirpA, eirpB:action.eirpB})
  }
  else if (action instanceof UpdateAntennaGainAction){
    state = Object.assign({}, state, {antennaGainList:action.antennaGainList})
  }
  else if (action instanceof UpdateAntennaListAction){
    state = Object.assign({}, state, {antennaList:action.antennaList})
  }
  else if (action instanceof UpdateAntennaAction){
    state = Object.assign({}, state, {antennaA:action.antennaA == null ? state.antennaA : action.antennaA , antennaB: action.antennaB == null? state.antennaB : action.antennaB})
  }
  else if (action instanceof UpdateTxPowerAction){
    state = Object.assign({}, state, {txPowerA:action.txPowerA == null ? state.txPowerA : action.txPowerA , txPowerB: action.txPowerB == null? state.txPowerB : action.txPowerB})
  }
  else if (action instanceof UpdateRxSensitivityAction){
    state = Object.assign({}, state, {rxSensitivityA:action.rxSensitivityA == null ? state.rxSensitivityA : action.rxSensitivityA , rxSensitivityB: action.rxSensitivityB == null? state.rxSensitivityB : action.rxSensitivityB})
  }

  return state
}

const actionHandlers = {
  calculations: LinkCalculationHandler
}

export const LinkCalculationAppRootHandler = combineActionHandler<ICalculationsState>(actionHandlers); 
export default LinkCalculationAppRootHandler;

