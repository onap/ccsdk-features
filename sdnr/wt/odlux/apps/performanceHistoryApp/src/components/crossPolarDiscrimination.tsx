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
import { RouteComponentProps, withRouter } from 'react-router-dom';

import { ColumnModel, ColumnType, MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { connect, Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';

import { SetFilterVisibility, SetSubViewAction } from '../actions/toggleActions';
import { createCrossPolarDiscriminationActions, createCrossPolarDiscriminationProperties } from '../handlers/crossPolarDiscriminationHandler';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { CrossPolarDiscriminationDatabaseDataType, CrossPolarDiscriminationDataType } from '../models/crossPolarDiscriminationDataType';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';
import ToggleContainer from './toggleContainer';


const mapProps = (state: IApplicationStoreState) => ({
  crossPolarDiscriminationProperties: createCrossPolarDiscriminationProperties(state),
  currentView: state.performanceHistory.subViews.CPD.subView,
  isFilterVisible: state.performanceHistory.subViews.CPD.isFilterVisible,
  existingFilter: state.performanceHistory.crossPolarDiscrimination.filter,

});

const mapDisp = (dispatcher: IDispatcher) => ({
  crossPolarDiscriminationActions: createCrossPolarDiscriminationActions(dispatcher.dispatch),
  setSubView: (value: 'chart' | 'table') => dispatcher.dispatch(new SetSubViewAction('CPD', value)),
  toggleFilterButton: (value: boolean) => { dispatcher.dispatch(new SetFilterVisibility('CPD', value));},
});

type CrossPolarDiscriminationComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string;
};

const CrossPolarDiscriminationTable = MaterialTable as MaterialTableCtorType<CrossPolarDiscriminationDataType>;

/**
 * The Component which gets the crossPolarDiscrimination data from the database based on the selected time period.
 */
class CrossPolarDiscriminationComponent extends React.Component<CrossPolarDiscriminationComponentProps> {
  onToggleFilterButton = () => {
    this.props.toggleFilterButton(!this.props.isFilterVisible);
  };

  onChange = (value: 'chart' | 'table') => {
    this.props.setSubView(value);
  };

  onFilterChanged = (property: string, filterTerm: string) => {
    this.props.crossPolarDiscriminationActions.onFilterChanged(property, filterTerm);
    if (!this.props.crossPolarDiscriminationProperties.showFilter)
      this.props.crossPolarDiscriminationActions.onToggleFilter(false);
  };

  render(): JSX.Element {
    const properties = this.props.crossPolarDiscriminationProperties;
    const actions = this.props.crossPolarDiscriminationActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const cpdColumns: ColumnModel<CrossPolarDiscriminationDataType>[] = [
      { property: 'radioSignalId', title: 'Radio signal', type: ColumnType.text },
      { property: 'scannerId', title: 'Scanner ID', type: ColumnType.text },
      { property: 'timeStamp', title: 'End Time', type: ColumnType.text },
      {
        property: 'suspectIntervalFlag', title: 'Suspect Interval', type: ColumnType.boolean,
      },
    ];

    chartPagedData.datasets.forEach(ds => {
      cpdColumns.push(addColumnLabels<CrossPolarDiscriminationDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        <ToggleContainer onToggleFilterButton={this.onToggleFilterButton} showFilter={this.props.isFilterVisible}
          existingFilter={this.props.crossPolarDiscriminationProperties.filter} onFilterChanged={this.onFilterChanged} selectedValue={this.props.currentView} onChange={this.onChange}>
          {lineChart(chartPagedData)}
          <CrossPolarDiscriminationTable stickyHeader idProperty={'_id'} tableId="cross-polar-discrimination-table" columns={cpdColumns} {...properties} {...actions} />
        </ToggleContainer>
      </>
    );
  }

  /**
   * This function gets the performance values for CPD according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: CrossPolarDiscriminationDataType[]): IDataSetsObject => {
    const data_rows = [...rows];
    sortDataByTimeStamp(data_rows);

    const datasets: IDataSet[] = [{
      name: 'xpdMin',
      label: 'xpd-min',
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'CPD (min)[db]',
    }, {
      name: 'xpdAvg',
      label: 'xpd-avg',
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'CPD (avg)[db]',
    }, {
      name: 'xpdMax',
      label: 'xpd-max',
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: 'CPD (max)[db]',
    }];

    data_rows.forEach(row => {
      row.xpdMin = row.performanceData.xpdMin;
      row.xpdAvg = row.performanceData.xpdAvg;
      row.xpdMax = row.performanceData.xpdMax;
      datasets.forEach(ds => {
        ds.data.push({
          x: row['timeStamp' as keyof CrossPolarDiscriminationDataType] as string,
          y: row.performanceData[ds.name as keyof CrossPolarDiscriminationDatabaseDataType] as string,
        });
      });
    });
    return {
      datasets: datasets,
    };
  };
}
const CrossPolarDiscrimination = withRouter(connect(mapProps, mapDisp)(CrossPolarDiscriminationComponent));
export default CrossPolarDiscrimination;
