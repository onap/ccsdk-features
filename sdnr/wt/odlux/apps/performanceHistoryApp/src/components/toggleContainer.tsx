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
import ToggleButton from '@material-ui/lab/ToggleButton';
import ToggleButtonGroup from '@material-ui/lab/ToggleButtonGroup';
import BarChartIcon from '@material-ui/icons/BarChart';
import TableChartIcon from '@material-ui/icons/TableChart';
import { makeStyles } from '@material-ui/core';
import Tooltip from '@material-ui/core/Tooltip';

const styles = makeStyles({
    toggleButton: {
        alignItems: "center",
        justifyContent: "center",
        padding: "10px",
    }
});

type toggleProps = { selectedValue: string, onChange(value: string): void };

const ToggleContainer: React.FunctionComponent<toggleProps> = (props) => {

    const classes = styles();

    const handleChange = (event: React.MouseEvent<HTMLElement>, newView: string) => {
        if (newView !== null) {
            props.onChange(newView)
        }
    };

    const children = React.Children.toArray(props.children);

    return (
        <>
            <ToggleButtonGroup className={classes.toggleButton} size="medium" value={props.selectedValue} exclusive onChange={handleChange}>
                <ToggleButton aria-label="display-chart" key={1} value="chart">
                    <Tooltip title="Chart">
                        <BarChartIcon />
                    </Tooltip>
                </ToggleButton>
                <ToggleButton aria-label="display-table" key={2} value="table">
                    <Tooltip title="Table">
                        <TableChartIcon />
                    </Tooltip>
                </ToggleButton>
            </ToggleButtonGroup>
            {props.selectedValue === "chart" ? children[0] : props.selectedValue === "table" && children[1]}
        </>);

}

export default ToggleContainer;