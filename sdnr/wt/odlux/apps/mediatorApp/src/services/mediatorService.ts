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
import * as $ from 'jquery';

import { requestRest, formEncode } from '../../../../framework/src/services/restService';
import { MediatorServer, MediatorServerVersionInfo, MediatorConfig, MediatorServerDevice, MediatorConfigResponse } from '../models/mediatorServer';
import { HitEntry } from '../../../../framework/src/models';

export const mediatorServerResourcePath = "mwtn/mediator-server";

type MediatorServerResponse<TData> = { code: number, data: TData };
type IndexableMediatorServer = MediatorServer & { [key: string]: any; };

/** 
 * Represents a web api accessor service for all mediator server actions.
 */
class MediatorService {
  /**
    * Inserts data into the mediator servers table.
    */
  public async insertMediatorServer(server: IndexableMediatorServer): Promise<MediatorServer | null> {
    const path = `database/${mediatorServerResourcePath}`;
    const data = Object.keys(server).reduce((acc, cur) => {
      if (cur !== "_id") acc[cur] = server[cur];
      return acc;
    }, {} as IndexableMediatorServer);
    const result = await requestRest<MediatorServer>(path, { method: "POST", body: JSON.stringify(data) });
    return result || null;
  }

  /**
    * Updates data into the mediator servers table.
    */
  public async updateMediatorServer(server: IndexableMediatorServer): Promise<MediatorServer | null> {
    const path = `database/${mediatorServerResourcePath}/${server._id}`;
    const data = Object.keys(server).reduce((acc, cur) => {
      if (cur !== "_id") { acc[cur] = server[cur] } else { acc["id"] = 0 };
      return acc;
    }, {} as IndexableMediatorServer);
    const result = await requestRest<MediatorServer>(path, { method: "PUT", body: JSON.stringify(data) });
    return result || null;
  }

  /**
    * Deletes data from the mediator servers table.
    */
  public async deleteMediatorServer(server: MediatorServer): Promise<MediatorServer | null> {
    const path = `database/${mediatorServerResourcePath}/${server._id}`;
    const result = await requestRest<MediatorServer>(path, { method: "DELETE" });
    return result || null;
  }

  public async getMediatorServerById(serverId: string): Promise<MediatorServer | null> {
    const path = `database/${mediatorServerResourcePath}/${serverId}`;
    const result = await requestRest<HitEntry<MediatorServer> & { found: boolean }>(path, { method: "GET" });
    return result && result.found && result._source && {
      _id: result._id,
      name: result._source.name,
      url: result._source.url,
    } || null;
  }

  // https://cloud-highstreet-technologies.com/wiki/doku.php?id=att:ms:api

  private async accassMediatorServer<TData = {}>(mediatorServerId: string, task: string, data?: {}): Promise<MediatorServerResponse<TData> | null> {
    const path = `ms/${mediatorServerId}/api/'?task=${task}`;
    const result = (await requestRest<string>(path, {
      method: data ? "POST" : "GET",
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: data ? formEncode({
        ...data,
        ...{ task: task }
      }) : null
    }, true)) || null;

    return result ? JSON.parse(result) as { code: number, data: TData } : null;
  }
  /*
  private accassMediatorServer<TData = {}>(mediatorServerId: string, task: string, data?: {}): Promise<MediatorServerResponse<TData> | null> {
    const path = `ms/${mediatorServerId}/api/?task=${task}`;
    return new Promise<{ code: number, data: TData }>((resolve, reject) => {
      $.ajax({
        method: data ? 'POST' : 'GET',
        url: path,
        data: { ...{ task: task }, ...data },
        //contentType: 'application/json'
      }).then((result: any) => {
        if (typeof result === "string") {
          resolve(JSON.parse(result));
        } else {
          resolve(result);
        };
      });
    });
  }*/

  public async getMediatorServerVersion(mediatorServerId: string): Promise<MediatorServerVersionInfo | null> {
    const result = await this.accassMediatorServer<MediatorServerVersionInfo>(mediatorServerId, 'version');
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async getMediatorServerAllConfigs(mediatorServerId: string): Promise<MediatorConfigResponse[] | null> {
    const result = await this.accassMediatorServer<MediatorConfigResponse[]>(mediatorServerId, 'getconfig');
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async getMediatorServerConfigByName(mediatorServerId: string, name: string): Promise<MediatorConfigResponse | null> {
    const result = await this.accassMediatorServer<MediatorConfigResponse[]>(mediatorServerId, `getconfig&name=${name}`);
    if (result && result.code === 1 && result.data && result.data.length === 1) return result.data[0];
    return null;
  }

  public async getMediatorServerSupportedDevices(mediatorServerId: string): Promise<MediatorServerDevice[] | null> {
    const result = await this.accassMediatorServer<MediatorServerDevice[]>(mediatorServerId, 'getdevices');
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async startMediatorByName(mediatorServerId: string, name: string): Promise<string | null> {
    const result = await this.accassMediatorServer<string>(mediatorServerId, `start&name=${name}`);
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async stopMediatorByName(mediatorServerId: string, name: string): Promise<string | null> {
    const result = await this.accassMediatorServer<string>(mediatorServerId, `stop&name=${name}`);
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async createMediatorConfig(mediatorServerId: string, config: MediatorConfig): Promise<string | null> {
    const result = await this.accassMediatorServer<string>(mediatorServerId, 'create', { config: JSON.stringify(config) });
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async updateMediatorConfigByName(mediatorServerId: string, config: MediatorConfig): Promise<string | null> {
    const result = await this.accassMediatorServer<string>(mediatorServerId, 'update', { config: JSON.stringify(config) });
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async deleteMediatorConfigByName(mediatorServerId: string, name: string): Promise<string | null> {
    const result = await this.accassMediatorServer<string>(mediatorServerId, `delete&name=${name}`);
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async getMediatorServerFreeNcPorts(mediatorServerId: string, limit?: number): Promise<number[] | null> {
    const result = await this.accassMediatorServer<number[]>(mediatorServerId, 'getncports', { limit });
    if (result && result.code === 1) return result.data;
    return null;
  }

  public async getMediatorServerFreeSnmpPorts(mediatorServerId: string, limit?: number): Promise<number[] | null> {
    const result = await this.accassMediatorServer<number[]>(mediatorServerId, 'getsnmpports', { limit });
    if (result && result.code === 1) return result.data;
    return null;
  }
}

export const mediatorService = new MediatorService;
export default mediatorService;