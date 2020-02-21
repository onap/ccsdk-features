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
import { MenuItem, Select, FormControl } from '@material-ui/core';
import { makeStyles } from '@material-ui/core/styles';
import { LtpIds } from 'models/availableLtps';
import { Loader } from '../../../../framework/src/components/material-ui';


const useStyles = makeStyles(theme => ({
    display: {
        display: "inline-block"
    },
    selectDropdown: {
        borderRadius: 1,
        position: "relative",
        backgroundColor: theme.palette.background.paper,
        border: "1px solid #ced4da",
        fontSize: 16,
        width: "auto",
        padding: "5px 26px 5px 12px",
        transition: theme.transitions.create(["border-color", "box-shadow"]),
    },
    center: {
        "flex": "1",
        "height": "100%",
        "display": "flex",
        "alignItems": "center",
        "justifyContent": "center",
    }
}));

type LtpSelectionProps = { selectedNE: string, finishedLoading: boolean, selectedLtp: string, availableLtps: LtpIds[], onChangeLtp(event: React.ChangeEvent<HTMLSelectElement>): void, selectedTimePeriod: string, onChangeTimePeriod(event: React.ChangeEvent<HTMLSelectElement>): void };

export const LtpSelection = (props: LtpSelectionProps) => {
    const classes = useStyles();
    return (
        <>
            <h3>Selected Network Element: {props.selectedNE} </h3>
            <FormControl className={classes.display}>
                <span>
                    Select LTP
                </span>
                <Select className={classes.selectDropdown} value={props.selectedLtp} onChange={props.onChangeLtp}  >
                    <MenuItem value={"-1"}><em>--Select--</em></MenuItem>
                    {props.availableLtps.map(ltp =>
                        (<MenuItem value={ltp.key} key={ltp.key}>{ltp.key}</MenuItem>))}
                </Select>
                <span> Time-Period </span>
                <Select className={classes.selectDropdown} value={props.selectedTimePeriod} onChange={props.onChangeTimePeriod} >
                    <MenuItem value={"15min"}>15min</MenuItem>
                    <MenuItem value={"24hours"}>24hours</MenuItem>
                </Select>
            </FormControl>
            {
                !props.finishedLoading &&
                <div className={classes.center}>
                    <Loader />
                    <h3>Collecting Data ...</h3>
                </div>
            }
            {
                props.selectedLtp === "-1" && props.finishedLoading &&
                <div className={classes.center}>
                    <h3>Please select a LTP</h3>
                </div>
            }
        </>)
}

export default LtpSelection;