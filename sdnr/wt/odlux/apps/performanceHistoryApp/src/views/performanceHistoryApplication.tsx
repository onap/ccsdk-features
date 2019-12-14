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
import * as React from 'react';

import { createStyles, Theme, withStyles, WithStyles } from '@material-ui/core/styles';
import FormControl from '@material-ui/core/FormControl';
import MenuItem from '@material-ui/core/MenuItem';
import Select from '@material-ui/core/Select';

import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { Panel } from '../../../../framework/src/components/material-ui';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';
import { Dispatch } from '../../../../framework/src/flux/store';

import { PanelId } from '../models/panelId';
import { PmDataInterval } from '../models/performanceDataType';
import PerformanceData from '../components/performanceData';
import ReceiveLevel from '../components/receiveLevel';
import TransmissionPower from '../components/transmissionPower';
import AdaptiveModulation from '../components/adaptiveModulation';
import Temperature from '../components/temperature';
import SignalToInterference from '../components/signalToInterference';
import CrossPolarDiscrimination from '../components/crossPolarDiscrimination';
import { loadAllDeviceListAsync } from '../actions/deviceListActions';
import { TimeChangeAction } from '../actions/timeChangeAction';
import { loadDistinctLtpsbyNetworkElementAsync } from '../actions/ltpAction';
import { SetPanelAction } from '../actions/panelChangeActions';
import { createPerformanceDataPreActions, performanceDataReloadAction, createPerformanceDataActions } from '../handlers/performanceDataHandler';
import { createReceiveLevelPreActions, receiveLevelReloadAction, createReceiveLevelActions } from '../handlers/receiveLevelHandler';
import { createTransmissionPowerPreActions, transmissionPowerReloadAction, createTransmissionPowerActions } from '../handlers/transmissionPowerHandler';
import { createAdaptiveModulationPreActions, adaptiveModulationReloadAction, createAdaptiveModulationActions } from '../handlers/adaptiveModulationHandler';
import { createTemperaturePreActions, temperatureReloadAction, createTemperatureActions } from '../handlers/temperatureHandler';
import { createSignalToInterferencePreActions, signalToInterferenceReloadAction, createSignalToInterferenceActions } from '../handlers/signalToInterferenceHandler';
import { createCrossPolarDiscriminationPreActions, crossPolarDiscriminationReloadAction, createCrossPolarDiscriminationActions } from '../handlers/crossPolarDiscriminationHandler';

import { MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';

const PerformanceHistoryComponentStyles = (theme: Theme) => createStyles({
  root: {
    display: "flex",
    flexWrap: "wrap",
  },
  margin: {
    margin: theme.spacing(1),
  },
  display: {
    display: "inline-block"
  },
  selectDropdown: {
    borderRadius: 1,
    position: "relative",
    backgroundColor: theme.palette.background.paper,
    border: "1px solid #ced4da",
    fontSize: 16,
    width: "auto",
    padding: "5px 26px 5px 12px",
    transition: theme.transitions.create(["border-color", "box-shadow"]),
  }
});

const mapProps = (state: IApplicationStoreState) => ({
  ...state.performanceHistory,
  activePanel: state.performanceHistory.currentOpenPanel,
  availableLtps: state.performanceHistory.ltps.distinctLtps,
  networkElements: state.performanceHistory.networkElements.deviceList,
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
  getDistinctLtpsIds: (selectedNetworkElement: string, selectedTimePeriod: string, selectedLtp: string, selectFirstLtp?: Function, resetLTP?: Function) => dispatcher.dispatch(loadDistinctLtpsbyNetworkElementAsync(selectedNetworkElement, selectedTimePeriod, selectedLtp, selectFirstLtp, resetLTP)),
  setCurrentPanel: (panelId: PanelId) => dispatcher.dispatch(new SetPanelAction(panelId)),
  timeIntervalChange: (time: PmDataInterval) => dispatcher.dispatch(new TimeChangeAction(time)),
  changeNode: (nodeId: string) => dispatcher.dispatch((dispatch: Dispatch) => {
    dispatch(new NavigateToApplication("performanceHistory", nodeId));
  })
});

export type NetworkElementType = {
  nodeId: string,
}
const NetworkElementTable = MaterialTable as MaterialTableCtorType<NetworkElementType>;

type PerformanceHistoryComponentProps = Connect<typeof mapProps, typeof mapDispatcher> & WithStyles<typeof PerformanceHistoryComponentStyles>;

type PerformanceHistoryComponentState = {
  selectedNetworkElement: string,
  selectedTimePeriod: string,
  selectedLtp: string,
  showNetworkElementsTable: boolean,
  showLtps: boolean,
  showPanels: boolean
};

/**
* Represents the component for Performance history application.
*/
class PerformanceHistoryComponent extends React.Component<PerformanceHistoryComponentProps, PerformanceHistoryComponentState>{
  /**
  * Initialises this instance
  */
  constructor(props: PerformanceHistoryComponentProps) {
    super(props);
    this.state = {
      selectedNetworkElement: "-1",
      selectedTimePeriod: "15min",
      selectedLtp: "-1",
      showNetworkElementsTable: true,
      showLtps: false,
      showPanels: false
    };
  }

  onTogglePanel = (panelId: PanelId) => {
    const nextActivePanel = panelId === this.props.activePanel ? null : panelId;
    this.props.setCurrentPanel(nextActivePanel);
    switch (nextActivePanel) {
      case "PerformanceData":
        this.props.reloadPerformanceData();
        break;
      case "ReceiveLevel":
        this.props.reloadReceiveLevel();
        break;
      case "TransmissionPower":
        this.props.reloadTransmissionPower();
        break;
      case "AdaptiveModulation":
        this.props.reloadAdaptiveModulation();
        break;
      case "Temperature":
        this.props.reloadTemperature();
        break;
      case "SINR":
        this.props.reloadSignalToInterference();
        break;
      case "CPD":
        this.props.reloadCrossPolarDiscrimination();
        break;
      default:
        // do nothing if all panels are closed
        break;
    }
  }

  render(): JSX.Element {
    const { classes } = this.props;
    const { activePanel, nodeId } = this.props;
    if (nodeId === "") {
      return (
        <>
          <NetworkElementTable title={"Please select the network element!"} idProperty={"nodeId"} rows={this.props.networkElements} asynchronus
            onHandleClick={(event, rowData) => { this.handleNetworkElementSelect(rowData.nodeId) }} columns={
              [{ property: "nodeId", title: "Node Name" }]
            } />
        </>
      )
    }
    else {
      3
      this.handleURLChange(nodeId);
      return (
        <>
          <h3>Selected Network Element: {this.state.selectedNetworkElement} </h3>
          {this.state.showLtps && (
            <div>
              <FormControl className={classes.display}>
                <span>
                  Select LTP
                </span>
                <Select className={classes.selectDropdown} value={this.state.selectedLtp} onChange={this.handleLtpChange}  >
                  <MenuItem value={"-1"}><em>--Select--</em></MenuItem>
                  {this.props.availableLtps.map(ltp =>
                    (<MenuItem value={ltp.key} key={ltp.key}>{ltp.key}</MenuItem>))}
                </Select>
                <span> Time-Period </span>
                <Select className={classes.selectDropdown} value={this.state.selectedTimePeriod} onChange={this.handleTimePeriodChange} >
                  <MenuItem value={"15min"}>15min</MenuItem>
                  <MenuItem value={"24hours"}>24hours</MenuItem>
                </Select>
              </FormControl>
              {this.state.showPanels && (
                <div>
                  <Panel activePanel={activePanel} panelId={"PerformanceData"} onToggle={this.onTogglePanel} title={"Performance Data"}>
                    <PerformanceData selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"ReceiveLevel"} onToggle={this.onTogglePanel} title={"Receive Level"}>
                    <ReceiveLevel selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"TransmissionPower"} onToggle={this.onTogglePanel} title={"Transmission Power"}>
                    <TransmissionPower selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"AdaptiveModulation"} onToggle={this.onTogglePanel} title={"Adaptive Modulation"}>
                    <AdaptiveModulation selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"Temperature"} onToggle={this.onTogglePanel} title={"Temperature"}>
                    <Temperature selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"SINR"} onToggle={this.onTogglePanel} title={"Signal-to-interference-plus-noise ratio (SINR)"}>
                    <SignalToInterference selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                  <Panel activePanel={activePanel} panelId={"CPD"} onToggle={this.onTogglePanel} title={"Cross Polar Discrimination"}>
                    <CrossPolarDiscrimination selectedTimePeriod={this.state.selectedTimePeriod} />
                  </Panel>
                </div>
              )}
            </div>
          )}
        </>
      );
    }
  };

  public componentDidMount() {
    this.props.getAllDevicesPMdata();
    this.props.enableFilterPerformanceData.onToggleFilter();
    this.props.enableFilterReceiveLevel.onToggleFilter();
    this.props.enableFilterTransmissionPower.onToggleFilter();
    this.props.enableFilterTemperature.onToggleFilter();
    this.props.enableFilterAdaptiveModulation.onToggleFilter();
    this.props.enableFilterSinr.onToggleFilter();
    this.props.enableFilterCpd.onToggleFilter();
  }

  /**
  * Function which selects the first ltp returned from the database on selection of network element.
  */
  private selectFirstLtp = (firstLtp: string) => {
    this.setState({
      showPanels: true,
      selectedLtp: firstLtp
    });
    this.preFilterChangeAndReload(this.state.selectedNetworkElement, this.state.selectedTimePeriod, firstLtp);
  }

  /**
  * A function which loads the tables based on prefilters defined by network element and ltp on selected time period.
  */
  private preFilterChangeAndReload = (networkElement: string, timePeriod: string, ltp: string) => {
    const preFilter = {
      "node-name": networkElement,
      "uuid-interface": ltp
    };
    this.props.performanceDataPreActions.onPreFilterChanged(preFilter);
    this.props.receiveLevelPreActions.onPreFilterChanged(preFilter);
    this.props.transmissionPowerPreActions.onPreFilterChanged(preFilter);
    this.props.adaptiveModulationPreActions.onPreFilterChanged(preFilter);
    this.props.temperaturePreActions.onPreFilterChanged(preFilter);
    this.props.signalToInterferencePreActions.onPreFilterChanged(preFilter);
    this.props.crossPolarDiscriminationPreActions.onPreFilterChanged(preFilter);

  }

  /**
   * Function which handles network element changes.
   */
  private handleNetworkElementSelect = (selectedNetworkElement: string) => {

    this.setState({
      showLtps: true,
      selectedNetworkElement: selectedNetworkElement,
      showNetworkElementsTable: false,
      showPanels: false,
      selectedLtp: "-1"
    });
    this.props.changeNode(selectedNetworkElement);
    this.props.getDistinctLtpsIds(selectedNetworkElement, this.state.selectedTimePeriod, "-1", this.selectFirstLtp);
  }

  private handleURLChange = (selectedNetworkElement: string) => {
    if (selectedNetworkElement !== this.state.selectedNetworkElement) {
      this.setState({
        showLtps: true,
        selectedNetworkElement: selectedNetworkElement,
        showPanels: false,
        selectedLtp: "-1"
      });
      this.props.getDistinctLtpsIds(selectedNetworkElement, this.state.selectedTimePeriod, "-1", this.selectFirstLtp);
    }
  }

  /**
    * Function which resets the ltps to "--select--" in the state if the passed parameter @ltpNotSelected is true.
    * @param ltpNotSelected: true, if existing selected is not available in the given time period, else false
    */
  private resetLtpDropdown = (ltpNotSelected: boolean) => {
    if (ltpNotSelected) {
      this.setState({
        selectedLtp: "-1",
      });
    }
  }

  /**
  * Function which handles the time period changes.
  */
  private handleTimePeriodChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedTimeInterval = event.target.value === "15min"
      ? PmDataInterval.pmInterval15Min
      : PmDataInterval.pmInterval24Hours

    this.setState({
      selectedTimePeriod: event.target.value,
    });
    this.props.timeIntervalChange(selectedTimeInterval);
    this.props.reloadPerformanceData();
    this.props.reloadReceiveLevel();
    this.props.reloadTransmissionPower();
    this.props.reloadAdaptiveModulation();
    this.props.reloadTemperature();
    this.props.reloadSignalToInterference();
    this.props.reloadCrossPolarDiscrimination();
    this.props.getDistinctLtpsIds(this.state.selectedNetworkElement, event.target.value, this.state.selectedLtp, undefined, this.resetLtpDropdown);
    this.preFilterChangeAndReload(this.state.selectedNetworkElement, event.target.value, this.state.selectedLtp);
  }

  /**
  * Function which handles the ltp changes.
  */
  private handleLtpChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    var showPanels: boolean = true;
    if (event.target.value === "-1") {
      showPanels = false;
    }
    this.setState({
      showPanels: showPanels,
      selectedLtp: event.target.value
    });
    this.preFilterChangeAndReload(this.state.selectedNetworkElement, this.state.selectedTimePeriod, event.target.value);
  }
}

const PerformanceHistoryApplication = withStyles(PerformanceHistoryComponentStyles)(connect(mapProps, mapDispatcher)(PerformanceHistoryComponent));
export default PerformanceHistoryApplication;
