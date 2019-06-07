import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { ReceiveLevelDataType } from '../models/receiveLevelDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createReceiveLevel15minProperties, createReceiveLevel15minActions } from '../handlers/receiveLevel15minHandler';
import { createReceiveLevel24hoursProperties, createReceiveLevel24hoursActions } from '../handlers/receiveLevel24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  receiveLevel15minProperties: createReceiveLevel15minProperties(state),
  receiveLevel24hoursProperties: createReceiveLevel24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  receiveLevel15minActions: createReceiveLevel15minActions(dispatcher.dispatch),
  receiveLevel24hoursActions: createReceiveLevel24hoursActions(dispatcher.dispatch)
});

type ReceiveLevelComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const ReceiveLevelTable = MaterialTable as MaterialTableCtorType<ReceiveLevelDataType>;

/**
 * The Component which gets the receiveLevel data from the database based on the selected time period.
 */
class ReceiveLevelComponent extends React.Component<ReceiveLevelComponentProps>{
  render(): JSX.Element {
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.receiveLevel15minProperties
      : this.props.receiveLevel24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.receiveLevel15minActions
      : this.props.receiveLevel24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const receiveLevelColumns: ColumnModel<ReceiveLevelDataType>[] = [
      { property: "radio-signal-id", title: "Radio signal", type: ColumnType.text },
      { property: "scanner-id", title: "Scanner ID", type: ColumnType.text },
      { property: "time-stamp", title: "End Time", type: ColumnType.text, disableFilter: true },
      {
        property: "suspect-interval-flag", title: "Suspect Interval", type: ColumnType.custom, customControl: ({ rowData }) => {
          const suspectIntervalFlag = rowData["suspect-interval-flag"].toString();
          return <div >{suspectIntervalFlag} </div>
        }
      }
    ];

    chartPagedData.datasets.forEach(ds => {
      receiveLevelColumns.push(addColumnLabels<ReceiveLevelDataType>(ds.name, ds.columnLabel));
    });

    return (
      <>
        {lineChart(chartPagedData)}
        <ReceiveLevelTable idProperty={"_id"} columns={receiveLevelColumns} {...properties} {...actions} />
      </>
    );
  };

  /**
   * This function gets the performance values for ReceiveLevel according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */
  private getChartDataValues = (rows: ReceiveLevelDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "rx-level-min",
      label: "rx-level-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rx min"
    }, {
      name: "rx-level-avg",
      label: "rx-level-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rx avg"
    }, {
      name: "rx-level-max",
      label: "rx-level-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "Rx max"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["time-stamp"],
          y: row[ds.name as keyof ReceiveLevelDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const ReceiveLevel = withRouter(connect(mapProps, mapDisp)(ReceiveLevelComponent));
export default ReceiveLevel;
