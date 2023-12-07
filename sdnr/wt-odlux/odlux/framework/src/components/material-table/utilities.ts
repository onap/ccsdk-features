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
import { Action, IActionHandler } from '../../flux/action';
import { Dispatch } from '../../flux/store';

import { AddErrorInfoAction } from '../../actions/errorActions';
import { IApplicationStoreState } from '../../store/applicationStore';

export const RowDisabled = Symbol("RowDisabled");
import { DataCallback } from ".";

export interface IExternalTableState<TData> {
  order: 'asc' | 'desc';
  orderBy: string | null;
  selected: any[] | null;
  hiddenColumns: string[]
  rows: (TData & { [RowDisabled]?: boolean })[];
  total: number;
  page: number;
  rowsPerPage: number;
  loading: boolean;
  showFilter: boolean;
  filter: { [property: string]: string };
  preFilter: { [property: string]: string };
}

export type ExternalMethodes<TData> = {
  reloadAction: (dispatch: Dispatch, getAppState: () => IApplicationStoreState) => Promise<void | AddErrorInfoAction>;
  createActions: (dispatch: Dispatch, skipRefresh?: boolean) => {
    onRefresh: () => void;
    onHandleRequestSort: (orderBy: string) => void;
    onHandleExplicitRequestSort: (property: string, sortOrder: "asc" | "desc") => void;
    onToggleFilter: (refresh?: boolean | undefined) => void;
    onFilterChanged: (property: string, filterTerm: string) => void;
    onHandleChangePage: (page: number) => void;
    onHandleChangeRowsPerPage: (rowsPerPage: number | null) => void;
    onHideColumns: (columnName: string[]) => void;
    onShowColumns: (columnName: string[]) => void;
    onClearFilters: () => void;
  },
 createPreActions: (dispatch: Dispatch, skipRefresh?: boolean) => {
  onPreFilterChanged: (preFilter: {
      [key: string]: string;
  }) => void;
 };
 createProperties: (state: IApplicationStoreState) => IExternalTableState<TData>;
 actionHandler: IActionHandler<IExternalTableState<TData>, Action>;
}


/** Create an actionHandler and actions for external table states. */
export function createExternal<TData>(callback: DataCallback<TData>, selectState: (appState: IApplicationStoreState) => IExternalTableState<TData>) : ExternalMethodes<TData> ;
export function createExternal<TData>(callback: DataCallback<TData>, selectState: (appState: IApplicationStoreState) => IExternalTableState<TData>, disableRow: (data: TData) => boolean) : ExternalMethodes<TData>;
export function createExternal<TData>(callback: DataCallback<TData>, selectState: (appState: IApplicationStoreState) => IExternalTableState<TData>, disableRow?: (data: TData) => boolean) : ExternalMethodes<TData> {

  //#region Actions
  abstract class TableAction extends Action { }


  class RequestSortAction extends TableAction {
    constructor(public orderBy: string) {
      super();
    }
  }

  class RequestExplicitSortAction extends TableAction {
    constructor(public propertyName: string, public sortOrder: "asc" | "desc") {
      super();
    }
  }

  class SetSelectedAction extends TableAction {
    constructor(public selected: TData[] | null) {
      super();
    }
  }

  class SetPageAction extends TableAction {
    constructor(public page: number) {
      super();
    }
  }

  class SetRowsPerPageAction extends TableAction {
    constructor(public rowsPerPage: number) {
      super();
    }
  }

  class SetPreFilterChangedAction extends TableAction {
    constructor(public preFilter: { [key: string]: string }) {
      super();
    }
  }

  class SetFilterChangedAction extends TableAction {
    constructor(public filter: { [key: string]: string }) {
      super();
    }
  }

  class SetShowFilterAction extends TableAction {
    constructor(public show: boolean) {
      super();
    }
  }

  class RefreshAction extends TableAction {
    constructor() {
      super();
    }
  }

  class SetResultAction extends TableAction {
    constructor(public result: { page: number, total: number, rows: TData[] }) {
      super();
    }
  }

  class HideColumnsAction extends TableAction{
    constructor(public property: string[]){
      super();
    }
  }

  class ShowColumnsAction extends TableAction{
    constructor(public property: string[]){
      super();
    }
  }

  // #endregion

  //#region Action Handler
  const externalTableStateInit: IExternalTableState<TData> = {
    order: 'asc',
    orderBy: null,
    selected: null,
    hiddenColumns:[],
    rows: [],
    total: 0,
    page: 0,
    rowsPerPage: 10,
    loading: false,
    showFilter: false,
    filter: {},
    preFilter: {}
  };

  const externalTableStateActionHandler: IActionHandler<IExternalTableState<TData>> = (state = externalTableStateInit, action) => {
    if (!(action instanceof TableAction)) return state;
    if (action instanceof RefreshAction) {
      state = {
        ...state,
        loading: true
      }
    } else if (action instanceof SetResultAction) {
      state = {
        ...state,
        loading: false,
        rows: disableRow 
          ? action.result.rows.map((row: TData) => ({...row, [RowDisabled]: disableRow(row) })) 
          : action.result.rows,
        total: action.result.total,
        page: action.result.page,
      }
    } else if (action instanceof RequestSortAction) {
      state = {
        ...state,
        loading: true,
        orderBy: state.orderBy === action.orderBy && state.order === 'desc' ? null : action.orderBy,
        order: state.orderBy === action.orderBy && state.order === 'asc' ? 'desc' : 'asc',
      }
    } else if (action instanceof RequestExplicitSortAction) {
      state = {
        ...state,
        loading: true,
        orderBy: action.propertyName,
        order: action.sortOrder
      }
    }
    else if (action instanceof SetShowFilterAction) {
      state = {
        ...state,
        loading: true,
        showFilter: action.show
      }
    } else if (action instanceof SetPreFilterChangedAction) {
      state = {
        ...state,
        loading: true,
        preFilter: action.preFilter
      }
    } else if (action instanceof SetFilterChangedAction) {
      state = {
        ...state,
        loading: true,
        filter: action.filter
      }
    } else if (action instanceof SetPageAction) {
      state = {
        ...state,
        loading: true,
        page: action.page
      }
    } else if (action instanceof SetRowsPerPageAction) {
      state = {
        ...state,
        loading: true,
        rowsPerPage: action.rowsPerPage
      }
    }
    else if (action instanceof HideColumnsAction){
      
      //merge arrays, remove duplicates
      const newArray = [...new Set([...state.hiddenColumns, ...action.property])]
      state = {...state, hiddenColumns: newArray};
    }
    else if(action instanceof ShowColumnsAction){

      const newArray = state.hiddenColumns.filter(el=> !action.property.includes(el));
      state = {...state, hiddenColumns: newArray};
    }

    return state;
  }

  //const createTableAction(tableAction)

  //#endregion
  const reloadAction = (dispatch: Dispatch, getAppState: () => IApplicationStoreState) => {
    dispatch(new RefreshAction());
    const ownState = selectState(getAppState());
    const filter = { ...ownState.preFilter, ...(ownState.showFilter && ownState.filter || {}) };
    return Promise.resolve(callback(ownState.page, ownState.rowsPerPage, ownState.orderBy, ownState.order, filter)).then(result => {

      if (ownState.page > 0 && ownState.rowsPerPage * ownState.page > result.total) { //if result is smaller than the currently shown page, new search and repaginate

        let newPage = Math.floor(result.total / ownState.rowsPerPage);

        Promise.resolve(callback(newPage, ownState.rowsPerPage, ownState.orderBy, ownState.order, filter)).then(result1 => {
          dispatch(new SetResultAction(result1));
        });


      } else {
        dispatch(new SetResultAction(result));
      }


    }).catch(error => dispatch(new AddErrorInfoAction(error)));
  };

  const createPreActions = (dispatch: Dispatch, skipRefresh: boolean = false) => {
    return {
      onPreFilterChanged: (preFilter: { [key: string]: string }) => {
        dispatch(new SetPreFilterChangedAction(preFilter));
        (!skipRefresh) && dispatch(reloadAction);
      }
    };
  }

  const createActions = (dispatch: Dispatch, skipRefresh: boolean = false) => {
    return {
      onRefresh: () => {
        dispatch(reloadAction);
      },
      onHandleRequestSort: (orderBy: string) => {
        dispatch((dispatch: Dispatch) => {
          dispatch(new RequestSortAction(orderBy));
          (!skipRefresh) && dispatch(reloadAction);
        });
      },
      onHandleExplicitRequestSort: (property: string, sortOrder: "asc" | "desc") => {
        dispatch((dispatch: Dispatch) => {
          dispatch(new RequestExplicitSortAction(property, sortOrder));
          (!skipRefresh) && dispatch(reloadAction);
        });
      },
      onToggleFilter: (refresh?: boolean) => {
        dispatch((dispatch: Dispatch, getAppState: () => IApplicationStoreState) => {
          const { showFilter } = selectState(getAppState());
          dispatch(new SetShowFilterAction(!showFilter));
          if (!skipRefresh && (refresh === undefined || refresh))
            dispatch(reloadAction);
        });
      },
      onFilterChanged: (property: string, filterTerm: string) => {
        dispatch((dispatch: Dispatch, getAppState: () => IApplicationStoreState) => {
          let { filter } = selectState(getAppState());
          filter = { ...filter, [property]: filterTerm };
          dispatch(new SetFilterChangedAction(filter));
          (!skipRefresh) && dispatch(reloadAction);
        });
      },
      onHandleChangePage: (page: number) => {
        dispatch((dispatch: Dispatch) => {
          dispatch(new SetPageAction(page));
          (!skipRefresh) && dispatch(reloadAction);
        });
      },
      onHandleChangeRowsPerPage: (rowsPerPage: number | null) => {
        dispatch((dispatch: Dispatch) => {
          dispatch(new SetRowsPerPageAction(rowsPerPage || 10));
          (!skipRefresh) && dispatch(reloadAction);
        });
      },
      onHideColumns: (columnName: string[]) =>{
        dispatch((dispatch: Dispatch) => {
          dispatch(new HideColumnsAction(columnName));
        })
      },
      onShowColumns: (columnName: string[]) =>{
        dispatch((dispatch: Dispatch) => {
          dispatch(new ShowColumnsAction(columnName));
        })
      },
      onClearFilters: () => {
        dispatch((dispatch: Dispatch) => {
          let filter = { };
          dispatch(new SetFilterChangedAction(filter));
        });
      },
      // selected:
    };
  };

  const createProperties = (state: IApplicationStoreState) => {
    return {
      ...selectState(state)
    }
  }

  return {
    reloadAction: reloadAction,
    createActions: createActions,
    createProperties: createProperties,
    createPreActions: createPreActions,
    actionHandler: externalTableStateActionHandler,
  }
}

