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
import * as marked from 'marked';

import { resolvePath } from '../utilities/path';

import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';
import { connect, Connect } from '../../../../framework/src/flux/connect';

import { Markdown } from "../components/markdown";

import '!style-loader!css-loader!github-markdown-css/github-markdown.css'

const mapProps = (state: IApplicationStoreState) => ({
  content: state.help.content,
  currentPath: state.help.currentPath
});

const containerStyle = {
  overflow: "auto",
  height: "100%",
  width: "100%"
};

const styles = {
  maxWidth: "960px",
  margin: "1.5em auto",

};

type HelpApplicationComponentProps = Connect<typeof mapProps>;

class HelpApplicationComponent extends React.Component<HelpApplicationComponentProps> {

  /**
   * Initializes a new instance.
   */
  constructor(props: HelpApplicationComponentProps) {
    super(props);

    this.renderer = new marked.Renderer();

    this.renderer.link = (href: string, title: string, text: string) => {
      // check if href is rel or abs
      const absUrlMatch = href.trim().match(/^https?:\/\//i);
      return `<a href="${absUrlMatch ? href : resolvePath('#/help/', this.props.currentPath || '/', href)}" title="${title}" >${text}</a>`
    };

    this.renderer.image = (href: string, title: string) => {
      return `<img src="${resolvePath('/help/', this.props.currentPath || '/', href)}" alt="${title}" />`
    };

  }

  render(): JSX.Element {
    return this.props.content ? (
      <div style={containerStyle}>
        <Markdown text={this.props.content} markedOptions={{ renderer: this.renderer }} className="markdown-body"
          style={styles} />
      </div>
    ) : (<h2>Loading ...</h2>)
  }

  private renderer: marked.Renderer;
}

export const HelpApplication = connect(mapProps)(HelpApplicationComponent);
export default HelpApplication;