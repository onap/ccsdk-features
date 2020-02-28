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

// ** do not remove **
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { IActionHandler } from '../../../../framework/src/flux/action';

import { IConnectAppStoreState } from '../../../connectApp/src/handlers/connectAppRootHandler';
import { IPerformanceDataState, performanceDataActionHandler } from './performanceDataHandler';
import { IReceiveLevelState, receiveLevelActionHandler } from './receiveLevelHandler';
import { ITransmissionPowerState, transmissionPowerActionHandler } from './transmissionPowerHandler';
import { IAdaptiveModulationState, adaptiveModulationActionHandler } from './adaptiveModulationHandler';
import { ITemperatureState, temperatureActionHandler } from './temperatureHandler';
import { ISignalToInterferenceState, signalToInterferenceActionHandler } from './signalToInterferenceHandler';
import { ICrossPolarDiscriminationState, crossPolarDiscriminationActionHandler } from './crossPolarDiscriminationHandler';
import { SetPanelAction } from '../actions/panelChangeActions';
import { IDeviceListState, deviceListActionHandler } from './deviceListActionHandler';
import { IAvailableLtpsState, availableLtpsActionHandler } from './availableLtpsActionHandler';
import { PmDataInterval } from '../models/performanceDataType';
import { TimeChangeAction } from '../actions/timeChangeAction';
import { UpdateMountId } from '../actions/deviceListActions';
import { SetSubViewAction, ResetAllSubViewsAction, SetFilterVisibility } from '../actions/toggleActions';
import { SubTabType, currentViewType } from '../models/toggleDataType';

export interface IPerformanceHistoryStoreState {
  nodeId: string;
  networkElements: IDeviceListState;
  ltps: IAvailableLtpsState;
  performanceData: IPerformanceDataState;
  receiveLevel: IReceiveLevelState;
  transmissionPower: ITransmissionPowerState;
  adaptiveModulation: IAdaptiveModulationState;
  temperature: ITemperatureState;
  signalToInterference: ISignalToInterferenceState;
  crossPolarDiscrimination: ICrossPolarDiscriminationState;
  currentOpenPanel: string | null;
  pmDataIntervalType: PmDataInterval;
  subViews: toggleViewDataType;
}

const mountIdHandler: IActionHandler<string> = (state = "", action) => {
  if (action instanceof UpdateMountId) {
    state = "";
    if (action.nodeId) {
      state = action.nodeId;
    }
  }
  return state;
}


const currentOpenPanelHandler: IActionHandler<string | null> = (state = null, action) => {
  if (action instanceof SetPanelAction) {
    state = action.panelId;
  }
  return state;
}

const currentPMDataIntervalHandler: IActionHandler<PmDataInterval> = (state = PmDataInterval.pmInterval15Min, action) => {
  if (action instanceof TimeChangeAction) {
    state = action.time;
  }
  return state;
}

type filterableSubview = { subView: SubTabType, isFilterVisible: boolean };
type toggleViewDataType = { currentSubView: currentViewType, performanceData: filterableSubview, receiveLevel: filterableSubview, transmissionPower: filterableSubview, adaptiveModulation: filterableSubview, temperatur: filterableSubview, SINR: filterableSubview, CPD: filterableSubview };


const toogleViewDataHandler: IActionHandler<toggleViewDataType> = (state = { currentSubView: "performanceData", performanceData: { subView: "chart", isFilterVisible: true }, receiveLevel: { subView: "chart", isFilterVisible: true }, adaptiveModulation: { subView: "chart", isFilterVisible: true }, transmissionPower: { subView: "chart", isFilterVisible: true }, temperatur: { subView: "chart", isFilterVisible: true }, SINR: { subView: "chart", isFilterVisible: true }, CPD: { subView: "chart", isFilterVisible: true } }, action) => {

  if (action instanceof SetSubViewAction) {
    switch (action.currentView) {
      case "performanceData": state = { ...state, performanceData: { ...state.performanceData, subView: action.selectedTab } }; break;
      case "adaptiveModulation": state = { ...state, adaptiveModulation: { ...state.adaptiveModulation, subView: action.selectedTab } }; break;
      case "receiveLevel": state = { ...state, receiveLevel: { ...state.receiveLevel, subView: action.selectedTab } }; break;
      case "transmissionPower": state = { ...state, transmissionPower: { ...state.transmissionPower, subView: action.selectedTab } }; break;
      case "Temp": state = { ...state, temperatur: { ...state.temperatur, subView: action.selectedTab } }; break;
      case "SINR": state = { ...state, SINR: { ...state.SINR, subView: action.selectedTab } }; break;
      case "CPD": state = { ...state, CPD: { ...state.CPD, subView: action.selectedTab } }; break;
    }
  }
  else if (action instanceof SetFilterVisibility) {
    switch (action.currentView) {
      case "performanceData": state = {
        ...state, performanceData: { ...state.performanceData, isFilterVisible: action.isVisible }
      }; break;
      case "adaptiveModulation": state = { ...state, adaptiveModulation: { ...state.performanceData, isFilterVisible: action.isVisible } }; break;
      case "receiveLevel": state = { ...state, receiveLevel: { ...state.receiveLevel, isFilterVisible: action.isVisible } }; break;
      case "transmissionPower": state = { ...state, transmissionPower: { ...state.transmissionPower, isFilterVisible: action.isVisible } }; break;
      case "Temp": state = { ...state, temperatur: { ...state.temperatur, isFilterVisible: action.isVisible } }; break;
      case "SINR": state = { ...state, SINR: { ...state.SINR, isFilterVisible: action.isVisible } }; break;
      case "CPD": state = { ...state, CPD: { ...state.CPD, isFilterVisible: action.isVisible } }; break;
    }

  } else if (action instanceof ResetAllSubViewsAction) {
    state = {
      ...state, performanceData: { ...state.performanceData, subView: "chart" },
      adaptiveModulation: { ...state.adaptiveModulation, subView: "chart" },
      receiveLevel: { ...state.receiveLevel, subView: "chart" },
      transmissionPower: { ...state.transmissionPower, subView: "chart" },
      temperatur: { ...state.temperatur, subView: "chart" },
      SINR: { ...state.SINR, subView: "chart" },
      CPD: { ...state.CPD, subView: "chart" }
    }
  }

  return state;
}

declare module '../../../../framework/src/store/applicationStore' {
  interface IApplicationStoreState {
    performanceHistory: IPerformanceHistoryStoreState;
    connect: IConnectAppStoreState;
  }
}

const actionHandlers = {
  nodeId: mountIdHandler,
  networkElements: deviceListActionHandler,
  ltps: availableLtpsActionHandler,
  performanceData: performanceDataActionHandler,
  receiveLevel: receiveLevelActionHandler,
  transmissionPower: transmissionPowerActionHandler,
  adaptiveModulation: adaptiveModulationActionHandler,
  temperature: temperatureActionHandler,
  signalToInterference: signalToInterferenceActionHandler,
  crossPolarDiscrimination: crossPolarDiscriminationActionHandler,
  currentOpenPanel: currentOpenPanelHandler,
  pmDataIntervalType: currentPMDataIntervalHandler,
  subViews: toogleViewDataHandler
};

const performanceHistoryRootHandler = combineActionHandler<IPerformanceHistoryStoreState>(actionHandlers);
export default performanceHistoryRootHandler;

