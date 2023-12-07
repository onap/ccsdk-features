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
import { Event } from "../common/event"

import { Action } from './action';
import { IActionHandler } from './action';

const LogLevel = +(localStorage.getItem('log.odlux.framework.flux.store') || 0);

export interface Dispatch {
  <TAction extends Action>(action: TAction): TAction;
}

export interface Enhancer<TStoreState> {
  (store: Store<TStoreState>): Dispatch;
}

class InitializationAction extends Action { };
const initializationAction = new InitializationAction();

export class Store<TStoreState> {

  constructor(actionHandler: IActionHandler<TStoreState>, enhancer?: Enhancer<TStoreState>)
  constructor(actionHandler: IActionHandler<TStoreState>, initialState: TStoreState, enhancer?: Enhancer<TStoreState>)
  constructor(actionHandler: IActionHandler<TStoreState>, initialState?: TStoreState | Enhancer<TStoreState>, enhancer?: Enhancer<TStoreState>) {
    if (typeof initialState === 'function') {
      enhancer = initialState as Enhancer<TStoreState>;
      initialState = undefined;
    }

    this._isDispatching = false;
     
    this.changed = new Event<void>();

    this._actionHandler = actionHandler;
    
    this._state = initialState as TStoreState;
    if (enhancer) this._dispatch = enhancer(this);

    this._dispatch(initializationAction);
  }

  public changed: Event<void>;

  private _dispatch: Dispatch = <TAction extends Action>(payload: TAction): TAction => {
    if (LogLevel > 2) {
      console.log('Store::Dispatch - ', payload);
    }
    if (payload == null || !(payload instanceof Action)) {
      throw new Error(
        'Actions must inherit from type Action. ' +
        'Use a custom middleware for async actions.'
      );
    }
    
    if (this._isDispatching) {
      throw new Error('ActionHandler may not dispatch actions.');
    }

    const oldState = this._state;
    try {
      this._isDispatching = true;
      this._state = this._actionHandler(oldState, payload);
    } finally {
      this._isDispatching = false;
    }

    if (this._state !== oldState) {
      if (LogLevel > 3) {
        console.log('Store::Dispatch - state has changed', this._state);
      }
      this.changed.invoke();
    }

    return payload;
  }

  public get dispatch(): Dispatch {
    return this._dispatch;
  }

  public get state() {
    return this._state
  }

  private _state: TStoreState;
  private _isDispatching: boolean;
  private _actionHandler: IActionHandler<TStoreState>;

}

