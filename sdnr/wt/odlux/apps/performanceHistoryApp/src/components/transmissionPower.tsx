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

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { TransmissionPowerDataType } from '../models/transmissionPowerDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createTransmissionPowerProperties, createTransmissionPowerActions } from '../handlers/transmissionPowerHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  transmissionPowerProperties: createTransmissionPowerProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  transmissionPowerActions: createTransmissionPowerActions(dispatcher.dispatch),
});

type TransmissionPowerComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
}

const TransmissionPowerTable = MaterialTable as MaterialTableCtorType<TransmissionPowerDataType>;

/**
 * The Component which gets the transmission power data from the database based on the selected time period.
 */
class TransmissionPowerComponent extends React.Component<TransmissionPowerComponentProps>{
  render(): JSX.Element {
    const properties = this.props.transmissionPowerProperties
    const actions = this.props.transmissionPowerActions

    const chartPagedData = this.getChartDataValues(properties.rows);

    const transmissionColumns: ColumnModel<TransmissionPowerDataType>[] = [
      { property: "radioSignalId", title: "Radio signal", type: ColumnType.text },
      { property: "scannerId", title: "Scanner ID", type: ColumnType.text },
      { property: "utcTimeStamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspectIntervalFlag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspectIntervalFlag"].toString();
          return <div >{suspectIntervalFlag} </div>
        }
      }
    ];

    chartPagedData.datasets.forEach(ds => {
      transmissionColumns.push(addColumnLabels<TransmissionPowerDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        {lineChart(chartPagedData)}
        <TransmissionPowerTable idProperty={"_id"} columns={transmissionColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for TransmissionPower according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: TransmissionPowerDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "txLevelMin",
      label: "tx-level-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx min"
    }, {
      name: "txLevelAvg",
      label: "tx-level-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx avg"
    }, {
      name: "txLevelMax",
      label: "tx-level-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Tx max"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["utcTimeStamp" as keyof TransmissionPowerDataType] as string,
          y: row[ds.name as keyof TransmissionPowerDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const TransmissionPower = withRouter(connect(mapProps, mapDisp)(TransmissionPowerComponent));
export default TransmissionPower;
