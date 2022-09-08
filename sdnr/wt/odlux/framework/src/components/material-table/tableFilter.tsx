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
import { Theme } from '@mui/material/styles';


import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';


import TableCell from '@mui/material/TableCell';
import TableRow from '@mui/material/TableRow';
import Input from '@mui/material/Input';
import { Select, FormControl, InputLabel, MenuItem, SelectChangeEvent } from '@mui/material';
import { toAriaLabel } from '../../utilities/yangHelper';


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
  hiddenColumns: string[];
  enableSelection?: boolean;
}

class EnhancedTableFilterComponent extends React.Component<IEnhancedTableFilterComponentProps> {
  createSelectFilterHandler = (property: string) => (event: SelectChangeEvent<HTMLSelectElement | string>) => {
    this.props.onFilterChanged && this.props.onFilterChanged(property, event.target.value as string);
  };
  createInputFilterHandler = (property: string) => (event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => {
    this.props.onFilterChanged && this.props.onFilterChanged(property, event.currentTarget.value);
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
          const tableCell = (
            <TableCell
              className={col.type === ColumnType.numeric ? classes.numberInput : ''}
              key={col.property}
              padding={col.disablePadding ? 'none' : 'normal'}
              style={style}
            >
              {col.disableFilter || (col.type === ColumnType.custom)
                ? null
                : (col.type === ColumnType.boolean)
                  ? <Select variant="standard" className={classes.input} aria-label={col.title ? toAriaLabel(col.title as string) + '-filter' : `${ind + 1}-filter`}
                    value={filter[col.property] !== undefined ? filter[col.property] : ''}
                    onChange={this.createSelectFilterHandler(col.property)}
                    inputProps={{ name: `${col.property}-bool`, id: `${col.property}-bool` }} >
                    <MenuItem value={undefined} aria-label="none-value" >
                      <em>None</em>
                    </MenuItem>
                    <MenuItem aria-label="true-value" value={true as any as string}>{col.labels ? col.labels["true"] : "true"}</MenuItem>
                    <MenuItem aria-label="false-value" value={false as any as string}>{col.labels ? col.labels["false"] : "false"}</MenuItem>
                  </Select>
                  : <Input className={classes.input}
                    inputProps={{ 'aria-label': col.title ? toAriaLabel(col.title as string) + '-filter' : `${ind + 1}-filter` }}
                    value={filter[col.property] || ''}
                    onChange={this.createInputFilterHandler(col.property)} />}
            </TableCell>
          );

          const showColumn = !this.props.hiddenColumns.includes(col.property);

          return showColumn && tableCell;
        }, this)}
      </TableRow>
    );
  }
}

export const EnhancedTableFilter = withStyles(styles)(EnhancedTableFilterComponent);