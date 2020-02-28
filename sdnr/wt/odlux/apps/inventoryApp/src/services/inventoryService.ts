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
import { requestRest } from '../../../../framework/src/services/restService';

import { InventoryTreeNode, InventoryType } from '../models/inventory';
import { getTree, getElement } from '../fakeData';

/**
 * Represents a web api accessor service for all maintenence entries related actions.
 */
class InventoryService {
  public async getInventoryTree(searchTerm?: string): Promise<InventoryTreeNode> {
    return await getTree(searchTerm);
  }

  public async getInventoryEntry(id: string): Promise<InventoryType| undefined> {
    return await getElement(id);
  }

}

export const inventoryService = new InventoryService();