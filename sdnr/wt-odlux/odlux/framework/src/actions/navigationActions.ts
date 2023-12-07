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
import { Action } from "../flux/action";

export abstract class NavigationAction extends Action { }

export class NavigateToApplication<TState = { }> extends NavigationAction {
 
  constructor(public applicationName: string, public href?: string, public state?: TState, public replace: boolean = false ) {
    super();
    
  }
}

export class PushAction<TState = { }> extends NavigationAction {
  constructor(public href: string, public state?: TState) {
    super();

  }
}

export class ReplaceAction<TState = { }> extends NavigationAction {
  constructor(public href: string, public state?: TState) {
    super();

  }
}

export class GoAction extends NavigationAction {
  constructor(public index: number) {
    super();

  }
}

export class GoBackAction extends NavigationAction {

}

export class GoForwardeAction extends NavigationAction {

}

export class LocationChanged extends NavigationAction {
  constructor(public pathname: string, public search: string, public hash: string ) {
    super();
    
  }
} 