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

import { convertPropertyNames, replaceHyphen } from "../../../../framework/src/utilities/yangHelper";

import { InventoryTreeNode, InventoryType } from "../models/inventory";

const data = [
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.1.1.5", "part-type-id": "3FE25774AA01", "model-identifier": "VAUIAEYAAA", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/CORE-MAIN/a2.module#5", "type-name": "a2.module", "serial": "0003548168", "id": "robot_sim_2_equipment/a2.module-1.1.1.5", "parent-uuid": "CARD-1.1.1.0", "contained-holder": ["SUBRACK-1.15.0.0"], "date": "2005-11-09T00:00:00.0Z" },
  { "manufacturer-identifier": "SAN", "version": "234", "uuid": "CARD-1.1.6.0", "part-type-id": "part-number-12", "model-identifier": "model-id-12", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/p8.module", "type-name": "p8.module", "serial": "serial-number-124", "id": "robot_sim_2_equipment/CARD-1.1.6.0", "parent-uuid": "SHELF-1.1.0.0", "contained-holder": ["PORT-1.1.6.5", "PORT-1.1.6.8", "PORT-1.1.6.7", "PORT-1.1.6.6"], "date": "2013-11-23T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.1.6.5", "part-type-id": "3EM23141AD01", "model-identifier": "CRPQABVFAA", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/p8.module/a2.module#5", "type-name": "a2.module", "serial": "310330008", "id": "robot_sim_2_equipment/a2.module-1.1.6.5", "parent-uuid": "CARD-1.1.6.0", "contained-holder": ["SUBRACK-1.65.0.0"], "date": "2013-04-13T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "2017", "uuid": "CARD-1.55.1.4", "part-type-id": "partNo2017-12", "model-identifier": "model-id-s3s", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "MWR#55Ch#1/RxDiv", "type-name": "RxDiv", "serial": "Serie2017-12", "id": "robot_sim_2_equipment/CARD-1.55.1.4", "parent-uuid": "IDU-1.55.0.0", "date": "2014-01-07T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.56.1.2", "part-type-id": "Partnumber", "model-identifier": "model-id", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "MWR#56Ch#1/a2.moduletraff", "type-name": "a2.module", "serial": "Serial1", "id": "robot_sim_2_equipment/a2.module-1.56.1.2", "parent-uuid": "ODU-1.56.0.0", "date": "2017-09-09T00:00:00.0Z" },
  { "manufacturer-identifier": "SAN", "version": "123", "uuid": "CARD-1.1.1.0", "part-type-id": "part-number-2", "model-identifier": "model-id-2", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/CORE-MAIN", "type-name": "latest", "serial": "asdf-asdasd-asd", "id": "robot_sim_2_equipment/CARD-1.1.1.0", "parent-uuid": "SHELF-1.1.0.0", "contained-holder": ["PORT-1.1.1.8", "PORT-1.1.1.7", "PORT-1.1.1.6", "PORT-1.1.1.5"], "date": "2015-08-17T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.1.1.8", "part-type-id": "1AB376720002", "model-identifier": "NGI7AMLMAA", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/CORE-MAIN/a2.module#8", "type-name": "a2.module", "serial": "01T441601301", "id": "robot_sim_2_equipment/a2.module-1.1.1.8", "parent-uuid": "CARD-1.1.1.0", "contained-holder": ["SUBRACK-1.18.0.0"], "date": "2010-02-05T00:00:00.0Z" },
  { "manufacturer-identifier": "SAN", "version": "234", "uuid": "CARD-1.1.5.0", "part-type-id": "part-number-12", "model-identifier": "model-id-12", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/p8.module", "type-name": "p8.module", "serial": "africa", "id": "robot_sim_2_equipment/CARD-1.1.5.0", "parent-uuid": "SHELF-1.1.0.0", "contained-holder": ["PORT-1.1.5.6", "PORT-1.1.5.5", "PORT-1.1.5.8", "PORT-1.1.5.7"], "date": "2013-10-21T00:00:00.0Z" },
  { "manufacturer-identifier": "", "version": "", "uuid": "a2.module-1.1.5.6", "part-type-id": "", "model-identifier": "", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/p8.module/a2.module#6", "type-name": "a2.module", "serial": "", "id": "robot_sim_2_equipment/a2.module-1.1.5.6", "parent-uuid": "CARD-1.1.5.0", "contained-holder": ["SUBRACK-1.56.0.0"] }, { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "MWR-ng", "uuid": "IDU-1.65.0.0", "part-type-id": "3DB76047BAAA02", "model-identifier": "model-id-s3s", "tree-level": 0, "node-id": "robot_sim_2_equipment", "description": "MWR-ng Dir#6.5-Ch#1", "type-name": "MWR-ng", "serial": "WAUZZI", "id": "robot_sim_2_equipment/IDU-1.65.0.0", "parent-uuid": "network-element", "contained-holder": ["PORT-1.65.1.4", "PORT-1.65.1.2"], "date": "2014-01-16T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.65.1.2", "part-type-id": "3EM23141AD01", "model-identifier": "CRPQABVFAA", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "MWR#65Ch#1/a2.moduletraff", "type-name": "a2.module", "serial": "310330008", "id": "robot_sim_2_equipment/a2.module-1.65.1.2", "parent-uuid": "IDU-1.65.0.0", "date": "2013-04-13T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.1.5.5", "part-type-id": "3EM23141AD01", "model-identifier": "CRPQABVFAA", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/p8.module/a2.module#5", "type-name": "a2.module", "serial": "310330015", "id": "robot_sim_2_equipment/a2.module-1.1.5.5", "parent-uuid": "CARD-1.1.5.0", "contained-holder": ["SUBRACK-1.55.0.0"], "date": "2013-04-13T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "unknown", "uuid": "CARD-1.1.8.0", "part-type-id": "unknown", "model-identifier": "model-id-s3s", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/DS3", "type-name": "p4.module", "serial": "sd-dsa-eqw", "id": "robot_sim_2_equipment/CARD-1.1.8.0", "parent-uuid": "SHELF-1.1.0.0", "date": "2008-10-21T00:00:00.0Z" },
  { "manufacturer-identifier": "CIT", "version": "wind", "uuid": "CARD-1.1.9.0", "part-type-id": "party-yea", "model-identifier": "model-id-s3s", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/wind", "type-name": "wind", "serial": "proto-type", "id": "robot_sim_2_equipment/CARD-1.1.9.0", "parent-uuid": "SHELF-1.1.0.0", "date": "2007-02-19T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.55.1.2", "part-type-id": "3EM23141AD01", "model-identifier": "CRPQABVFAA", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "MWR#55Ch#1/a2.moduletraff", "type-name": "a2.module", "serial": "310330015", "id": "robot_sim_2_equipment/a2.module-1.55.1.2", "parent-uuid": "IDU-1.55.0.0", "date": "2013-04-13T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "SHELF-1.1.0.0", "part-type-id": "Partnumber", "model-identifier": "model-id", "tree-level": 0, "node-id": "robot_sim_2_equipment", "description": "WS-8", "type-name": "WS-8", "serial": "Serial1", "id": "robot_sim_2_equipment/SHELF-1.1.0.0", "parent-uuid": "network-element", "contained-holder": ["SLOT-1.1.9.0", "SLOT-1.1.7.0", "SLOT-1.1.8.0", "SLOT-1.1.5.0", "SLOT-1.1.6.0", "SLOT-1.1.3.0", "SLOT-1.1.4.0", "SLOT-1.1.2.0", "SLOT-1.1.1.0"], "date": "2017-09-09T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "MWR-ng", "uuid": "IDU-1.55.0.0", "part-type-id": "3DB76047BAAA02", "model-identifier": "model-id-s3s", "tree-level": 0, "node-id": "robot_sim_2_equipment", "description": "MWR-ng Dir#5.5-Ch#1", "type-name": "MWR-ng", "serial": "Serie2017-14", "id": "robot_sim_2_equipment/IDU-1.55.0.0", "parent-uuid": "network-element", "contained-holder": ["PORT-1.55.1.2", "PORT-1.55.1.4"], "date": "2014-01-15T00:00:00.0Z" },
  { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "2017", "uuid": "CARD-1.65.1.4", "part-type-id": "partNo2017-12", "model-identifier": "model-id-s3s", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "MWR#55Ch#0/RxDiv", "type-name": "RxDiv", "serial": "Serie2017-13", "id": "robot_sim_2_equipment/CARD-1.65.1.4", "parent-uuid": "IDU-1.65.0.0", "date": "2014-01-08T00:00:00.0Z" }, { "manufacturer-identifier": "ONF-Wireless-Transport", "version": "a2.module-newest", "uuid": "a2.module-1.1.1.7", "part-type-id": "1AB187280031", "model-identifier": "mod2", "tree-level": 2, "node-id": "robot_sim_2_equipment", "description": "WS/CORE-MAIN/a2.module#7", "type-name": "a2.module", "serial": "91T403003322", "id": "robot_sim_2_equipment/a2.module-1.1.1.7", "parent-uuid": "CARD-1.1.1.0", "contained-holder": ["SUBRACK-1.17.0.0"], "date": "2009-01-19T00:00:00.0Z" },
  { "manufacturer-identifier": "CIT", "version": "p1.module", "uuid": "CARD-1.1.7.0", "part-type-id": "part-number-s3s", "model-identifier": "model-id-s3s", "tree-level": 1, "node-id": "robot_sim_2_equipment", "description": "WS/DS1", "type-name": "p1.module_A", "serial": "serial-number-s3s", "id": "robot_sim_2_equipment/CARD-1.1.7.0", "parent-uuid": "SHELF-1.1.0.0", "date": "2007-08-27T00:00:00.0Z" },
  { "manufacturer-identifier": "", "version": "extrem-hyper", "uuid": "ODU-1.56.0.0", "part-type-id": "", "model-identifier": "", "tree-level": 0, "node-id": "robot_sim_2_equipment", "description": "MWR-hyper Dir#5.6-Ch#1", "type-name": "MWR-hyper", "serial": "", "id": "robot_sim_2_equipment/ODU-1.56.0.0", "parent-uuid": "network-element", "contained-holder": ["PORT-1.56.1.3", "PORT-1.56.1.4", "PORT-1.56.1.2"] }
];

const deleay = (time: number) => () => new Promise<number>(resolve => setTimeout(resolve, time, time));

const getTreeElements = (searchTerm: string | null, treeLevel: number = 0, parentUUID: string | null = null): [InventoryTreeNode, boolean] => {
  const elements = (data.filter(e => e["tree-level"] === treeLevel && (!parentUUID || e["parent-uuid"] === parentUUID)) || [])
  let elementMatch = false;
  const treeNode = elements.reduce<InventoryTreeNode>((acc, cur) => {
    const [children, childMatch] = getTreeElements(searchTerm, treeLevel + 1, cur["uuid"]);
    const isMatch = searchTerm ? Object.keys(cur).some(k => String((cur as any)[k]).indexOf(searchTerm) > -1) : false;
    elementMatch = elementMatch || isMatch || childMatch;
    if (!searchTerm || isMatch || childMatch) {
      acc[cur["uuid"]] = {
        label: cur["uuid"],
        children: children,
        isMatch: isMatch,
      };
    }
    return acc;
  }, {});

  return [treeNode, elementMatch]
};

export const getTree = async (searchTerm: string | null = null): Promise<InventoryTreeNode> => {
  await deleay(600);
  const [node] = getTreeElements(searchTerm);
  return node;
};

export const getElement = async (id: string): Promise<InventoryType | undefined> => {
  await deleay(600);
  const res = data.find(e => e.uuid === id);
  return res && convertPropertyNames(res, replaceHyphen) as unknown as InventoryType;
};
