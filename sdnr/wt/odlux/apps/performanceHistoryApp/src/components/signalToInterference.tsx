import * as React from 'react';

import { withRouter, RouteComponentProps } from 'react-router-dom';

import { MaterialTable, ColumnType, ColumnModel, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';

import { SignalToInterferenceDataType } from '../models/signalToInteferenceDataType';
import { IDataSet, IDataSetsObject } from '../models/chartTypes';
import { createSignalToInterference15minProperties, createSignalToInterference15minActions } from '../handlers/signalToInterference15minHandler';
import { createSignalToInterference24hoursProperties, createSignalToInterference24hoursActions } from '../handlers/signalToInterference24hoursHandler';
import { lineChart, sortDataByTimeStamp } from '../utils/chartUtils';
import { addColumnLabels } from '../utils/tableUtils';

const mapProps = (state: IApplicationStoreState) => ({
  signalToInterference15minProperties: createSignalToInterference15minProperties(state),
  signalToInterference24hoursProperties: createSignalToInterference24hoursProperties(state)
});

const mapDisp = (dispatcher: IDispatcher) => ({
  signalToInterference15minActions: createSignalToInterference15minActions(dispatcher.dispatch),
  signalToInterference24hoursActions: createSignalToInterference24hoursActions(dispatcher.dispatch)
});

type SignalToInterferenceComponentProps = RouteComponentProps & Connect<typeof mapProps, typeof mapDisp> & {
  selectedTimePeriod: string
};

const SignalToInterferenceTable = MaterialTable as MaterialTableCtorType<SignalToInterferenceDataType>;

/**
 * The Component which gets the signal to interference data from the database based on the selected time period.
 */
class SignalToInterferenceComponent extends React.Component<SignalToInterferenceComponentProps>{
  render(): JSX.Element {
    const properties = this.props.selectedTimePeriod === "15min"
      ? this.props.signalToInterference15minProperties
      : this.props.signalToInterference24hoursProperties;
    const actions = this.props.selectedTimePeriod === "15min"
      ? this.props.signalToInterference15minActions
      : this.props.signalToInterference24hoursActions;

    const chartPagedData = this.getChartDataValues(properties.rows);

    const sinrColumns: ColumnModel<SignalToInterferenceDataType>[] = [

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
      sinrColumns.push(addColumnLabels<SignalToInterferenceDataType>(ds.name, ds.columnLabel));
    });
    return (
      <>
        {lineChart(chartPagedData)}
        <SignalToInterferenceTable idProperty={"_id"} columns={sinrColumns} {...properties} {...actions}
        />
      </>
    );
  };

  /**
   * This function gets the performance values for SINR according on the chartjs dataset structure 
   * which is to be sent to the chart.
   */

  private getChartDataValues = (rows: SignalToInterferenceDataType[]): IDataSetsObject => {
    const _rows = [...rows];
    sortDataByTimeStamp(_rows);

    const datasets: IDataSet[] = [{
      name: "snir-min",
      label: "snir-min",
      borderColor: '#0e17f3de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "SINR (min)[db]"
    }, {
      name: "snir-avg",
      label: "snir-avg",
      borderColor: '#08edb6de',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "SINR (avg)[db]"
    }, {
      name: "snir-max",
      label: "snir-max",
      borderColor: '#b308edde',
      bezierCurve: false,
      lineTension: 0,
      fill: false,
      data: [],
      columnLabel: "SINR (max)[db]"
    }];

    _rows.forEach(row => {
      datasets.forEach(ds => {
        ds.data.push({
          x: row["time-stamp"],
          y: row[ds.name as keyof SignalToInterferenceDataType] as string
        });
      });
    });
    return {
      datasets: datasets
    };
  }
}

const SignalToInterference = withRouter(connect(mapProps, mapDisp)(SignalToInterferenceComponent));
export default SignalToInterference;
