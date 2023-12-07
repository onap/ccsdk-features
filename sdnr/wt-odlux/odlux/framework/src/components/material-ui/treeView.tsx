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

import { makeStyles, WithStyles, WithTheme } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';

import { List, ListItem, TextField, ListItemText, ListItemIcon, Typography } from '@mui/material';
import { DistributiveOmit } from '@mui/types';

import withTheme from '@mui/styles/withTheme';

import { SvgIconProps } from '@mui/material/SvgIcon';
import FileIcon from '@mui/icons-material/InsertDriveFile';
import CloseIcon from '@mui/icons-material/ExpandLess';
import OpenIcon from '@mui/icons-material/ExpandMore';
import FolderIcon from '@mui/icons-material/Folder';

declare module '@mui/styles/defaultTheme' {
  // eslint-disable-next-line @typescript-eslint/no-empty-interface (remove this line if you don't have the rule enabled)
  interface DefaultTheme extends Theme {}
}

const styles = (theme: Theme) => createStyles({
  root: {
    padding: 0,
    paddingBottom: 8,
    paddingTop: 8,
  },
  search: {
    padding: `0px ${theme.spacing(1)}`
  }
});

export enum SearchMode {
  OnKeyDown = 1,
  OnEnter =2
}

export type TreeItem<TData = { }> = {
  disabled?: boolean;
  icon?: React.ComponentType<SvgIconProps>;
  iconClass?: string;
  content: string;
  contentClass?: string;
  children?: TreeItem<TData>[];
  value?: TData;
}

export type ExternalTreeItem<TData = {}> = TreeItem<TData> & {
  isMatch?: boolean;
}


type TreeViewComponentState<TData = { }> = {
  /** All indices of all expanded Items */
  expandedItems: ExternalTreeItem<TData>[];
  /** The index of the active iten or undefined if no item is active. */
  activeItem?: ExternalTreeItem<TData>;
  /** The search term or undefined if search is currently not active. */
  searchTerm?: string;
  searchTermValue?: string;
}

type TreeViewComponentBaseProps<TData = {}> = WithTheme & WithStyles<typeof styles> & {
  className?: string;
  items: TreeItem<TData>[];
  useFolderIcons?: boolean;
  enableSearchBar?: boolean;
  autoExpandFolder?: boolean;
  style?: React.CSSProperties;
  itemHeight?: number;
  depthOffset?: number;
  searchMode?: SearchMode;

}

type TreeViewComponentWithInternalStateProps<TData = { }> = TreeViewComponentBaseProps<TData> & {
  initialSearchTerm? : string;
  onItemClick?: (item: TreeItem<TData>) => void;
  onFolderClick?: (item: TreeItem<TData>) => void;
}

type TreeViewComponentWithExternalSearchProps<TData = {}> = TreeViewComponentBaseProps<TData> & {
  items: ExternalTreeItem<TData>[];
  initialSearchTerm? : string;
  searchTerm: string;
  onSearch: (searchTerm: string) => void;
  onItemClick?: (item: TreeItem<TData>) => void;
  onFolderClick?: (item: TreeItem<TData>) => void;
}

type TreeViewComponentWithExternalStateProps<TData = {}> = TreeViewComponentBaseProps<TData> & TreeViewComponentState<TData> & {
  items: ExternalTreeItem<TData>[];
  initialSearchTerm? : string;
  searchTerm: string;
  onSearch: (searchTerm: string) => void;
  onItemClick: (item: TreeItem<TData>) => void;
  onFolderClick: (item: TreeItem<TData>) => void;
}

type TreeViewComponentProps<TData = { }> =
  | TreeViewComponentWithInternalStateProps<TData>
  | TreeViewComponentWithExternalSearchProps<TData>
  | TreeViewComponentWithExternalStateProps<TData>;

function isTreeViewComponentWithExternalSearchProps(props: TreeViewComponentProps): props is TreeViewComponentWithExternalSearchProps {
  const propsWithExternalState = (props as TreeViewComponentWithExternalStateProps)
  return (
    propsWithExternalState.onSearch instanceof Function &&
    propsWithExternalState.onFolderClick === undefined &&
    propsWithExternalState.expandedItems === undefined &&
    propsWithExternalState.searchTerm !== undefined
  );
}

function isTreeViewComponentWithExternalStateProps(props: TreeViewComponentProps): props is TreeViewComponentWithExternalStateProps {
  const propsWithExternalState = (props as TreeViewComponentWithExternalStateProps)
  return (
    propsWithExternalState.onSearch instanceof Function &&
    propsWithExternalState.onFolderClick instanceof Function &&
    propsWithExternalState.expandedItems !== undefined &&
    propsWithExternalState.searchTerm !== undefined
  );
}

class TreeViewComponent<TData = { }> extends React.Component<TreeViewComponentProps<TData>, TreeViewComponentState<TData>> {

  /**
    * Initializes a new instance.
    */
  constructor(props: TreeViewComponentProps<TData>) {
    super(props);

    this.state = {
      expandedItems: [],
      activeItem: undefined,
      searchTerm: undefined,
      searchTermValue: props.initialSearchTerm
    };
  }

  render(): JSX.Element {
    this.itemIndex = 0;
    const { searchTerm , searchTermValue} = this.state;
    const { children, items, enableSearchBar } = this.props;

    return (
      <div className={this.props.className ? `${this.props.classes.root} ${this.props.className}` : this.props.classes.root} style={this.props.style}>
        {children}
        {enableSearchBar && <TextField variant="standard" label={"Search"} inputProps={{'aria-label': 'treeview-searchfield'}} fullWidth={true} className={this.props.classes.search} value={searchTermValue} onKeyDown={this.onSearchKeyDown} onChange={this.onChangeSearchText} /> || null}
        {enableSearchBar && (searchTerm === undefined || searchTerm.length===0 )&& <Typography style={{marginTop:'10px'}}>Please search for an inventory identifier or use *.</Typography>}
        <List>
          {this.renderItems(items, searchTerm && searchTerm.toLowerCase())}
        </List>
      </div>
    );
  }

  private itemIndex: number = 0;
  private renderItems = (items: TreeItem<TData>[], searchTerm: string | undefined, depth: number = 1, forceRender: boolean = true) => {

    return items.reduce((acc, item) => {

      const children = item.children; // this.props.childrenProperty && ((item as any)[this.props.childrenProperty] as TData[]);
      const childrenJsx = children && this.renderItems(children, searchTerm, depth + 1, this.state.expandedItems.indexOf(item) > -1);

      const expanded = !isTreeViewComponentWithExternalStateProps(this.props) && searchTerm
        ? childrenJsx && childrenJsx.length > 0
        : !children
          ? false
          : this.state.expandedItems.indexOf(item) > -1;
      const isFolder = children !== undefined;

      const itemJsx = this.renderItem(item, searchTerm, depth, isFolder, expanded || false, forceRender);
      itemJsx && acc.push(itemJsx);

      if (isFolder && expanded && childrenJsx) {
        acc.push(...childrenJsx);
      }
      return acc;

    }, [] as JSX.Element[]);
  }
  private renderItem = (item: ExternalTreeItem<TData> , searchTerm: string | undefined, depth: number, isFolder: boolean, expanded: boolean, forceRender: boolean): JSX.Element | null => {  
    const styles = {
      item: {
        paddingLeft: (((this.props.depthOffset || 0) + depth) * Number(this.props.theme.spacing(3).replace("px", ''))),
        backgroundColor: this.state.activeItem === item ? this.props.theme.palette.action.selected : undefined,
        height: this.props.itemHeight || undefined,
        cursor: item.disabled ? 'not-allowed' : 'pointer',
        color: item.disabled ? this.props.theme.palette.text.disabled : this.props.theme.palette.text.primary,
        overflow: 'hidden',
        transform: 'translateZ(0)',
      }
    };

    const text = item.content || ''; // need to keep track of search
    const search_array = searchTerm?.split("*");
    const index = search_array?.findIndex(function (_str: String) {
      return _str.length > 0;
    }) || 0;
    const firstSearchSubString = search_array ? search_array[index] : "";
    const matchIndex = firstSearchSubString ? text.toLowerCase().indexOf(firstSearchSubString) : -1;

    const hasStarInSearch = search_array ? search_array.length > 1 : false;
    const isSearchStringWithStar = hasStarInSearch && firstSearchSubString?.length > 0 || false;

    const searchTermLength = firstSearchSubString && firstSearchSubString.length || 0;

    const handleClickCreator = (isIcon: boolean) => (event: React.SyntheticEvent) => {
      if (item.disabled) return;
      event.preventDefault();
      event.stopPropagation();
      if (isFolder && (this.props.autoExpandFolder || isIcon)) {
        this.props.onFolderClick ? this.props.onFolderClick(item) : this.onFolderClick(item);
      } else {
        this.props.onItemClick ? this.props.onItemClick(item) : this.onItemClick(item);
      }
    };

    return ((searchTerm && (matchIndex > -1 || expanded || (!isTreeViewComponentWithExternalStateProps(this.props) && item.isMatch || depth === 1)) || !searchTerm || forceRender)
      ? (
        <ListItem key={`tree-list-${this.itemIndex++}`} aria-label="tree-view-item" style={styles.item} onClick={handleClickCreator(false)} button >

          { // display the left icon
            (this.props.useFolderIcons && <ListItemIcon>{isFolder ? <FolderIcon /> : <FileIcon />}</ListItemIcon>) ||
            (item.icon && (<ListItemIcon className={ item.iconClass }><item.icon /></ListItemIcon>))}


          { // highlight search result
            isSearchStringWithStar && matchIndex > -1
              ? <ListItemText className={item.contentClass} primary={(
                <span
                  style={{
                    display: 'inline-block',
                    backgroundColor: 'rgba(255,235,59,0.5)',
                    padding: '3px',
                  }}
                >
                  {text}
                </span>)} />
              : matchIndex > -1
                ? <ListItemText className={item.contentClass} primary={(<span>
                  {text.substring(0, matchIndex)}
                  <span
                    style={{
                      display: 'inline-block',
                      backgroundColor: 'rgba(255,235,59,0.5)',
                      padding: '3px',
                    }}
                  >
                    {text.substring(matchIndex, matchIndex + searchTermLength)}
                  </span>
                  {text.substring(matchIndex + searchTermLength)}
                </span>)} />
                : <ListItemText className={item.contentClass} primary={(
                  <span style={item.isMatch ? {
                    display: 'inline-block',
                    padding: '3px',
                  } : undefined}>
                    {text} </span>
                )} />
          }

          { // display the right icon, depending on the state
            !isFolder ? null : expanded ? (<OpenIcon onClick={handleClickCreator(true)} />) : (<CloseIcon onClick={handleClickCreator(true)} />)}
        </ListItem>
      )
      : null
    );
  }

  private onFolderClick = (item: TreeItem<TData>) => {
    // toggle items with children
    if (this.state.searchTerm) return;
    const indexOfItemToToggle = this.state.expandedItems.indexOf(item);
    if (indexOfItemToToggle === -1) {
      this.setState({
        expandedItems: [...this.state.expandedItems, item],
      });
    } else {
      this.setState({
        expandedItems: [
          ...this.state.expandedItems.slice(0, indexOfItemToToggle),
          ...this.state.expandedItems.slice(indexOfItemToToggle + 1),
        ]
      });
    }
  };

  private onItemClick = (item: TreeItem<TData>) => {
    // activate items without children
    this.setState({
      activeItem: item,
    });
  };

  private onSearchKeyDown = (event: React.KeyboardEvent<HTMLInputElement>) => {
    const enterMode = this.props.searchMode === SearchMode.OnEnter;

    if (enterMode && event.keyCode === 13) {
      event.preventDefault();
      event.stopPropagation();

      enterMode && this.setState({
        searchTerm: this.state.searchTermValue
      });

      if (isTreeViewComponentWithExternalSearchProps(this.props) || isTreeViewComponentWithExternalStateProps(this.props)) {
        this.props.onSearch(this.state.searchTermValue || "");
      }
    }
  }

  private onChangeSearchText = (event: React.ChangeEvent<HTMLInputElement>) => {
    event.preventDefault();
    event.stopPropagation();

    const keyDownMode = (!this.props.searchMode || this.props.searchMode === SearchMode.OnKeyDown);

    this.setState(keyDownMode
      ? {
        searchTerm: event.target.value,
        searchTermValue: event.target.value,
      } as any : {
        searchTermValue: event.target.value,
      }) as any;

    if ((isTreeViewComponentWithExternalSearchProps(this.props) || isTreeViewComponentWithExternalStateProps(this.props)) && keyDownMode) {
      this.props.onSearch(event.target.value);
    }
  };

  static getDerivedStateFromProps(props: TreeViewComponentProps, state: TreeViewComponentState): TreeViewComponentState {
    if (isTreeViewComponentWithExternalStateProps(props)) {
      return {
        ...state,
        expandedItems: props.expandedItems || [],
        activeItem: props.activeItem,
        searchTerm: props.searchTerm
      };
    } else if (isTreeViewComponentWithExternalSearchProps(props)) {
      return {
        ...state,
        searchTerm: props.searchTerm,
      };
    }
    return state;
  }

  public static defaultProps = {
    useFolderIcons: false,
    enableSearchBar: false,
    autoExpandFolder: false,
    depthOffset: 0
  }
}

export type TreeViewCtorType<TData = { }> = new () => React.Component<DistributiveOmit<TreeViewComponentProps<TData>, 'theme'|'classes'>>;

export const TreeView = withTheme(withStyles(styles)(TreeViewComponent));
export default TreeView;