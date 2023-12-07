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
import { TocTreeNode, TocNodeCollection } from '../models/tocNode';

class HelpService {

  private tocNodeCollection: TocTreeNode[] | null = null;
  private documents: { [path: string]: string | null } = {};

  public async getDocument(path: string): Promise<string | null> {
    // check if the result is allready in the cache
    if (this.documents[path]) return Promise.resolve(this.documents[path]);

    // request the document
    const result = await requestRest<string>(`/help/${path}`.replace(/\/{2,}/i, '/'));
    if (result) {
      this.documents[path] = result;
    }
    return this.documents[path] || null;
  }

  public async getTableOfContents(): Promise<TocTreeNode[] | null> {
    // check if the result is allready in the cache
    if (this.tocNodeCollection) return Promise.resolve(this.tocNodeCollection);

    // request the table of contents
    const result = await requestRest<TocNodeCollection>('/help/?meta', undefined, false);
    if (result !== null) {
      const mapNodesCollection = (col: TocNodeCollection): TocTreeNode[] => {
        return Object.keys(col).reduce<TocTreeNode[]>((acc, key) => {
          const current = col[key];
          acc.push({
            id: key,
            label: current.label,
            uri: current.versions.current.path,
            nodes: current.nodes && mapNodesCollection(current.nodes) || undefined
          });
          return acc;
        }, []);
      }

      this.tocNodeCollection = result && mapNodesCollection(result) || null;
    }
    return this.tocNodeCollection || null;
  }
}

export const helpService = new HelpService();
export default helpService;