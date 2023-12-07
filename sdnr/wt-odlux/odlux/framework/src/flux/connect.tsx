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
import React, { FC, useContext, createContext, useState, useEffect, useRef } from 'react';

import { Dispatch } from './store';

import { ApplicationStore, IApplicationStoreState } from '../store/applicationStore';

const LogLevel = +(localStorage.getItem('log.odlux.framework.flux.connect') || 0);

interface IApplicationStoreContext {
  applicationStore: ApplicationStore;
}

export interface IDispatcher {
  dispatch: Dispatch;
}

interface IApplicationStoreProps {
  state: IApplicationStoreState;
}

interface IDispatchProps {
  dispatch: Dispatch;
}

type ComponentDecoratorInfer<TMergedProps> = {
  <TProps>(wrappedComponent: React.ComponentType<TProps & TMergedProps>): React.ComponentClass<Omit<TProps & TMergedProps, keyof TMergedProps>>;
};

const ApplicationStoreContext = createContext<IApplicationStoreContext | undefined>(undefined);

export type Connect<TMapProps extends ((...args: any) => any) | undefined = undefined, TMapDispatch extends ((...args: any) => any) | undefined = undefined> =
  (TMapProps extends ((...args: any) => any) ? ReturnType<TMapProps> : IApplicationStoreProps) &
  (TMapDispatch extends ((...args: any) => any) ? ReturnType<TMapDispatch> : IDispatchProps);

export function connect(): ComponentDecoratorInfer<IApplicationStoreProps & IDispatchProps>;

export function connect<TStateProps>(
  mapStateToProps: (state: IApplicationStoreState) => TStateProps
): ComponentDecoratorInfer<TStateProps & IDispatchProps>;

export function connect<TStateProps, TDispatchProps>(
  mapStateToProps: (state: IApplicationStoreState) => TStateProps,
  mapDispatchToProps: (dispatcher: IDispatcher) => TDispatchProps
): ComponentDecoratorInfer<TStateProps & TDispatchProps>;


export function connect<TDispatchProps>(
  mapStateToProps: undefined,
  mapDispatchToProps: (dispatcher: IDispatcher) => TDispatchProps
): ComponentDecoratorInfer<IApplicationStoreProps & TDispatchProps>;


export function connect<TProps, TStateProps, TDispatchProps>(
  mapStateToProps?: ((state: IApplicationStoreState) => TStateProps),
  mapDispatchToProps?: ((dispatcher: IDispatcher) => TDispatchProps)
):
  ((WrappedComponent: React.ComponentType<TProps & (IApplicationStoreProps | TStateProps) & IDispatchProps>) => React.ComponentType<TProps>) {

  const injectApplicationStore = (WrappedComponent: React.ComponentType<TProps & (IApplicationStoreProps | TStateProps) & IDispatchProps>): React.ComponentType<TProps> => {

    class StoreAdapter extends React.Component<TProps, {}> {
      
      render(): JSX.Element {

        if (isWrappedComponentIsVersion1(WrappedComponent)) {
          const element = React.createElement(WrappedComponent, { ...(this.props as any), state: this.store.state, dispatch: this.store.dispatch.bind(this.store) });
          return element;
        } else if (mapStateToProps && isWrappedComponentIsVersion2(WrappedComponent)) {
          const element = React.createElement(WrappedComponent, { ...(this.props as any), ...(mapStateToProps(this.store.state) as any), dispatch: this.store.dispatch.bind(this.store) });
          return element;
        } else if (mapStateToProps && mapDispatchToProps && isWrappedComponentIsVersion3(WrappedComponent)) {
          const element = React.createElement(WrappedComponent, { ...(this.props as any), ...(mapStateToProps(this.store.state) as any), ...(mapDispatchToProps({ dispatch: this.store.dispatch.bind(this.store) }) as any) });
          return element;
        } else if (!mapStateToProps && mapDispatchToProps && isWrappedComponentIsVersion4(WrappedComponent)) {
          const element = React.createElement(WrappedComponent, { ...(this.props as any), state: this.store.state, ...(mapDispatchToProps({ dispatch: this.store.dispatch.bind(this.store) }) as any) });
          return element;
        }
        throw new Error("Invalid arguments in connect.");
      }

      componentDidMount(): void {
        this.store && this.store.changed.addHandler(this.handleStoreChanged);
      }

      componentWillUnmount(): void {
        this.store && this.store.changed.removeHandler(this.handleStoreChanged);
      }

      private get store(): ApplicationStore {
        return this.context.applicationStore;
      }

      private handleStoreChanged = () => {
        this.forceUpdate();
      }
    }
    StoreAdapter.contextType = ApplicationStoreContext;
    return StoreAdapter;
  }


  return injectApplicationStore;

  /* inline methods */

  function isWrappedComponentIsVersion1(wrappedComponent: any): wrappedComponent is React.ComponentType<TProps & IApplicationStoreProps & IDispatchProps> {
    return !mapStateToProps && !mapDispatchToProps;
  }

  function isWrappedComponentIsVersion2(wrappedComponent: any): wrappedComponent is React.ComponentType<TProps & TStateProps & IDispatchProps> {
    return !!mapStateToProps && !mapDispatchToProps;
  }

  function isWrappedComponentIsVersion3(wrappedComponent: any): wrappedComponent is React.ComponentType<TProps & TStateProps & TDispatchProps> {
    return !!mapStateToProps && !!mapDispatchToProps;
  }

  function isWrappedComponentIsVersion4(wrappedComponent: any): wrappedComponent is React.ComponentType<TProps & TStateProps & TDispatchProps> {
    return !mapStateToProps && !!mapDispatchToProps;
  }
}

type ApplicationStoreProviderProps = {
  applicationStore: ApplicationStore;
}

export const ApplicationStoreProvider: FC<ApplicationStoreProviderProps> = (props) => {
  const { applicationStore, children } = props;

  return (
    <ApplicationStoreContext.Provider value={{ applicationStore }}>
      {children}
    </ApplicationStoreContext.Provider>
  );
};

export const useApplicationStore = (): ApplicationStore => {
  const context = useContext(ApplicationStoreContext);
  if (context == null || context.applicationStore == null) {
    throw new Error("Requires application store provider!")
  }
  return context.applicationStore
};

export const useSelectApplicationState = <TProp extends unknown >( selector: (state: IApplicationStoreState) => TProp, eqFunc = (a: TProp, b: TProp) => a === b ): TProp => {
  const context = useContext(ApplicationStoreContext);
  if (context == null || context.applicationStore == null) {
    throw new Error("Requires application store provider!")
  }
  
  const [propState, setPropState] = useState<TProp>(selector(context.applicationStore.state));
  
  const selectorRef = useRef(selector);
  selectorRef.current = selector;

  const propStateRef = useRef({propState});
  propStateRef.current.propState = propState;

  useEffect(() => {
    if (context == null || context.applicationStore == null) {
      throw new Error("Requires application store provider!")
    }

    const changedHandler = () => {
      const newState = selectorRef.current(context.applicationStore.state);
      if (!eqFunc(newState, propStateRef.current.propState)) {
        setPropState(newState);
      }
    };

    if (LogLevel > 3) {
      console.log("useSelectApplicationState: adding handler", changedHandler);
    }

    context.applicationStore.changed.addHandler(changedHandler);

    return () => {
      if (LogLevel > 3) {
        console.log("useSelectApplicationState: removing handler", changedHandler);
      }

      context.applicationStore.changed.removeHandler(changedHandler);
    }
  }, [context]);

  return propState;
  
};

export const useApplicationDispatch = (): Dispatch => {
  const context = useContext(ApplicationStoreContext);
  if (context == null || context.applicationStore == null) {
    throw new Error("Requires application store provider!")
  }
  return context.applicationStore.dispatch;
};
