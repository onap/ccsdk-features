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
import { convertPropertyNames, replaceHyphen } from '../../../../framework/src/utilities/yangHelper';

import { InventoryTreeNode, InventoryType } from '../models/inventory';
import { getTree, getElement } from '../fakeData';

/**
 * Represents a web api accessor service for all maintenence entries related actions.
 */
class InventoryService {
  public async getInventoryTree(mountId: string, searchTerm: string = ""): Promise<InventoryTreeNode | null> {
    //return await getTree(searchTerm);
    const path = `/tree/read-inventoryequipment-tree/${mountId}`;
    const body = {
      "query": searchTerm
    };
    const inventoryTree = await requestRest<InventoryTreeNode>(path, { method: "POST" , body: JSON.stringify(body)});
    return inventoryTree && convertPropertyNames(inventoryTree, replaceHyphen) || null;
  }

  public async getInventoryEntry(id: string): Promise<InventoryType | undefined> {
    const path = `/restconf/operations/data-provider:read-inventory-list`;
    const body = {
      "input": {
        "filter": [
          { property: "id", filtervalue: id },
        ],
        "sortorder": [],
        "pagination": {
          "size": 1,
          "page": 1
        }
      }
    };
    const inventoryTreeElement = await requestRest<{
      output: {
        "pagination": {
          "size": number,
          "page": number,
          "total": number
        },
        "data": InventoryType[]
      }
    }>(path, { method: "POST", body: JSON.stringify(body) });

    return inventoryTreeElement && inventoryTreeElement.output && inventoryTreeElement.output.pagination && inventoryTreeElement.output.pagination.total >= 1 &&
      inventoryTreeElement.output.data && convertPropertyNames(inventoryTreeElement.output.data[0], replaceHyphen) || undefined;
   // return await getElement(id);
  }

}

export const inventoryService = new InventoryService();