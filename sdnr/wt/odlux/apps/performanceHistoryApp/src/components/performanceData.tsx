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
import { PerformanceDataType, PerformanceDatabaseDataType } from '../models/performanceDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createPerformanceDataProperties, createPerformanceDataActions } from '../handlers/performanceDataHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';
import { SetSubViewAction } from '../actions/toggleActions';

const mapProps = (state: IApplicationStoreState) => ({
  performanceDataProperties: createPerformanceDataProperties(state),
  currentView: state.performanceHistory.subViews.performanceDataSelection,
});

const mapDisp = (dispatcher: IDispatcher) => ({
  performanceDataActions: createPerformanceDataActions(dispatcher.dispatch),
  setSubView: (value: "chart" | "table") => dispatcher.dispatch(new SetSubViewAction("performanceData", value)),
});

type PerformanceDataComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const PerformanceDataTable = MaterialTable as MaterialTableCtorType<PerformanceDataType>;

/**
 * The Component which gets the performance data from the database based on the selected time period.
 */
class PerformanceDataComponent extends React.Component<PerformanceDataComponentProps>{

  onChange = (value: "chart" | "table") => {
    this.props.setSubView(value);
  }

  render(): JSX.Element {
    const properties = this.props.performanceDataProperties;
    const actions = this.props.performanceDataActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const performanceColumns: ColumnModel<PerformanceDataType>[] = [
      { property: "radioSignalId", title: "Radio signal", type: ColumnType.text },
      { property: "scannerId", title: "Scanner ID", type: ColumnType.text },
      { property: "timeStamp", title: "End Time", type: ColumnType.text },
      {
        property: "suspectIntervalFlag", title: "Suspect Interval", customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspectIntervalFlag"].toString();
          return <div >{suspectIntervalFlag} </div>
        }
      }
    ];

    chartPagedData.datasets.forEach(ds => {
      performanceColumns.push(addColumnLabels<PerformanceDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        <ToggleContainer selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <PerformanceDataTable idProperty={"_id"} columns={performanceColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  };

  /**
   * This function gets the performance values for PerformanceData according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: PerformanceDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "es",
      label: "es",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "ES"
    }, {
      name: "ses",
      label: "ses",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "SES"
    }, {
      name: "unavailability",
      label: "unavailability",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Unavailability"
    }];

    _rows.forEach(row => {
      row.es = row.performanceData.es;
      row.ses = row.performanceData.ses;
      row.unavailability = row.performanceData.unavailability;
      datasets.forEach(ds => {
        ds.data.push({
          x: row["timeStamp" as keyof PerformanceDataType] as string,
          y: row.performanceData[ds.name as keyof PerformanceDatabaseDataType] as string
        });
      });
    });

    return {
      datasets: datasets
    };
  }
}

const PerformanceData = withRouter(connect(mapProps, mapDisp)(PerformanceDataComponent));
export default PerformanceData;
