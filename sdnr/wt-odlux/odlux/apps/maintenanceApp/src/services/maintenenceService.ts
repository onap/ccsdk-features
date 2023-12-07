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
import { DeleteResponse, PostResponse } from '../../../../framework/src/models/elasticSearch';
import { requestRest } from '../../../../framework/src/services/restService';
import { convertPropertyNames, replaceUpperCase } from '../../../../framework/src/utilities/yangHelper';
import { MaintenanceEntry } from '../models/maintenanceEntryType';

import { convertToISODateString } from '../utils/timeUtils';


export const maintenenceEntryDatabasePath = 'mwtn/maintenancemode';

/**
 * Represents a web api accessor service for all maintenence entries related actions.
 */
class MaintenenceService {

  /**
  * Adds or updates one maintenence entry to the backend.
  */
  public async writeMaintenenceEntry(maintenenceEntry: MaintenanceEntry): Promise<PostResponse | null> {
    const path = '/rests/operations/data-provider:create-maintenance';

    const query = {
      'id': maintenenceEntry.mId,
      'node-id': maintenenceEntry.nodeId,
      'active': maintenenceEntry.active,
      'description': maintenenceEntry.description,
      'end': convertToISODateString(maintenenceEntry.end),
      'start': convertToISODateString(maintenenceEntry.start),
    };

    const result = await requestRest<PostResponse>(path, { method: 'POST', body: JSON.stringify(convertPropertyNames({ 'data-provider:input': query }, replaceUpperCase)) });
    return result || null;
  }

  /**
  * Deletes one maintenence entry by its mountId from the backend.
  */
  public async deleteMaintenenceEntry(maintenenceEntry: MaintenanceEntry): Promise<(DeleteResponse) | null> {
    const path = '/rests/operations/data-provider:delete-maintenance';

    const query = {
      'id': maintenenceEntry.mId,
      'node-id': maintenenceEntry.nodeId,
      'active': maintenenceEntry.active,
      'description': maintenenceEntry.description,
      'end': convertToISODateString(maintenenceEntry.end),
      'start': convertToISODateString(maintenenceEntry.start),
    };
    const result = await requestRest<DeleteResponse>(path, { method: 'POST', body: JSON.stringify(convertPropertyNames({ 'data-provider:input': query }, replaceUpperCase)) });
    return result || null;
  }
}

export const maintenenceService = new MaintenenceService();
export default maintenenceService;