/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import { TextField, Select, MenuItem, FormControl, InputLabel } from '@mui/material';

import makeStyles from '@mui/styles/makeStyles';

const styles = makeStyles({
  filterInput: {
    marginRight: '15px',
  },
  filterContainer: {
    marginLeft: '90px',
  },
});

type filterProps = { isVisible: boolean; onFilterChanged: (property: string, filterTerm: string) => void; filters: any };

const ChartFilter: React.FunctionComponent<filterProps> = (props) => {


  const classes = styles();

  // make sure suspectIntervalFlag is a string to show the correct value in the select element

  const suspectIntervalFlag = props.filters.suspectIntervalFlag === undefined ? undefined : props.filters.suspectIntervalFlag.toString();
  return (
    <>
      {
        props.isVisible &&
        <div className={classes.filterContainer}>
          <TextField variant="standard" inputProps={{ 'aria-label': 'radio-signal-filter' }} className={classes.filterInput}
            label="Radio Signal" value={props.filters.radioSignalId || ''} onChange={(event) => props.onFilterChanged('radioSignalId', event.target.value)} InputLabelProps={{
              shrink: true,
            }} />
          <TextField variant="standard" inputProps={{ 'aria-label': 'scanner-id-filter' }} className={classes.filterInput} label="Scanner ID" value={props.filters.scannerId || ''} onChange={(event) => props.onFilterChanged('scannerId', event.target.value)} InputLabelProps={{
            shrink: true,
          }} />
          <TextField variant="standard" inputProps={{ 'aria-label': 'end-time-filter' }} className={classes.filterInput} label="End Time" value={props.filters.timeStamp || ''} onChange={(event) => props.onFilterChanged('timeStamp', event.target.value)} InputLabelProps={{
            shrink: true,
          }} />
          <FormControl variant="standard">
            <InputLabel id="suspect-interval-label" shrink>Suspect Interval</InputLabel>

            <Select variant="standard" aria-label="suspect-interval-selection" labelId="suspect-interval-label" value={suspectIntervalFlag || ''} onChange={(event) => props.onFilterChanged('suspectIntervalFlag', event.target.value as string)}>
              <MenuItem value={undefined} aria-label="none">None</MenuItem>
              <MenuItem value={'true'} aria-label="true">true</MenuItem>
              <MenuItem value={'false'} aria-label="false">false</MenuItem>
            </Select>
          </FormControl>
        </ div>
      }
    </>
  );
};

export default ChartFilter;