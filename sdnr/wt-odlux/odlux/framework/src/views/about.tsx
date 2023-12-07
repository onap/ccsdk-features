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
import React, { FC, useEffect, useState } from 'react';
import * as marked from 'marked';
import * as hljs from 'highlight.js';
import { requestRestExt } from '../services/restService';
import { Button, Typography } from '@mui/material';

const defaultRenderer = new marked.Renderer();
defaultRenderer.link = (href, title, text) => (
  `<a target="_blank" rel="noopener noreferrer" href="${href}" title="${title}">${text}</a>`
);

type OdluxVersion= {version:string,build:string, framework: string, 
  applications:{
    configurationApp: string,
    connectApp: string,
    eventLogApp: string,
    faultApp: string,
    helpApp: string,
    inventoryApp: string,
    microwaveApp: string,
    maintenanceApp: string,
    mediatorApp: string,
    networkMapApp: string,
    permanceHistoryApp: string,
    siteManagerApp: string,
  }};

type TopologyVersion = {version: string, buildTimestamp: string};

const AboutComponent: FC = (props) => {
  
  const textareaRef = React.createRef<HTMLTextAreaElement>();
  const [content, setContent] = useState<string | null>(null);
  const [isCopiedSuccessfully, setCopySuccess] = useState(false);
  const [isContetLoaded, setContentLoaded] = useState(false);

  useEffect(()=>{
    loadAboutContent();
  },[]);

  const getMarkOdluxVersionMarkdownTable = (data:OdluxVersion|null|undefined):string => {
    if(!data) {
      return "";
    }else{
      let applicationVersions= '';
      if(data.applications){

        applicationVersions = `| Framework | ${data.framework}|\n `+
        `| ConnectApp | ${data.applications.connectApp}|\n `+
        `| FaultApp | ${data.applications.faultApp}|\n `+
        `| MaintenanceApp | ${data.applications.maintenanceApp}|\n `+
        `| ConfigurationApp | ${data.applications.configurationApp}|\n `+
        `| PerformanceHistoryApp | ${data.applications.permanceHistoryApp}|\n `+
        `| InventoryApp | ${data.applications.inventoryApp}|\n `+
        `| EventLogApp | ${data.applications.eventLogApp}|\n `+
        `| MediatorApp | ${data.applications.mediatorApp}|\n `+
        `| NetworkMapApp | ${data.applications.networkMapApp}|\n `+
        `| MicrowaveApp | ${data.applications.microwaveApp}|\n `+
        `| SiteManagerApp | ${data.applications.siteManagerApp}|\n `+
        `| HelpApp | ${data.applications.helpApp}|\n `;
      }
    
    return `| | |\n| --- | --- |\n| Version | ${data.version} |\n| Build timestamp | ${data.build}|\n`+
    applicationVersions;
    }
  }

  const getTopologyVersionMarkdownTable = (data: TopologyVersion|null|undefined) => { 
    if(!data){
      return "No version";
    }
    else
    {
      const topologyInfo = `| | |\n| --- | --- |\n| Version | ${data.version} |\n` +
                           `| Build timestamp | ${data.buildTimestamp} |\n`;
      return topologyInfo;
    }
  }

  const loadAboutContent = (): void => {
    const baseUri = window.location.pathname.substring(0,window.location.pathname.lastIndexOf("/")+1);
    const init = {
      'method': 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'text/markdown',
      }
    };
    const p1 = requestRestExt<string>('/about',init);
    const p2 = requestRestExt<OdluxVersion>(`${baseUri}version.json`);
    const p3 = requestRestExt<any>(`/topology/info/version`);

    Promise.all([p1,p2, p3]).then((responses) => {
      const response = responses[0];
      const response2 = responses[1]; 
      const response3 = responses[2];   
      const content = response.status == 200 ? response.data : `${response.status} ${response.message}` || "Server error";
      const content2 = `\n## ODLUX Version Info\n`+(response2.status == 200 ? getMarkOdluxVersionMarkdownTable(response2.data) : `${response2.message}` || "ODLUX Server error");
      const content3 =  `\n## Topology API Version Info\n`+(response3.status == 200 ? getTopologyVersionMarkdownTable(response3.data): `Topology API not available`);
      const loadedSucessfully = response.status == 200 ? true : false;
      setContent((content + content2 + content3 ) || null);
      setContentLoaded(loadedSucessfully);
    }).catch((error) => {
      setContent(error);
    });
  }

  const copyToClipboard = (e: React.MouseEvent<HTMLButtonElement>) =>{
    e.preventDefault();

    if(textareaRef.current!==null){
      textareaRef.current.select();
      document.execCommand('copy');
      if(e.currentTarget != null){ // refocus on button, otherwhise the textarea would be focused
        e.currentTarget.focus();
      }
      setCopySuccess(true);
      window.setTimeout(()=>{ setCopySuccess(false);},2000);
    }
  }

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

    const html = (marked(content || 'loading', { renderer: markedOptions && markedOptions.renderer || defaultRenderer }));

    return (
      <div style={containerStyle}>
        { isContetLoaded &&
        <div style={{float: "right", marginRight: "10px"}}>
        <Button aria-label="copy-version-information-button" color="inherit" variant="contained" onClick={e => copyToClipboard(e)}>
           Copy to clipboard
        </Button>
          {
            isCopiedSuccessfully && 
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
            ref={textareaRef}
            value={content || ''}
          />
        </form>
      </div>
    );
};

export const About = AboutComponent;
export default About;