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
import * as React from "react";

import { Connect, connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { MaterialTable, MaterialTableCtorType } from '../../../../framework/src/components/material-table';

import { InventoryType } from '../models/inventory';
import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import { createInventoryElementsProperties, createInventoryElementsActions } from "../handlers/inventoryElementsHandler";

const InventoryTable = MaterialTable as MaterialTableCtorType<InventoryType & {_id: string}>;

const mapProps = (state: IApplicationStoreState) => ({
  inventoryElementsProperties: createInventoryElementsProperties(state),
  inventoryElements: state.inventory.inventoryElements
});

const mapDispatch = (dispatcher: IDispatcher) => ({
  inventoryElementsActions: createInventoryElementsActions(dispatcher.dispatch)
});

class DashboardComponent extends React.Component<Connect<typeof mapProps, typeof mapDispatch>> {
  render() {
    return <InventoryTable title="Inventory" idProperty="_id" columns={[
      { property: "nodeId", title: "Node Name" },
      { property: "manufacturerIdentifier", title: "Manufacturer" },
      { property: "parentUuid", title: "Parent" },
      { property: "uuid", title: "Name" },
      { property: "serial", title: "Serial" },
      { property: "version", title: "Version" },
      { property: "date", title: "Date" },
      { property: "description", title: "Description" },
      { property: "partTypeId", title: "Part Type Id" },
      { property: "modelIdentifier", title: "Model Identifier" },
      { property: "typeName", title: "Type" },
      { property: "treeLevel", title: "Containment Level" },
    ]}  {...this.props.inventoryElementsActions} {...this.props.inventoryElementsProperties} >
    </InventoryTable>
  }

  componentDidMount() {
    this.props.inventoryElementsActions.onToggleFilter();
    this.props.inventoryElementsActions.onHandleRequestSort("node-id");
  }
}

export const Dashboard = connect(mapProps, mapDispatch)(DashboardComponent);
export default Dashboard;