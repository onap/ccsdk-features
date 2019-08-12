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

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import connect, { Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { TreeView, TreeViewCtorType } from '../../../../framework/src/components/material-ui/treeView';

import { ListItemText } from '@material-ui/core';

import { NavigateToApplication } from '../../../../framework/src/actions/navigationActions';

import { TocTreeNode } from '../models/tocNode';

const TocTree = TreeView as any as TreeViewCtorType<TocTreeNode>;

const mapProps = (state: IApplicationStoreState) => ({
  helpToc: state.help.toc,
  helpBusy: state.help.busy
});

const mapDisp = (dispatcher: IDispatcher) => ({
  requestDocument: (node: TocTreeNode) => dispatcher.dispatch(new NavigateToApplication("help", node.uri))
});

const SubMenuEntryComponent: React.SFC<Connect<typeof mapProps, typeof mapDisp>> = (props) => {
  return props.helpToc
  ? (
    <TocTree items={ props.helpToc } contentProperty={ "label" } childrenProperty={ "nodes" } depthOffset={ 1 }
        useFolderIcons={ false } enableSearchBar={ false } onItemClick={ props.requestDocument } />
    )
  : (
    <ListItemText >Loading ...</ListItemText>
  )
};

export const SubMenuEntry = connect(mapProps, mapDisp)(SubMenuEntryComponent);
export default SubMenuEntry;

