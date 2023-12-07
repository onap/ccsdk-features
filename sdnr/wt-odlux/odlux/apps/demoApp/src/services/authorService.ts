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
import { IAuthor } from '../models/author';

import * as $ from 'jquery';

const base_url = 'https://api.mfico.de/v1/authors';

/** 
 * Represents a web api accessor service for all author related actions.
 */
class AuthorService {

  /**
   * Gets all known authors from the backend.
   * @returns A promise of the type array of @see {@link IAuthor} containing all known authors.
   */
  public getAllAuthors(): Promise<IAuthor[]> {
    return new Promise((resolve: (value: IAuthor[]) => void, reject: (err: any) => void) => {
      $.ajax({ method: 'GET', url: base_url })
        .then((data) => { resolve(data); }, (err) => { reject(err); });
    });
  }

  /**
   * Gets an author by its id from the backend.
   * @returns A promise of the type @see {@link IAuthor} containing the author to get.
   */
  public getAuthorById(id: string | number): Promise<IAuthor> {
    return new Promise((resolve: (value: IAuthor) => void, reject: (err: any) => void) => {
      $.ajax({ method: 'GET', url: base_url + '/' + id })
        .then((data) => { resolve(data); }, (err) => { reject(err); });
    });
  }


  /**
 * Saves the given author to the backend api.
 * @returns A promise of the type @see {@link IAuthor} containing the autor returned by the backend api.
 */
  public saveAuthor(author: IAuthor): Promise<IAuthor> {
    return new Promise((resolve: (value: IAuthor) => void, reject: (err: any) => void) => {
      // simulate server save
      window.setTimeout(() => {
        if (Math.random() > 0.8) {
          reject('Could not save author.');
        } else {
          resolve(author);
        }
      }, 800); // simulate a short network delay
    });
  }
}

// return as a singleton
export const authorService = new AuthorService();
export default authorService;
