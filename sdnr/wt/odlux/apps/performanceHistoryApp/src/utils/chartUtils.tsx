import * as React from 'react';
import { IDataSetsObject } from '../models/chartTypes';
import { Line } from 'react-chartjs-2';
import * as moment from 'moment';
import { ITimeStamp } from 'models/chartTypes';

export const lineChart = (chartPagedData: IDataSetsObject) => {
  return (
    <Line ref="chart" data={chartPagedData} options={{
      scales: {
        xAxes: [{
          type: 'time',
          time: {
            displayFormats: {
              'second': 'DD MMM YYYY HH:mm:ss',
              'minute': 'DD MMM YYYY HH:mm:ss',
              'hour': 'DD MMM YYYY HH:mm:ss',
              'year': 'DD MMM YYYY HH:mm:ss',
            },
            parser: function (date: string) {
              let offsetValue = new Date().getTimezoneOffset();
              var utcDate = moment(date, 'YYYY-MM-DDTHH:mm:ss').utcOffset(offsetValue).utc(false);
              return utcDate;
            }
          },
          display: true,
          scaleLabel: {
            display: true,
            labelString: 'Timestamp'
          }
        }],
        yAxes: [{
          ticks: {
            beginAtZero: true
          },
          scaleLabel: {
            display: true,
            labelString: 'Value'
          }
        }]
      }
    }} />
  );
}

export const sortDataByTimeStamp = <T extends ITimeStamp>(_rows: T[]): T[] => {
  return (_rows.sort((a, b) => {
    const result = Date.parse(a["time-stamp"]) - Date.parse(b["time-stamp"]);
    return isNaN(result) ? 0 : result;
  }));
}