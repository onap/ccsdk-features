import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { PerformanceDataType } from '../models/performanceDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createPerformanceData15minProperties, createPerformanceData15minActions } from '../handlers/performanceData15minHandler';
import { createPerformanceData24hoursProperties, createPerformanceData24hoursActions } from '../handlers/performanceData24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  performanceData15minProperties: createPerformanceData15minProperties(state),
  performanceData24hoursProperties: createPerformanceData24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  performanceData15minActions: createPerformanceData15minActions(dispatcher.dispatch),
  performanceData24hoursActions: createPerformanceData24hoursActions(dispatcher.dispatch)
});

type PerformanceDataComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const PerformanceDataTable = MaterialTable as MaterialTableCtorType<PerformanceDataType>;

/**
 * The Component which gets the performance data from the database based on the selected time period.
 */
class PerformanceDataComponent extends React.Component<PerformanceDataComponentProps>{
  render(): JSX.Element {
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.performanceData15minProperties
      : this.props.performanceData24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.performanceData15minActions
      : this.props.performanceData24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);
    const performanceColumns: ColumnModel<PerformanceDataType>[] = [
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
      performanceColumns.push(addColumnLabels<PerformanceDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        {lineChart(chartPagedData)}
        <PerformanceDataTable idProperty={"_id"} columns={performanceColumns} {...properties} {...actions} />
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
      datasets.forEach(ds => {
        ds.data.push({
          x: row["time-stamp"],
          y: row[ds.name as keyof PerformanceDataType] as string
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
