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
import moment from 'moment';
import { Line } from 'react-chartjs-2';

import { IDataSetsObject } from '../models/chartTypes';
import { ITimeStamp } from '../models/chartTypes';

const style: React.CSSProperties = {
  height: '80%',
};

export const lineChart = (chartPagedData: IDataSetsObject) => {
  return (
    <div style={style}>
      <Line ref="chart" data={chartPagedData} options={{
        responsive: true,
        maintainAspectRatio: false,
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
              },
            },
            display: true,
            scaleLabel: {
              display: true,
              labelString: 'Timestamp',
            },
          }],
          yAxes: [{
            ticks: {
              beginAtZero: true,
            },
            scaleLabel: {
              display: true,
              labelString: 'Value',
            },
          }],
        },
      }} />
    </div>
  );
};

export const sortDataByTimeStamp = <T extends ITimeStamp>(_rows: T[]): T[] => {
  return (_rows.sort((a, b) => {
    const result = Date.parse(a.timeStamp) - Date.parse(b.timeStamp);
    return isNaN(result) ? 0 : result;
  }));
};