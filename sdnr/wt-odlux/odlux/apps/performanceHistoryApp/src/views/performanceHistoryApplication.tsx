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
import React from 'react';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { Theme } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';


import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { Dispatch } from '../../../../framework/src/flux/store';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { loadAllDeviceListAsync } from '../actions/deviceListActions';
import { loadDistinctLtpsbyNetworkElementAsync, ResetLtpsAction } from '../actions/ltpAction';
import { SetPanelAction } from '../actions/panelChangeActions';
import { TimeChangeAction } from '../actions/timeChangeAction';
import AdaptiveModulation from '../components/adaptiveModulation';
import CrossPolarDiscrimination from '../components/crossPolarDiscrimination';
import PerformanceData from '../components/performanceData';
import ReceiveLevel from '../components/receiveLevel';
import SignalToInterference from '../components/signalToInterference';
import Temperature from '../components/temperature';
import TransmissionPower from '../components/transmissionPower';
import { adaptiveModulationReloadAction, createAdaptiveModulationActions, createAdaptiveModulationPreActions } from '../handlers/adaptiveModulationHandler';
import { createCrossPolarDiscriminationActions, createCrossPolarDiscriminationPreActions, crossPolarDiscriminationReloadAction } from '../handlers/crossPolarDiscriminationHandler';
import { createPerformanceDataActions, createPerformanceDataPreActions, performanceDataReloadAction } from '../handlers/performanceDataHandler';
import { createReceiveLevelActions, createReceiveLevelPreActions, receiveLevelReloadAction } from '../handlers/receiveLevelHandler';
import { createSignalToInterferenceActions, createSignalToInterferencePreActions, signalToInterferenceReloadAction } from '../handlers/signalToInterferenceHandler';
import { createTemperatureActions, createTemperaturePreActions, temperatureReloadAction } from '../handlers/temperatureHandler';
import { createTransmissionPowerActions, createTransmissionPowerPreActions, transmissionPowerReloadAction } from '../handlers/transmissionPowerHandler';
import { PanelId } from '../models/panelId';
import { PmDataInterval } from '../models/performanceDataType';

import { AppBar, SelectChangeEvent, Tab, Tabs } from '@mui/material';
import { MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { ReloadAction } from '../actions/reloadAction';
import { ResetAllSubViewsAction } from '../actions/toggleActions';
import LtpSelection from '../components/ltpSelection';

const PerformanceHistoryComponentStyles = (theme: Theme) => createStyles({
  root: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  margin: {
    margin: theme.spacing(1),
  },
});

const mapProps = (state: IApplicationStoreState) => ({
  ...state.performanceHistory,
  activePanel: state.performanceHistory.currentOpenPanel,
  availableLtps: state.performanceHistory.ltps.distinctLtps,
  networkElements: state.performanceHistory.networkElements.deviceList,
  initialLoaded: state.performanceHistory.ltps.loadedOnce,
  error: state.performanceHistory.ltps.error,
  shouldReload: state.performanceHistory.isReloadSchedueled,
});

const mapDispatcher = (dispatcher: IDispatcher) => ({
  enableFilterPerformanceData: createPerformanceDataActions(dispatcher.dispatch),
  enableFilterReceiveLevel: createReceiveLevelActions(dispatcher.dispatch),
  enableFilterTransmissionPower: createTransmissionPowerActions(dispatcher.dispatch),
  enableFilterAdaptiveModulation: createAdaptiveModulationActions(dispatcher.dispatch),
  enableFilterTemperature: createTemperatureActions(dispatcher.dispatch),
  enableFilterSinr: createSignalToInterferenceActions(dispatcher.dispatch),
  enableFilterCpd: createCrossPolarDiscriminationActions(dispatcher.dispatch),
  reloadPerformanceData: () => dispatcher.dispatch(performanceDataReloadAction),
  reloadReceiveLevel: () => dispatcher.dispatch(receiveLevelReloadAction),
  reloadTransmissionPower: () => dispatcher.dispatch(transmissionPowerReloadAction),
  reloadAdaptiveModulation: () => dispatcher.dispatch(adaptiveModulationReloadAction),
  reloadTemperature: () => dispatcher.dispatch(temperatureReloadAction),
  reloadSignalToInterference: () => dispatcher.dispatch(signalToInterferenceReloadAction),
  reloadCrossPolarDiscrimination: () => dispatcher.dispatch(crossPolarDiscriminationReloadAction),
  performanceDataPreActions: createPerformanceDataPreActions(dispatcher.dispatch),
  receiveLevelPreActions: createReceiveLevelPreActions(dispatcher.dispatch),
  transmissionPowerPreActions: createTransmissionPowerPreActions(dispatcher.dispatch),
  adaptiveModulationPreActions: createAdaptiveModulationPreActions(dispatcher.dispatch),
  temperaturePreActions: createTemperaturePreActions(dispatcher.dispatch),
  signalToInterferencePreActions: createSignalToInterferencePreActions(dispatcher.dispatch),
  crossPolarDiscriminationPreActions: createCrossPolarDiscriminationPreActions(dispatcher.dispatch),
  getAllDevicesPMdata: async () => {
    await dispatcher.dispatch(loadAllDeviceListAsync);
  },
  getDistinctLtpsIds: (
    selectedNetworkElement: string, 
    selectedTimePeriod: string, 
    selectedLtp: string, 
    selectFirstLtp?: Function, 
    resetLTP?: Function,
  ) => dispatcher.dispatch(loadDistinctLtpsbyNetworkElementAsync(selectedNetworkElement, selectedTimePeriod, selectedLtp, selectFirstLtp, resetLTP)),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId)),
  timeIntervalChange: (time: PmDataInterval) => dispatcher.dispatch(new TimeChangeAction(time)),
  changeNode: (nodeId: string) => dispatcher.dispatch((dispatch: Dispatch) => {
    dispatch(new NavigateToApplication('performanceHistory', nodeId));
  }),
  resetLtps: () => dispatcher.dispatch((dispatch: Dispatch) => { dispatch(new ResetLtpsAction()); }),
  resetSubViews: () => dispatcher.dispatch(new ResetAllSubViewsAction()),
  setShouldReload: (show: boolean) => dispatcher.dispatch(new ReloadAction(show)),
});

export type NetworkElementType = {
  nodeId: string;
};

const NetworkElementTable = MaterialTable as MaterialTableCtorType<NetworkElementType>;

type PerformanceHistoryComponentProps = Connect<typeof mapProps, typeof mapDispatcher> & WithStyles<typeof PerformanceHistoryComponentStyles>;

type PerformanceHistoryComponentState = {
  selectedNetworkElement: string;
  selectedTimePeriod: string;
  selectedLtp: string;
  showNetworkElementsTable: boolean;
  showLtps: boolean;
  showPanels: boolean;
  preFilter:
  {
    'node-name': string;
    'uuid-interface': string;
  } | {};
};

/**
* Represents the component for Performance history application.
*/
class PerformanceHistoryComponent extends React.Component<PerformanceHistoryComponentProps, PerformanceHistoryComponentState> {
  /**
  * Initialises this instance
  */
  constructor(props: PerformanceHistoryComponentProps) {
    super(props);
    this.state = {
      selectedNetworkElement: props.nodeId !== '' ? props.nodeId : '-1',
      selectedTimePeriod: '15min',
      selectedLtp: '-1',
      showNetworkElementsTable: true,
      showLtps: false,
      showPanels: false,
      preFilter: {},
    };
  }

  onChangeTabs = (event: React.SyntheticEvent, newValue: PanelId) => {
    const nextActivePanel = newValue;
    this.changeTabs(nextActivePanel);
  };

  changeTabs = (nextActivePanel: PanelId) => {
    this.props.setCurrentPanel(nextActivePanel);
    const preFilter = this.state.preFilter;
    switch (nextActivePanel) {
      case 'PerformanceData':
        if (this.props.performanceData.preFilter !== {} && this.props.performanceData.preFilter === preFilter) {
          this.props.reloadPerformanceData();
        } else {
          this.props.performanceDataPreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'ReceiveLevel':
        if (this.props.receiveLevel.preFilter !== {} && this.props.receiveLevel.preFilter === preFilter) {
          this.props.reloadReceiveLevel();
        } else {
          this.props.receiveLevelPreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'TransmissionPower':
        if (this.props.transmissionPower.preFilter !== {} && this.props.transmissionPower.preFilter === preFilter) {
          this.props.reloadTransmissionPower();
        } else {
          this.props.transmissionPowerPreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'AdaptiveModulation':
        if (this.props.adaptiveModulation.preFilter !== {} && this.props.adaptiveModulation.preFilter === preFilter) {
          this.props.reloadAdaptiveModulation();
        } else {
          this.props.adaptiveModulationPreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'Temperature':
        if (this.props.temperature.preFilter !== {} && this.props.temperature.preFilter === preFilter) {
          this.props.reloadTemperature();
        } else {
          this.props.temperaturePreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'SINR':
        if (this.props.signalToInterference.preFilter !== {} && this.props.signalToInterference.preFilter === preFilter) {
          this.props.reloadSignalToInterference();
        } else {
          this.props.signalToInterferencePreActions.onPreFilterChanged(preFilter);
        }
        break;
      case 'CPD':
        if (this.props.crossPolarDiscrimination.preFilter !== {} && this.props.crossPolarDiscrimination.preFilter === preFilter) {
          this.props.reloadCrossPolarDiscrimination();
        } else {
          this.props.crossPolarDiscriminationPreActions.onPreFilterChanged(preFilter);
        }
        break;
      default:
        // do nothing if all panels are closed
        break;
    }
  };

  render(): JSX.Element {
    const { activePanel, nodeId } = this.props;
    if (nodeId === '') {
      return (
        <>
          <NetworkElementTable tableId="performance-data-element-selection-table" defaultSortColumn={'nodeId'} defaultSortOrder="asc" stickyHeader title={'Please select the network element!'} idProperty={'nodeId'} rows={this.props.networkElements} asynchronus
            onHandleClick={(event, rowData) => { this.handleNetworkElementSelect(rowData.nodeId); }} columns={
              [{ property: 'nodeId', title: 'Node Name' }]
            } />
        </>
      );
    } else {
      this.handleURLChange(nodeId);
      return (
        <>
          {this.state.showLtps &&

            <LtpSelection error={this.props.error} selectedNE={this.state.selectedNetworkElement} selectedLtp={this.state.selectedLtp} selectedTimePeriod={this.state.selectedTimePeriod}
              availableLtps={this.props.availableLtps} finishedLoading={this.props.initialLoaded} onChangeTimePeriod={this.handleTimePeriodChange}
              onChangeLtp={this.handleLtpChange}
            />
          }
          {this.state.showPanels &&
            <>

              <AppBar enableColorOnDark position="static" >
                <Tabs indicatorColor="secondary" textColor="inherit" value={activePanel} onChange={this.onChangeTabs} variant="scrollable" scrollButtons="auto" aria-label="performance-data-tabs">
                  <Tab label="Performance Data" value="PerformanceData" aria-label="performance-data" />
                  <Tab label="Receive Level" value="ReceiveLevel" aria-label="receive-level" />
                  <Tab label="Transmission Power" value="TransmissionPower" aria-label="transmission-power" />
                  <Tab label="Adaptive Modulation" value="AdaptiveModulation" aria-label="adaptive-modulation" />
                  <Tab label="Temperature" value="Temperature" aria-label="temperature"  />
                  <Tab label="Signal-to-interference-plus-noise ratio (SINR)" value="SINR" aria-label="sinr" />
                  <Tab label="Cross Polar Discrimination" value="CPD" aria-label="cross-polar-discrimination" />
                </Tabs>
              </AppBar>
              {
                activePanel === 'PerformanceData' &&
                <PerformanceData selectedTimePeriod={this.state.selectedTimePeriod} />
              }

              {
                activePanel === 'ReceiveLevel' &&
                <ReceiveLevel selectedTimePeriod={this.state.selectedTimePeriod} />
              }

              {
                activePanel === 'TransmissionPower' &&
                <TransmissionPower selectedTimePeriod={this.state.selectedTimePeriod} />
              }

              {
                activePanel === 'AdaptiveModulation' &&
                <AdaptiveModulation selectedTimePeriod={this.state.selectedTimePeriod} />
              }
              {
                activePanel === 'Temperature' &&
                <Temperature selectedTimePeriod={this.state.selectedTimePeriod} />
              }

              {
                activePanel === 'SINR' &&
                <SignalToInterference selectedTimePeriod={this.state.selectedTimePeriod} />
              }

              {
                activePanel === 'CPD' &&
                <CrossPolarDiscrimination selectedTimePeriod={this.state.selectedTimePeriod} />
              }
            </>
          }
        </>
      );
    }
  }


  public componentDidMount() {
    this.props.setCurrentPanel(null);
    this.props.getAllDevicesPMdata();
  }

  /**
  * Function which selects the first ltp returned from the database on selection of network element.
  */
  private selectFirstLtp = (firstLtp: string) => {
    this.setState({
      showPanels: true,
      selectedLtp: firstLtp,
    });
    this.preFilterChangeAndReload(this.state.selectedNetworkElement, this.state.selectedTimePeriod, firstLtp);
    this.changeTabs('PerformanceData');
  };

  /**
  * A function which reloads the visible table, if available, based on prefilters defined by network element and ltp on selected time period.
  */
  private preFilterChangeAndReload = (networkElement: string, timePeriod: string, ltp: string) => {
    const newPreFilter = {
      'node-name': networkElement,
      'uuid-interface': ltp,
    };

    const activePanel = this.props.activePanel;

    if (this.props.activePanel !== null) {
      // set prefilter and reload data if panel is open

      switch (activePanel) {
        case 'PerformanceData':
          this.props.performanceDataPreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'ReceiveLevel':
          this.props.receiveLevelPreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'TransmissionPower':
          this.props.transmissionPowerPreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'AdaptiveModulation':
          this.props.adaptiveModulationPreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'Temperature':
          this.props.temperaturePreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'SINR':
          this.props.signalToInterferencePreActions.onPreFilterChanged(newPreFilter);
          break;
        case 'CPD':
          this.props.crossPolarDiscriminationPreActions.onPreFilterChanged(newPreFilter);
          break;
        default:
          // do nothing if all panels are closed
          break;
      }
    }

    // set prefilter
    this.setState({ preFilter: newPreFilter });

  };

  /**
   * Function which handles network element changes.
   */
  private handleNetworkElementSelect = (selectedNetworkElement: string) => {

    this.setState({
      showLtps: true,
      selectedNetworkElement: selectedNetworkElement,
      showNetworkElementsTable: false,
      showPanels: false,
      selectedLtp: '-1',
    });

    this.props.resetSubViews();
    this.props.resetLtps();
    this.setState({ preFilter: {} });
    this.props.changeNode(selectedNetworkElement);
    this.props.getDistinctLtpsIds(selectedNetworkElement, this.state.selectedTimePeriod, '-1', this.selectFirstLtp);
  };

  private handleURLChange = (selectedNetworkElement: string) => {

    if (this.props.shouldReload) {

      this.setState({
        showLtps: true,
        selectedNetworkElement: selectedNetworkElement,
        showPanels: false,
        selectedLtp: '-1',
      });
      this.props.getDistinctLtpsIds(selectedNetworkElement, this.state.selectedTimePeriod, '-1', this.selectFirstLtp);
      this.props.setShouldReload(false);
    }
  };

  /**
    * Function which resets the ltps to "--select--" in the state if the passed parameter @ltpNotSelected is true.
    * @param ltpNotSelected: true, if existing selected is not available in the given time period, else false
    */
  private resetLtpDropdown = (ltpNotSelected: boolean) => {
    if (ltpNotSelected) {
      this.setState({
        selectedLtp: '-1',
        showPanels: false,
      });
    }
  };

  /**
  * Function which handles the time period changes.
  */
  private handleTimePeriodChange = (event: SelectChangeEvent<HTMLSelectElement>) => {

    const selectedTimeInterval = event.target.value === '15min'
      ? PmDataInterval.pmInterval15Min
      : PmDataInterval.pmInterval24Hours;

    this.setState({
      selectedTimePeriod: event.target.value as string,
    });

    this.props.timeIntervalChange(selectedTimeInterval);
    this.props.getDistinctLtpsIds(this.state.selectedNetworkElement, event.target.value as string, this.state.selectedLtp, undefined, this.resetLtpDropdown);
    this.preFilterChangeAndReload(this.state.selectedNetworkElement, event.target.value as string, this.state.selectedLtp);
  };

  /**
  * Function which handles the ltp changes.
  */
  private handleLtpChange = (event:SelectChangeEvent<HTMLSelectElement> ) => {

    if (event.target.value === '-1') {
      this.setState({
        showPanels: false,
        selectedLtp: event.target.value,
      });

    } else if (event.target.value !== this.state.selectedLtp) {
      this.setState({
        showPanels: true,
        selectedLtp: event.target.value as string,
      });
      this.preFilterChangeAndReload(this.state.selectedNetworkElement, this.state.selectedTimePeriod, event.target.value as string);

    }
  };
}

const PerformanceHistoryApplication = withStyles(PerformanceHistoryComponentStyles)(connect(mapProps, mapDispatcher)(PerformanceHistoryComponent));
export default PerformanceHistoryApplication;
