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

import { withRouter, RouteComponentProps } from 'react-router-dom';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';;
import { Theme } from '@mui/material';
import { WithStyles } from '@mui/styles';
import createStyles from '@mui/styles/createStyles';
import withStyles from '@mui/styles/withStyles';
import { Doughnut } from 'react-chartjs-2';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';

const styles = (theme: Theme) => createStyles({
  pageWidthSettings: {
    width: '50%',
    float: 'left'
  },
})

const scrollbar = { overflow: "auto", paddingRight: "20px" }

let connectionStatusinitialLoad = true;
let connectionStatusinitialStateChanged = false;
let connectionStatusDataLoad: number[] = [0, 0, 0, 0];
let connectionTotalCount = 0;

let alarmStatusinitialLoad = true;
let alarmStatusinitialStateChanged = false;
let alarmStatusDataLoad: number[] = [0, 0, 0, 0];
let alarmTotalCount = 0;

const mapProps = (state: IApplicationStoreState) => ({
  connectionStatusCount: state.connect.connectionStatusCount,
  alarmStatus: state.fault.faultStatus
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path)),
});

type HomeComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDispatch> & WithStyles<typeof styles>;

class DashboardHome extends React.Component<HomeComponentProps>  {
  constructor(props: HomeComponentProps) {
    super(props);
    this.state = {
    }
  }

  render(): JSX.Element {
    const { classes } = this.props;

    if (!this.props.connectionStatusCount.isLoadingConnectionStatusChart) {
      connectionStatusDataLoad = [
        this.props.connectionStatusCount.Connected,
        this.props.connectionStatusCount.Connecting,
        this.props.connectionStatusCount.Disconnected,
        this.props.connectionStatusCount.UnableToConnect
      ];
      connectionTotalCount = this.props.connectionStatusCount.Connected + this.props.connectionStatusCount.Connecting
        + this.props.connectionStatusCount.Disconnected + this.props.connectionStatusCount.UnableToConnect;

    }

    if (!this.props.alarmStatus.isLoadingAlarmStatusChart) {
      alarmStatusDataLoad = [
        this.props.alarmStatus.critical,
        this.props.alarmStatus.major,
        this.props.alarmStatus.minor,
        this.props.alarmStatus.warning
      ];
      alarmTotalCount = this.props.alarmStatus.critical + this.props.alarmStatus.major
        + this.props.alarmStatus.minor + this.props.alarmStatus.warning;
    }

    /** Available Network Connection Status chart data */
    const connectionStatusData = {
      labels: ['Connected', 'Connecting', 'Disconnected', 'UnableToConnect'],
      datasets: [{
        data: connectionStatusDataLoad,
        backgroundColor: [
          'rgb(0, 153, 51)',
          'rgb(255, 102, 0)',
          'rgb(191, 191, 191)',
          'rgb(191, 191, 191)'
        ]
      }]
    };


    /** No Devices available */
    const connectionStatusUnavailableData = {
      labels: ['No Devices available'],
      datasets: [{
        data: [1],
        backgroundColor: [
          'rgb(255, 255, 255)'
        ]
      }]
    };

    /** Loading Connection Status chart */
    const connectionStatusisLoading = {
      labels: ['Loading chart...'],
      datasets: [{
        data: [1],
        backgroundColor: [
          'rgb(255, 255, 255)'
        ]
      }]
    };

    /** Loading Alarm Status chart */
    const alarmStatusisLoading = {
      labels: ['Loading chart...'],
      datasets: [{
        data: [1],
        backgroundColor: [
          'rgb(255, 255, 255)'
        ]
      }]
    };

    /** Connection status options */
    let labels: String[] = ['Connected', 'Connecting', 'Disconnected', 'UnableToConnect'];
    const connectionStatusOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 0
      },
      plugins: {
        legend: {
          display: true,
          position: 'top'
        }
      },
      onClick: (event: MouseEvent, item: any) => {
        if (item[0]) {
          let connectionStatus = labels[item[0]._index] + '';
          this.props.navigateToApplication("connect", '/connectionStatus/' + connectionStatus);
        }
      }
    }

    /** Connection status unavailable options */
    const connectionStatusUnavailableOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 0
      },
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          enabled: false
        }
      }
    }

    /** Add text inside the doughnut chart for Connection Status */
    const connectionStatusPlugins = [{
      beforeDraw: function (chart: any) {
        var width = chart.width,
          height = chart.height,
          ctx = chart.ctx;
        ctx.restore();
        var fontSize = (height / 480).toFixed(2);
        ctx.font = fontSize + "em sans-serif";
        ctx.textBaseline = "top";
        var text = "Network Connection Status",
          textX = Math.round((width - ctx.measureText(text).width) / 2),
          textY = height / 2;
        ctx.fillText(text, textX, textY);
        ctx.save();
      }
    }]

    /** Alarm status Data */
    const alarmStatusData = {
      labels: [
        'Critical',
        'Major',
        'Minor',
        'Warning'
      ],
      datasets: [{
        data: alarmStatusDataLoad,
        backgroundColor: [
          'rgb(240, 25, 10)',
          'rgb(240, 133, 10)',
          'rgb(240, 240, 10)',
          'rgb(46, 115, 176)'
        ],
      }]
    }

    /** No Alarm status available */
    const alarmStatusUnavailableData = {
      labels: ['No Alarms available'],
      datasets: [{
        data: [1],
        backgroundColor: [
          'rgb(0, 153, 51)'
        ]
      }]
    };

    /** Alarm status Options */
    let alarmLabels: String[] = ['Critical', 'Major', 'Minor', 'Warning'];
    const alarmStatusOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 0
      },
      plugins: {
        legend: {
          display: true,
          position: 'top'
        }
      },
      onClick: (event: MouseEvent, item: any) => {
        if (item[0]) {
          let severity = alarmLabels[item[0].index] + '';
          this.props.navigateToApplication("fault", '/alarmStatus/' + severity);
        }
      },
    };

    /** Alarm status unavailable options */
    const alarmStatusUnavailableOptions = {
      responsive: true,
      maintainAspectRatio: false,
      animation: {
        duration: 0
      },
      plugins: {
        legend: {
          display: true,
          position: 'top'
        },
        tooltip: {
          enabled: false
        }
      }
    }
    /** Add text inside the doughnut chart for Alarm Status */
    const alarmStatusPlugins = [{
      beforeDraw: function (chart: any) {
        var width = chart.width,
          height = chart.height,
          ctx = chart.ctx;
        ctx.restore();
        var fontSize = (height / 480).toFixed(2);
        ctx.font = fontSize + "em sans-serif";
        ctx.textBaseline = "top";
        var text = "Network Alarm Status",
          textX = Math.round((width - ctx.measureText(text).width) / 2),
          textY = height / 2;
        ctx.fillText(text, textX, textY);
        ctx.save();
      }
    }]

    return (
      <>
        <div style={scrollbar} >
          <h1>Welcome to ODLUX</h1>
          <div style={{ width: '50%', float: 'left' }}>
            {this.checkElementsAreLoaded() ?
              this.checkConnectionStatus() && connectionTotalCount != 0 ?
                <Doughnut
                  data={connectionStatusData}
                  type={Doughnut}
                  width={500}
                  height={500}
                  options={connectionStatusOptions}
                  plugins={connectionStatusPlugins}
                />
                : <Doughnut
                  data={connectionStatusUnavailableData}
                  type={Doughnut}
                  width={500}
                  height={500}
                  options={connectionStatusUnavailableOptions}
                  plugins={connectionStatusPlugins} />
              : <Doughnut
                data={connectionStatusisLoading}
                type={Doughnut}
                width={500}
                height={500}
                options={connectionStatusUnavailableOptions}
                plugins={connectionStatusPlugins}
              />
            }
          </div>
          <div style={{ width: '50%', float: 'left' }}>
            {this.checkAlarmsAreLoaded() ?
              this.checkAlarmStatus() && alarmTotalCount != 0 ?
                <Doughnut
                  data={alarmStatusData}
                  type={Doughnut}
                  width={500}
                  height={500}
                  options={alarmStatusOptions}
                  plugins={alarmStatusPlugins}
                />
                : <Doughnut
                  data={alarmStatusUnavailableData}
                  type={Doughnut}
                  width={500}
                  height={500}
                  options={alarmStatusUnavailableOptions}
                  plugins={alarmStatusPlugins}
                />
              : <Doughnut
                data={alarmStatusisLoading}
                type={Doughnut}
                width={500}
                height={500}
                options={alarmStatusUnavailableOptions}
                plugins={alarmStatusPlugins}
              />
            }
          </div>
        </div>
      </>
    )
  }

  /** Check if connection status data available */
  public checkConnectionStatus = () => {
    let statusCount = this.props.connectionStatusCount;
    if (statusCount.isLoadingConnectionStatusChart) {
      return true;
    }
    if (statusCount.Connected == 0 && statusCount.Connecting == 0 && statusCount.Disconnected == 0
      && statusCount.UnableToConnect == 0) {
      return false;
    } else {
      return true;
    }
  }

  /** Check if connection status chart data is loaded */
  public checkElementsAreLoaded = () => {
    let isLoadingCheck = this.props.connectionStatusCount;
    if (connectionStatusinitialLoad && !isLoadingCheck.isLoadingConnectionStatusChart) {
      if (this.checkConnectionStatus()) {
        connectionStatusinitialLoad = false;
        return true;
      }
      return false;
    } else if (connectionStatusinitialLoad && isLoadingCheck.isLoadingConnectionStatusChart) {
      connectionStatusinitialLoad = false;
      connectionStatusinitialStateChanged = true;
      return !isLoadingCheck.isLoadingConnectionStatusChart;
    } else if (connectionStatusinitialStateChanged) {
      if (!isLoadingCheck.isLoadingConnectionStatusChart) {
        connectionStatusinitialStateChanged = false;
      }
      return !isLoadingCheck.isLoadingConnectionStatusChart;
    }
    return true;
  }

  /** Check if alarms data available */
  public checkAlarmStatus = () => {
    let alarmCount = this.props.alarmStatus;
    if (alarmCount.isLoadingAlarmStatusChart) {
      return true;
    }
    if (alarmCount.critical == 0 && alarmCount.major == 0 && alarmCount.minor == 0 && alarmCount.warning == 0) {
      return false;
    }
    else {
      return true;
    }
  }

  /** Check if alarm status chart data is loaded */
  public checkAlarmsAreLoaded = () => {
    let isLoadingCheck = this.props.alarmStatus;
    if (alarmStatusinitialLoad && !isLoadingCheck.isLoadingAlarmStatusChart) {
      if (this.checkAlarmStatus()) {
        alarmStatusinitialLoad = false;
        return true;
      }
      return false;
    } else if (alarmStatusinitialLoad && isLoadingCheck.isLoadingAlarmStatusChart) {
      alarmStatusinitialLoad = false;
      alarmStatusinitialStateChanged = true;
      return !isLoadingCheck.isLoadingAlarmStatusChart;
    } else if (alarmStatusinitialStateChanged) {
      if (!isLoadingCheck.isLoadingAlarmStatusChart) {
        alarmStatusinitialStateChanged = false;
      }
      return !isLoadingCheck.isLoadingAlarmStatusChart;
    }
    return true;
  }
}

export default (withRouter(connect(mapProps, mapDispatch)(DashboardHome)));