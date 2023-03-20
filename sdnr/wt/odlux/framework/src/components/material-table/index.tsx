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
import { Theme } from '@mui/material/styles';

import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TablePagination from '@mui/material/TablePagination';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Checkbox from '@mui/material/Checkbox';

import { TableToolbar } from './tableToolbar';
import { EnhancedTableHead } from './tableHead';
import { EnhancedTableFilter } from './tableFilter';

import { ColumnModel, ColumnType } from './columnModel';
import { Menu, Typography } from '@mui/material';
import { DistributiveOmit } from '@mui/types';

import makeStyles from '@mui/styles/makeStyles';

import { SvgIconProps } from '@mui/material/SvgIcon';

import { DividerTypeMap } from '@mui/material/Divider';
import { MenuItemProps } from '@mui/material/MenuItem';
import { flexbox } from '@mui/system';
import { RowDisabled } from './utilities';
import { toAriaLabel } from '../../utilities/yangHelper';
export { ColumnModel, ColumnType } from './columnModel';

type propType = string | number | null | undefined | (string | number)[];
type dataType = { [prop: string]: propType };
type resultType<TData = dataType> = { page: number, total: number, rows: TData[] };

export type DataCallback<TData = dataType> = (page?: number, rowsPerPage?: number, orderBy?: string | null, order?: 'asc' | 'desc' | null, filter?: { [property: string]: string }) => resultType<TData> | Promise<resultType<TData>>;

function regExpEscape(s: string) {
  return s.replace(/[|\\{}()[\]^$+*?.]/g, '\\$&');
}

function wildcardCheck(input: string, pattern: string) {
   if (!pattern) return true; 
   const regex = new RegExp(
     (!pattern.startsWith('*') ? '^' : '') + 
     pattern.split(/\*+/).map(p => p.split(/\?+/).map(regExpEscape).join('.')).join('.*') + 
     (!pattern.endsWith('*') ? '$' : '')
   );
   return input.match(regex) !== null && input.match(regex)!.length >= 1;
}

function desc(a: dataType, b: dataType, orderBy: string) {
  if ((b[orderBy] || "") < (a[orderBy] || "")) {
    return -1;
  }
  if ((b[orderBy] || "") > (a[orderBy] || "")) {
    return 1;
  }
  return 0;
}

function stableSort(array: dataType[], cmp: (a: dataType, b: dataType) => number) {
  const stabilizedThis = array.map((el, index) => [el, index]) as [dataType, number][];
  stabilizedThis.sort((a, b) => {
    const order = cmp(a[0], b[0]);
    if (order !== 0) return order;
    return a[1] - b[1];
  });
  return stabilizedThis.map(el => el[0]);
}

function getSorting(order: 'asc' | 'desc' | null, orderBy: string) {
  return order === 'desc' ? (a: dataType, b: dataType) => desc(a, b, orderBy) : (a: dataType, b: dataType) => -desc(a, b, orderBy);
}

const styles = (theme: Theme) => createStyles({
  root: {
    width: '100%',
    overflow: "hidden",
    marginTop: theme.spacing(3),
    position: "relative",
    boxSizing: "border-box",
    display: "flex",
    flexDirection: "column",
  },
  container: {
    flex: "1 1 100%"
  },
  pagination: {
    overflow: "hidden",
    minHeight: "52px"
  }
});

const useTableRowExtStyles = makeStyles((theme: Theme) => createStyles({
  disabled: {
    color: "rgba(180, 180, 180, 0.7)",
  },
}));

type GetStatelessComponentProps<T> = T extends (props: infer P & { children?: React.ReactNode }) => any ? P : any;
type TableRowExtProps = GetStatelessComponentProps<typeof TableRow> & { disabled: boolean };
const TableRowExt : React.FC<TableRowExtProps> = (props) => {
  const [disabled, setDisabled] = React.useState(true);
  const classes = useTableRowExtStyles();
  
  const onMouseDown = (ev: React.MouseEvent<HTMLElement>) => {
      if (ev.button ===1){
        setDisabled(!disabled);  
        ev.preventDefault();
        ev.stopPropagation();
      } else if (props.disabled && disabled) {
        ev.preventDefault();
        ev.stopPropagation();
      }
  }; 

  return (   
    <TableRow {...{...props,  color: props.disabled && disabled ? '#a0a0a0' : undefined , className: props.disabled && disabled ? classes.disabled : '', onMouseDown, onContextMenu: props.disabled && disabled ? onMouseDown : props.onContextMenu } }  /> 
  );
};

export type MaterialTableComponentState<TData = {}> = {
  order: 'asc' | 'desc';
  orderBy: string | null;
  selected: any[] | null;
  rows: TData[];
  total: number;
  page: number;
  rowsPerPage: number;
  loading: boolean;
  showFilter: boolean;
  hiddenColumns: string[];
  filter: { [property: string]: string };
};

export type TableApi = { forceRefresh?: () => Promise<void> };

type MaterialTableComponentBaseProps<TData> = WithStyles<typeof styles>  & {
  className?: string;
  columns: ColumnModel<TData>[];
  idProperty: keyof TData | ((data: TData) => React.Key);
  
  //Note: used to save settings as well. Must be unique across apps. Null tableIds will not get saved to the settings
  tableId: string | null;
  isPopup?: boolean;
  title?: string;
  stickyHeader?: boolean;
  allowHtmlHeader?: boolean;
  defaultSortOrder?: 'asc' | 'desc';
  defaultSortColumn?: keyof TData;
  enableSelection?: boolean;
  disableSorting?: boolean;
  disableFilter?: boolean;
  customActionButtons?: { icon: React.ComponentType<SvgIconProps>, tooltip?: string, ariaLabel: string, onClick: () => void, disabled?: boolean }[];
  onHandleClick?(event: React.MouseEvent<HTMLTableRowElement>, rowData: TData): void;
  createContextMenu?: (row: TData) => React.ReactElement<MenuItemProps | DividerTypeMap<{}, "hr">, React.ComponentType<MenuItemProps | DividerTypeMap<{}, "hr">>>[];
};

type MaterialTableComponentPropsWithRows<TData = {}> = MaterialTableComponentBaseProps<TData> & { rows: TData[]; asynchronus?: boolean; };
type MaterialTableComponentPropsWithRequestData<TData = {}> = MaterialTableComponentBaseProps<TData> & { onRequestData: DataCallback; tableApi?: TableApi; };
type MaterialTableComponentPropsWithExternalState<TData = {}> = MaterialTableComponentBaseProps<TData> & MaterialTableComponentState & {
  onToggleFilter: () => void;
  onFilterChanged: (property: string, filterTerm: string) => void;
  onHandleChangePage: (page: number) => void;
  onHandleChangeRowsPerPage: (rowsPerPage: number | null) => void;
  onHandleRequestSort: (property: string) => void;
  onHideColumns : (columnNames: string[]) => void
  onShowColumns:  (columnNames: string[]) => void
};

type MaterialTableComponentProps<TData = {}> =
  MaterialTableComponentPropsWithRows<TData> |
  MaterialTableComponentPropsWithRequestData<TData> |
  MaterialTableComponentPropsWithExternalState<TData>;

function isMaterialTableComponentPropsWithRows(props: MaterialTableComponentProps): props is MaterialTableComponentPropsWithRows {
  return (props as MaterialTableComponentPropsWithRows).rows !== undefined && (props as MaterialTableComponentPropsWithRows).rows instanceof Array;
}

function isMaterialTableComponentPropsWithRequestData(props: MaterialTableComponentProps): props is MaterialTableComponentPropsWithRequestData {
  return (props as MaterialTableComponentPropsWithRequestData).onRequestData !== undefined && (props as MaterialTableComponentPropsWithRequestData).onRequestData instanceof Function;
}

function isMaterialTableComponentPropsWithRowsAndRequestData(props: MaterialTableComponentProps): props is MaterialTableComponentPropsWithExternalState {
  const propsWithExternalState = (props as MaterialTableComponentPropsWithExternalState)
  return propsWithExternalState.onFilterChanged instanceof Function ||
    propsWithExternalState.onHandleChangePage instanceof Function ||
    propsWithExternalState.onHandleChangeRowsPerPage instanceof Function ||
    propsWithExternalState.onToggleFilter instanceof Function ||
    propsWithExternalState.onHideColumns instanceof Function ||
    propsWithExternalState.onHandleRequestSort instanceof Function
}

// get settings in here!


class MaterialTableComponent<TData extends {} = {}> extends React.Component<MaterialTableComponentProps, MaterialTableComponentState & { contextMenuInfo: { index: number; mouseX?: number; mouseY?: number }; }> {

  constructor(props: MaterialTableComponentProps) {
    super(props);
    

    const page = isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.page : 0;
    const rowsPerPage = isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.rowsPerPage || 10 : 10;

    this.state = {
      contextMenuInfo: { index: -1 },
      filter: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.filter || {} : {},
      showFilter: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.showFilter : false,
      loading: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.loading : false,
      order: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.order : this.props.defaultSortOrder || 'asc',
      orderBy: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.orderBy : this.props.defaultSortColumn || null,
      selected: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.selected : null,
      rows: isMaterialTableComponentPropsWithRows(this.props) && this.props.rows.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage) || [],
      total: isMaterialTableComponentPropsWithRows(this.props) && this.props.rows.length || 0,
      hiddenColumns: isMaterialTableComponentPropsWithRowsAndRequestData(this.props) && this.props.hiddenColumns || [],
      page,
      rowsPerPage,
    };

    if (isMaterialTableComponentPropsWithRequestData(this.props)) {
      this.update();

      if (this.props.tableApi) {
        this.props.tableApi.forceRefresh = () => this.update();
      }
    }
  }
  render(): JSX.Element {
    const { classes, columns, allowHtmlHeader } = this.props;
    const { rows, total: rowCount, order, orderBy, selected, rowsPerPage, page, showFilter, filter } = this.state;
    const emptyRows = rowsPerPage - Math.min(rowsPerPage, rowCount - page * rowsPerPage);
    const getId = typeof this.props.idProperty !== "function" ? (data: TData) => ((data as { [key: string]: any })[this.props.idProperty as any as string] as string | number) : this.props.idProperty;
    const toggleFilter = isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.onToggleFilter : () => { !this.props.disableFilter && this.setState({ showFilter: !showFilter }, this.update) }

    const hideColumns = isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.onHideColumns : (data: string[]) => { const newArray = [...new Set([...this.state.hiddenColumns, ...data])]; this.setState({hiddenColumns:newArray}); }
    const showColumns = isMaterialTableComponentPropsWithRowsAndRequestData(this.props) ? this.props.onShowColumns : (data: string[]) => { const newArray = this.state.hiddenColumns.filter(el=> !data.includes(el));   this.setState({hiddenColumns:newArray}); }

    const allColumnsHidden = this.props.columns.length === this.state.hiddenColumns.length;
    return (
      <Paper className={this.props.className ? `${classes.root} ${this.props.className}` : classes.root}>
        <TableContainer className={classes.container}>
          <TableToolbar tableId={this.props.tableId} numSelected={selected && selected.length} title={this.props.title} customActionButtons={this.props.customActionButtons} onExportToCsv={this.exportToCsv}
            onToggleFilter={toggleFilter}
            columns={columns}
            onHideColumns={hideColumns}
            onShowColumns={showColumns} />
          <Table padding="normal" aria-label={this.props.tableId ? this.props.tableId : 'tableTitle'} stickyHeader={this.props.stickyHeader || false} >
            <EnhancedTableHead
              allowHtmlHeader={allowHtmlHeader || false}
              columns={columns}
              numSelected={selected && selected.length}
              order={order}
              orderBy={orderBy}
              onSelectAllClick={this.handleSelectAllClick}
              onRequestSort={this.onHandleRequestSort}
              rowCount={rows.length}
              enableSelection={this.props.enableSelection}
              hiddenColumns={this.state.hiddenColumns}
            />
            <TableBody>
              {showFilter && <EnhancedTableFilter columns={columns} hiddenColumns={this.state.hiddenColumns} filter={filter} onFilterChanged={this.onFilterChanged} enableSelection={this.props.enableSelection} /> || null}
              
              {allColumnsHidden ? <Typography variant="body1" textAlign="center">All columns of this table are hidden.</Typography> :
              
              rows // may need ordering here
                .map((entry: TData & { [RowDisabled]?: boolean, [kex: string]: any }, index) => {
                  const entryId = getId(entry);
                  const contextMenu = (this.props.createContextMenu && this.state.contextMenuInfo.index === index && this.props.createContextMenu(entry)) || null;
                  const isSelected = this.isSelected(entryId) || this.state.contextMenuInfo.index === index;
                  return (
                    <TableRowExt
                      hover
                      onClick={event => {
                        if (this.props.createContextMenu) {
                          this.setState({
                            contextMenuInfo: {
                              index: -1
                            }
                          });
                        }
                        this.handleClick(event, entry, entryId);
                      }}
                      onContextMenu={event => {
                        if (this.props.createContextMenu) {
                          event.preventDefault();
                          event.stopPropagation();
                          this.setState({ contextMenuInfo: { index, mouseX: event.clientX - 2, mouseY: event.clientY - 4 } });
                        }
                      }}
                      role="checkbox"
                      aria-checked={isSelected}
                      aria-label="table-row"
                      tabIndex={-1}
                      key={entryId}
                      selected={isSelected}
                      disabled={entry[RowDisabled] || false}
                    >
                      {this.props.enableSelection
                        ? <TableCell padding="checkbox" style={{ width: "50px", color:  entry[RowDisabled] || false ? "inherit" : undefined } }>
                          <Checkbox color='secondary' checked={isSelected} />
                        </TableCell>
                        : null
                      }
                      {
                        
                        this.props.columns.map(
                          col => {
                            const style = col.width ? { width: col.width } : {};
                            const tableCell = (

                              <TableCell style={ entry[RowDisabled] || false ? { ...style, color: "inherit"  } : style } aria-label={col.title? toAriaLabel(col.title) : toAriaLabel(col.property)} key={col.property} align={col.type === ColumnType.numeric && !col.align ? "right" : col.align} >
                                {col.type === ColumnType.custom && col.customControl
                                  ? <col.customControl className={col.className} style={col.style} rowData={entry} />
                                  : col.type === ColumnType.boolean
                                    ? <span className={col.className} style={col.style}>{col.labels ? col.labels[entry[col.property] ? "true" : "false"] : String(entry[col.property])}</span>
                                    : <span className={col.className} style={col.style}>{String(entry[col.property])}</span>
                                }
                              </TableCell>
                            );
                            
                            //show column if...
                            const showColumn = !this.state.hiddenColumns.includes(col.property);
                            return showColumn && tableCell
                          }
                        )
                      }
                      {<Menu open={!!contextMenu} onClose={() => this.setState({ contextMenuInfo: { index: -1 } })} anchorReference="anchorPosition" keepMounted
                        anchorPosition={this.state.contextMenuInfo.mouseY != null && this.state.contextMenuInfo.mouseX != null ? { top: this.state.contextMenuInfo.mouseY, left: this.state.contextMenuInfo.mouseX } : undefined}>
                        {contextMenu}
                      </Menu> || null}
                    </TableRowExt>
                  );
                })}
              {emptyRows > 0 && (
                <TableRow style={{ height: 49 * emptyRows }}>
                  <TableCell colSpan={this.props.columns.length} />
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination className={classes.pagination}
          rowsPerPageOptions={[5, 10, 20, 50]}
          component="div"
          count={rowCount}
          rowsPerPage={rowsPerPage}
          page={page}
          aria-label={this.props.isPopup ? "popup-table-pagination-footer" : "table-pagination-footer" }
          backIconButtonProps={{
            'aria-label': this.props.isPopup ? 'popup-previous-page' : 'previous-page',
          }}
          nextIconButtonProps={{
            'aria-label': this.props.isPopup ? 'popup-next-page': 'next-page',
          }}
          onPageChange={this.onHandleChangePage}
          onRowsPerPageChange={this.onHandleChangeRowsPerPage}
        />
      </Paper>
    );
  }

  static getDerivedStateFromProps(props: MaterialTableComponentProps, state: MaterialTableComponentState & { _rawRows: {}[] }): MaterialTableComponentState & { _rawRows: {}[] } {
   
    if (isMaterialTableComponentPropsWithRowsAndRequestData(props)) {
      return {
        ...state,
        rows: props.rows,
        total: props.total,
        orderBy: props.orderBy,
        order: props.order,
        filter: props.filter,
        loading: props.loading,
        showFilter: props.showFilter,
        page: props.page,
        hiddenColumns: props.hiddenColumns,
        rowsPerPage: props.rowsPerPage
      }
    } else if (isMaterialTableComponentPropsWithRows(props) && props.asynchronus && state._rawRows !== props.rows) {
      const newState = MaterialTableComponent.updateRows(props, state);
      return {
        ...state,
        ...newState,
        _rawRows: props.rows || []
      };
    }
    return state;
  }

  private static updateRows(props: MaterialTableComponentPropsWithRows, state: MaterialTableComponentState): { rows: {}[], total: number, page: number } {

    let data = [...(props.rows as dataType[] || [])];
    const columns = props.columns;

    const { page, rowsPerPage, order, orderBy, filter } = state;

    try {
      if (state.showFilter) {
        Object.keys(filter).forEach(prop => {
          const column = columns.find(c => c.property === prop);
          const filterExpression = filter[prop];

          if (!column) throw new Error("Filter for not existing column found.");

          if (filterExpression != null) {
            data = data.filter((val) => {
              const dataValue = val[prop];

              if (dataValue != null) {

                if (column.type === ColumnType.boolean) {

                  const boolDataValue = JSON.parse(String(dataValue).toLowerCase());
                  const boolFilterExpression = JSON.parse(String(filterExpression).toLowerCase());
                  return boolDataValue == boolFilterExpression;

                } else if (column.type === ColumnType.text) {

                  const valueAsString = String(dataValue);
                  const filterExpressionAsString = String(filterExpression).trim();
                  if (filterExpressionAsString.length === 0) return true;
                  return wildcardCheck(valueAsString, filterExpressionAsString);

                } else if (column.type === ColumnType.numeric){
                  
                  const valueAsNumber = Number(dataValue);
                  const filterExpressionAsString = String(filterExpression).trim();
                  if (filterExpressionAsString.length === 0 || isNaN(valueAsNumber)) return true;
                  
                  if (filterExpressionAsString.startsWith('>=')) {
                    return valueAsNumber >= Number(filterExpressionAsString.substring(2).trim());
                  } else if (filterExpressionAsString.startsWith('<=')) {
                    return valueAsNumber <= Number(filterExpressionAsString.substring(2).trim());
                  } else if (filterExpressionAsString.startsWith('>')) {
                    return valueAsNumber > Number(filterExpressionAsString.substring(1).trim());
                  } else if (filterExpressionAsString.startsWith('<')) {
                    return valueAsNumber < Number(filterExpressionAsString.substring(1).trim());
                  }
                } else if (column.type === ColumnType.date){
                   const valueAsString = String(dataValue);

                   const convertToDate = (valueAsString: string) => {
                    // time value needs to be padded   
                    const hasTimeValue = /T\d{2,2}/.test(valueAsString);
                    const indexCollon =  valueAsString.indexOf(':');
                        if (hasTimeValue && (indexCollon === -1 || indexCollon >= valueAsString.length-2)) {
                            valueAsString = indexCollon === -1 
                            ? valueAsString + ":00"
                            : indexCollon === valueAsString.length-1
                                ? valueAsString + "00"
                                : valueAsString += "0"
                        }
                     return new Date(Date.parse(valueAsString));   
                   };
                   
                   // @ts-ignore
                   const valueAsDate = new Date(Date.parse(dataValue));
                   const filterExpressionAsString = String(filterExpression).trim();             

                   if (filterExpressionAsString.startsWith('>=')) {
                    return valueAsDate >= convertToDate(filterExpressionAsString.substring(2).trim());
                  } else if (filterExpressionAsString.startsWith('<=')) {
                    return valueAsDate <= convertToDate(filterExpressionAsString.substring(2).trim());
                  } else if (filterExpressionAsString.startsWith('>')) {
                    return valueAsDate > convertToDate(filterExpressionAsString.substring(1).trim());
                  } else if (filterExpressionAsString.startsWith('<')) {
                    return valueAsDate < convertToDate(filterExpressionAsString.substring(1).trim());
                  }

                  
                  if (filterExpressionAsString.length === 0) return true;
                  return wildcardCheck(valueAsString, filterExpressionAsString);

                }
              }

              return (dataValue == filterExpression)
            });
          };
        });
      }

      const rowCount = data.length;

      if (page > 0 && rowsPerPage * page > rowCount) { //if result is smaller than the currently shown page, new search and repaginate
        let newPage = Math.floor(rowCount / rowsPerPage);
        return {
          rows: data,
          total: rowCount,
          page: newPage
        };
      } else {
        data = (orderBy && order
          ? stableSort(data, getSorting(order, orderBy))
          : data).slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage);

        return {
          rows: data,
          total: rowCount,
          page: page
        };
      }


    } catch (e) {
      console.error(e);
      return {
        rows: [],
        total: 0,
        page: page
      }
    }
  }

  private async update() {
    if (isMaterialTableComponentPropsWithRequestData(this.props)) {
      const response = await Promise.resolve(
        this.props.onRequestData(
          this.state.page, this.state.rowsPerPage, this.state.orderBy, this.state.order, this.state.showFilter && this.state.filter || {})
      );
      this.setState(response);
    } else {
      let updateResult = MaterialTableComponent.updateRows(this.props, this.state);
      this.setState(updateResult);
    }
  }

  private onFilterChanged = (property: string, filterTerm: string) => {
    if (isMaterialTableComponentPropsWithRowsAndRequestData(this.props)) {
      this.props.onFilterChanged(property, filterTerm);
      return;
    }
    if (this.props.disableFilter) return;
    const colDefinition = this.props.columns && this.props.columns.find(col => col.property === property);
    if (colDefinition && colDefinition.disableFilter) return;

    const filter = { ...this.state.filter, [property]: filterTerm };
    this.setState({
      filter
    }, this.update);
  };

  private onHandleRequestSort = (event: React.SyntheticEvent, property: string) => {
    if (isMaterialTableComponentPropsWithRowsAndRequestData(this.props)) {
      this.props.onHandleRequestSort(property);
      return;
    }
    if (this.props.disableSorting) return;
    const colDefinition = this.props.columns && this.props.columns.find(col => col.property === property);
    if (colDefinition && colDefinition.disableSorting) return;

    const orderBy = this.state.orderBy === property && this.state.order === 'desc' ? null : property;
    const order = this.state.orderBy === property && this.state.order === 'asc' ? 'desc' : 'asc';
    this.setState({
      order,
      orderBy
    }, this.update);
  };

  handleSelectAllClick: () => {};

  private onHandleChangePage = (event: any | null, page: number) => {
    if (isMaterialTableComponentPropsWithRowsAndRequestData(this.props)) {
      this.props.onHandleChangePage(page);
      return;
    }
    this.setState({
      page
    }, this.update);
  };

  private onHandleChangeRowsPerPage = (event: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>) => {
    if (isMaterialTableComponentPropsWithRowsAndRequestData(this.props)) {
      this.props.onHandleChangeRowsPerPage(+(event && event.target.value));
      return;
    }
    const rowsPerPage = +(event && event.target.value);
    if (rowsPerPage && rowsPerPage > 0) {
      this.setState({
        rowsPerPage
      }, this.update);
    }
  };

  private isSelected(id: string | number): boolean {
    let selected = this.state.selected || [];
    const selectedIndex = selected.indexOf(id);
    return (selectedIndex > -1);
  }

  private handleClick(event: any, rowData: TData, id: string | number): void {
    if (this.props.onHandleClick instanceof Function) {
      this.props.onHandleClick(event, rowData);
      return;
    }
    if (!this.props.enableSelection) {
      return;
    }
    let selected = this.state.selected || [];
    const selectedIndex = selected.indexOf(id);
    if (selectedIndex > -1) {
      selected = [
        ...selected.slice(0, selectedIndex),
        ...selected.slice(selectedIndex + 1)
      ];
    } else {
      selected = [
        ...selected,
        id
      ];
    }
    this.setState({
      selected
    });
  }


  private exportToCsv = async () => {
    let file;
    let data: dataType[] | null = null;
    let csv: string[] = [];

    if (isMaterialTableComponentPropsWithRequestData(this.props)) {
      // table with extra request handler
      this.setState({ loading: true });
      const result = await Promise.resolve(
        this.props.onRequestData(0, 1000, this.state.orderBy, this.state.order, this.state.showFilter && this.state.filter || {})
      );
      data = result.rows;
      this.setState({ loading: true });
    } else if (isMaterialTableComponentPropsWithRowsAndRequestData(this.props)) {
      // table with generated handlers note: exports data shown on current page
      data = this.props.rows;
    }
    else {
      // table with local data
      data = MaterialTableComponent.updateRows(this.props, this.state).rows;
    }

    if (data && data.length > 0) {
      csv.push(this.props.columns.map(col => col.title || col.property).join(',') + "\r\n");
      this.state.rows && this.state.rows.forEach((row: any) => {
        csv.push(this.props.columns.map(col => row[col.property]).join(',') + "\r\n");
      });
      const properties = { type: "text/csv;charset=utf-8" }; // Specify the file's mime-type.
      try {
        // Specify the filename using the File constructor, but ...
        file = new File(csv, "export.csv", properties);
      } catch (e) {
        // ... fall back to the Blob constructor if that isn't supported.
        file = new Blob(csv, properties);
      }
    }
    if (!file) return;
    var reader = new FileReader();
    reader.onload = function (e) {
      const dataUri = reader.result as any;
      const link = document.createElement("a");
      if (typeof link.download === 'string') {
        link.href = dataUri;
        link.download = "export.csv";

        //Firefox requires the link to be in the body
        document.body.appendChild(link);

        //simulate click
        link.click();

        //remove the link when done
        document.body.removeChild(link);
      } else {
        window.open(dataUri);
      }
    }
    reader.readAsDataURL(file);

    // const url = URL.createObjectURL(file);
    // window.location.replace(url);
  }
}

export type MaterialTableCtorType<TData extends {} = {}> = new () => React.Component<DistributiveOmit<MaterialTableComponentProps<TData>, 'classes'>>;

export const MaterialTable = withStyles(styles)(MaterialTableComponent);
export default MaterialTable;