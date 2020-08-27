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
import { ColumnModel, ColumnType } from './columnModel';
import { withStyles, WithStyles, createStyles, Theme } from '@material-ui/core/styles';


import TableCell from '@material-ui/core/TableCell';
import TableRow from '@material-ui/core/TableRow';
import Input from '@material-ui/core/Input';
import { Select, FormControl, InputLabel, MenuItem } from '@material-ui/core';


const styles = (theme: Theme) => createStyles({
  container: {
    display: 'flex',
    flexWrap: 'wrap',
  },
  input: {
    margin: theme.spacing(1),
  },
  numberInput: {
    float: "right"
  }
});

interface IEnhancedTableFilterComponentProps extends WithStyles<typeof styles> {
  onFilterChanged: (property: string, filterTerm: string) => void;
  filter: { [property: string]: string };
  columns: ColumnModel<{}>[];
  enableSelection?: boolean;
}

class EnhancedTableFilterComponent extends React.Component<IEnhancedTableFilterComponentProps> {
  createFilterHandler = (property: string) => (event: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    this.props.onFilterChanged && this.props.onFilterChanged(property, event.target.value);
  };

  render() {
    const { columns, filter, classes } = this.props;
    return (
      <TableRow>
        {this.props.enableSelection
          ? <TableCell padding="checkbox" style={{ width: "50px" }}>
          </TableCell>
          : null
        }
        {columns.map((col, ind) => {
          const style = col.width ? { width: col.width } : {};
          return (
            <TableCell
              className={col.type === ColumnType.numeric ? classes.numberInput : ''}
              key={col.property}
              padding={col.disablePadding ? 'none' : 'default'}
              style={style}
            >
              {col.disableFilter || (col.type === ColumnType.custom)
                ? null
                : (col.type === ColumnType.boolean)
                  ? <Select className={classes.input} aria-label={col.title ? (col.title as string).toLowerCase() + ' filter' : `${ind + 1}-filter`} value={filter[col.property] !== undefined ? filter[col.property] : ''} onChange={this.createFilterHandler(col.property)} inputProps={{ name: `${col.property}-bool`, id: `${col.property}-bool` }} >
                    <MenuItem value={undefined} aria-label="none-value" >
                      <em>None</em>
                    </MenuItem>
                    <MenuItem aria-label="true-value" value={true as any as string}>{col.labels ? col.labels["true"] : "true"}</MenuItem>
                    <MenuItem aria-label="false-value" value={false as any as string}>{col.labels ? col.labels["false"] : "false"}</MenuItem>
                  </Select>
                  : <Input className={classes.input} inputProps={{ 'aria-label': col.title ? (col.title as string).toLowerCase() + ' filter' : `${ind + 1}-filter` }} value={filter[col.property] || ''} onChange={this.createFilterHandler(col.property)} />}
            </TableCell>
          );
        }, this)}
      </TableRow>
    );
  }
}

export const EnhancedTableFilter = withStyles(styles)(EnhancedTableFilterComponent);