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
import * as React from 'react';
import * as marked from 'marked';
import * as hljs from 'highlight.js';
import { requestRestExt } from '../services/restService';
import { Button, Typography } from '@material-ui/core';
const defaultRenderer = new marked.Renderer();
defaultRenderer.link = (href, title, text) => (
  `<a target="_blank" rel="noopener noreferrer" href="${href}" title="${title}">${text}</a>`
);
interface AboutState {
  content: string | null;
  isCopiedSuccessfully: boolean;
  isContentLoadedSucessfully: boolean;
}

class AboutComponent extends React.Component<any, AboutState> {
  textarea: React.RefObject<HTMLTextAreaElement>;


  constructor(props: any) {
    super(props);
    this.state = { content: null, isCopiedSuccessfully:false, isContentLoadedSucessfully: false }
    this.textarea = React.createRef();
    this.loadAboutContent();
  }
  private loadAboutContent(): void {
    requestRestExt<string>('/about').then((response) => {
      const content = response.status == 200 ? response.data : `${response.status} ${response.message}` || "Server error";
      const loadedSucessfully = response.status == 200 ? true : false;
      this.setState({ content: content, isContentLoadedSucessfully: loadedSucessfully });
    }).catch((error) => {
      this.setState({ content: error })
    })
  }

  copyToClipboard = (e: React.MouseEvent<HTMLButtonElement>) =>{
    e.preventDefault();

    if(this.textarea.current!==null){
      this.textarea.current.select();
      document.execCommand('copy');
      if(e.currentTarget != null){ // refocus on button, otherwhise the textarea would be focused
        e.currentTarget.focus();
      }
      this.setState({isCopiedSuccessfully: true});
      window.setTimeout(()=>{this.setState({isCopiedSuccessfully: false});},2000);
    }
  }

  render() {

    const markedOptions: marked.MarkedOptions = {
      gfm: true,
      breaks: false,
      pedantic: false,
      sanitize: true,
      smartLists: true,
      smartypants: false,
      langPrefix: 'hljs ',
      ...({}),
      highlight: (code, lang) => {
        if (!!(lang && hljs.getLanguage(lang))) {
          return hljs.highlight(lang, code).value;
        }
        return code;
      }
    };


    const className = "about-table"
    const style: React.CSSProperties = {};
    const containerStyle = { overflow: "auto", paddingRight: "20px" }

    const html = (marked(this.state.content || 'loading', { renderer: markedOptions && markedOptions.renderer || defaultRenderer }));

    return (
      <div style={containerStyle}>
        { this.state.isContentLoadedSucessfully &&
        <div style={{float: "right", marginRight: "10px"}}>
        <Button variant="contained" onClick={e => this.copyToClipboard(e)}>
           Copy to clipboard
        </Button>
          {
            this.state.isCopiedSuccessfully && 
            <Typography variant="body1" style={{color: "green"}} align="center">
             copied successfully
            </Typography>
          }
        </div>
      }
       
        <div
          dangerouslySetInnerHTML={{ __html: html }}
          className={className}
          style={style}
        />
         <form>
          <textarea
           style={{opacity: ".01"}}
            ref={this.textarea}
            value={this.state.content || ''}
          />
        </form>
      </div>
    );
  }
};

export const About = AboutComponent;
export default About;