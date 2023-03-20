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
import React from 'react';

import { connect, Connect } from '../../flux/connect';

import { SetTitleAction } from '../../actions/titleActions';
import { AddErrorInfoAction } from '../../actions/errorActions';

import { IconType } from '../../models/iconDefinition';

export interface IAppFrameProps  {
  title: string;
  icon?: IconType;
  appId?: string
}

/**
 * Represents a component to wich will embed each single app providing the
 * functionality to update the title and implement an exeprion border.
 */
export class AppFrame extends React.Component<IAppFrameProps & Connect> {

  public render(): JSX.Element {
    return (
      <div style={{ flex: "1", overflow: "hidden", display: "flex", flexDirection: "column" }}>
        { this.props.children }
      </div>
     )
    }

  public componentDidMount() {
    this.props.dispatch(new SetTitleAction(this.props.title, this.props.icon, this.props.appId));
  }
  public componentDidCatch(error: Error | null, info: object) {
    this.props.dispatch(new AddErrorInfoAction({ error, info }));
  }
}

export default connect()(AppFrame);