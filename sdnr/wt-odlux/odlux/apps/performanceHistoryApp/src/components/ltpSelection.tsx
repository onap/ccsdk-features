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

import { FormControl, MenuItem, Select, SelectChangeEvent, Typography } from '@mui/material';
import { Theme } from '@mui/material/styles';
import makeStyles from '@mui/styles/makeStyles';
import { Loader } from '../../../../framework/src/components/material-ui';
import { LtpIds } from '../models/availableLtps';

const useStyles = makeStyles((theme: Theme) => ({
  display: {
    display: 'inline-block',
  },
  selectDropdown: {
    borderRadius: 1,
    position: 'relative',
    backgroundColor: theme.palette.background.paper,
    border: '1px solid #ced4da',
    fontSize: 16,
    width: 'auto',
    padding: '5px 5px 5px 5px',
    transition: theme.transitions.create(['border-color', 'box-shadow']),
  },
  center: {
    'flex': '1',
    'height': '100%',
    'display': 'flex',
    'alignItems': 'center',
    'justifyContent': 'center',
    flexDirection: 'column',
  },
}));

type LtpSelectionProps = {
  selectedNE: string; error?: string; finishedLoading: boolean; selectedLtp: string;
  availableLtps: LtpIds[];
  onChangeLtp(event: SelectChangeEvent<HTMLSelectElement | string>): void;
  selectedTimePeriod: string;
  onChangeTimePeriod(event: SelectChangeEvent<HTMLSelectElement | string>): void;
};

export const LtpSelection = (props: LtpSelectionProps) => {
  const classes = useStyles();
  return (
    <>
      <h3>Selected Network Element: {props.selectedNE} </h3>
      <FormControl variant="standard" className={classes.display}>
        <span>
          Select LTP
        </span>
        <Select variant="standard" className={classes.selectDropdown} value={props.selectedLtp} onChange={props.onChangeLtp} aria-label="ltp-selection" >
          <MenuItem value={'-1'} aria-label="none"><em>--Select--</em></MenuItem>
          {props.availableLtps.map(ltp =>
            (<MenuItem value={ltp.key} key={ltp.key} aria-label={ltp.key}>{ltp.key}</MenuItem>))}
        </Select>
        <span> Time-Period </span>
        <Select variant="standard" className={classes.selectDropdown} value={props.selectedTimePeriod} onChange={props.onChangeTimePeriod} aria-label="time-period-selection">
          <MenuItem value={'15min'} aria-label="15minutes">15min</MenuItem>
          <MenuItem value={'24hours'} aria-label="24hours">24hours</MenuItem>
        </Select>
      </FormControl>
      {
        !props.finishedLoading && !props.error &&
        <div className={classes.center}>
          <Loader />
          <h3>Collecting Data ...</h3>
        </div>
      }
      {
        props.finishedLoading && props.error &&
        <div className={classes.center}>
          <h3>Data couldn't be loaded</h3>
          <Typography variant="body1">{props.error}</Typography>
        </div>
      }
      {
        props.selectedLtp === '-1' && props.finishedLoading && !props.error && (props.availableLtps.length > 0 ?
          <div className={classes.center}>
            <h3>Please select a LTP</h3>
          </div>
          :
          <div className={classes.center}>
            <h3>No performance data found</h3>
          </div>)
      }
    </>);
};

export default LtpSelection;