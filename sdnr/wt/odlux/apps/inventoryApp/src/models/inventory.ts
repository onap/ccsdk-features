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

import { ExternalTreeItem } from '../../../../framework/src/components/material-ui/treeView';

export { HitEntry, Result } from '../../../../framework/src/models';

export type InventoryType = {
  treeLevel: number;
  parentUuid: string;
  nodeId: string;
  uuid: string;
  containedHolder?: (string)[] | null;
  manufacturerName?: string;
  manufacturerIdentifier: string;
  serial: string;
  date: string;
  version: string;
  description: string;
  partTypeId: string;
  modelIdentifier: string;
  typeName: string;
};

export type InventoryTreeNode = {
  [key: string]: {
    label: string;
    children?: InventoryTreeNode;
    isMatch?: boolean;
    ownSeverity?: string;
    childrenSeveritySummary?: string;
  };
};

export type TreeDemoItem = ExternalTreeItem<string>;