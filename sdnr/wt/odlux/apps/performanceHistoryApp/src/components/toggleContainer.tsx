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
import ChartFilter from './chartFilter'
import FilterListIcon from '@material-ui/icons/FilterList';

const styles = makeStyles({
    toggleButtonContainer: {
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        padding: "10px",
    },
    subViewGroup: {
        padding: "10px"
    },
    filterGroup: {
        marginLeft: "10px"
    }
});

type toggleProps = { selectedValue: string, onChange(value: string): void, showFilter: boolean, onToggleFilterButton(): void, onFilterChanged: (property: string, filterTerm: string) => void, existingFilter: any };

const ToggleContainer: React.FunctionComponent<toggleProps> = (props) => {

    const classes = styles();

    const handleChange = (event: React.MouseEvent<HTMLElement>, newView: string) => {
        if (newView !== null) {
            props.onChange(newView)
        }
    };

    const handleFilterChange = (event: React.MouseEvent<HTMLElement>, newView: string) => {
        props.onToggleFilterButton();
    };

    const children = React.Children.toArray(props.children);

    //hide filter if visible + table
    //put current name into state, let container handle stuff itelf, register for togglestate, get right via set name

    return (
        <>
            <div className={classes.toggleButtonContainer} >
                <ToggleButtonGroup className={classes.subViewGroup} size="medium" value={props.selectedValue} exclusive onChange={handleChange}>
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

                <ToggleButtonGroup className={classes.filterGroup} onChange={handleFilterChange} >
                    <ToggleButton aria-label="show-filter" selected={props.showFilter as boolean} disabled={props.selectedValue !== "chart"}>
                        <Tooltip title={props.showFilter ? 'Hide filter' : 'Show available filter'}>
                            <FilterListIcon />
                        </Tooltip>
                    </ToggleButton>
                </ToggleButtonGroup>


            </div>
            {
                props.selectedValue === "chart" &&
                <ChartFilter filters={props.existingFilter} onFilterChanged={props.onFilterChanged} isVisible={props.showFilter} />

            }
            {props.selectedValue === "chart" ? children[0] : props.selectedValue === "table" && children[1]}
        </>);

}

export default ToggleContainer;