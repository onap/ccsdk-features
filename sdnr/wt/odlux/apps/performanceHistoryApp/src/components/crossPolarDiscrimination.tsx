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

import { MaterialTable, ColumnType, MaterialTableCtorType, ColumnModel } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { CrossPolarDiscriminationDataType } from '../models/crossPolarDiscriminationDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createCrossPolarDiscriminationProperties, createCrossPolarDiscriminationActions } from '../handlers/crossPolarDiscriminationHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  crossPolarDiscriminationProperties: createCrossPolarDiscriminationProperties(state),
});

const mapDisp = (dispatcher: IDispatcher) => ({
  crossPolarDiscriminationActions: createCrossPolarDiscriminationActions(dispatcher.dispatch),
});

type CrossPolarDiscriminationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const CrossPolarDiscriminationTable = MaterialTable as MaterialTableCtorType<CrossPolarDiscriminationDataType>;

/**
 * The Component which gets the crossPolarDiscrimination data from the database based on the selected time period.
 */
class CrossPolarDiscriminationComponent extends React.Component<CrossPolarDiscriminationComponentProps>{
  render(): JSX.Element {
    const properties = this.props.crossPolarDiscriminationProperties;
    const actions = this.props.crossPolarDiscriminationActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const cpdColumns: ColumnModel<CrossPolarDiscriminationDataType>[] = [
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
      cpdColumns.push(addColumnLabels<CrossPolarDiscriminationDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        {lineChart(chartPagedData)}
        <CrossPolarDiscriminationTable idProperty={"_id"} columns={cpdColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for CPD according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: CrossPolarDiscriminationDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "xpdMin",
      label: "xpd-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (min)[db]"
    }, {
      name: "xpdAvg",
      label: "xpd-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (avg)[db]"
    }, {
      name: "xpdMax",
      label: "xpd-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "CPD (max)[db]"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["utcTimeStamp" as keyof CrossPolarDiscriminationDataType] as string,
          y: row[ds.name as keyof CrossPolarDiscriminationDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}
const CrossPolarDiscrimination = withRouter(connect(mapProps, mapDisp)(CrossPolarDiscriminationComponent));
export default CrossPolarDiscrimination;
