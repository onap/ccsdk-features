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

 import Button from '@material-ui/core/Button';
 import Dialog from '@material-ui/core/Dialog';
 import DialogActions from '@material-ui/core/DialogActions';
 import DialogTitle from '@material-ui/core/DialogTitle';
 import { MaterialTable, ColumnType, MaterialTableCtorType } from '../../../../framework/src/components/material-table';
 import { IDispatcher, connect, Connect } from '../../../../framework/src/flux/connect';
 
 import { NetworkElementConnection } from '../models/networkElementConnection';
 import { AvailableCapabilities } from '../models/yangCapabilitiesType'
 
 export enum InfoNetworkElementDialogMode {
   None = "none",
   InfoNetworkElement = "infoNetworkElement"
 }
 
 const mapDispatch = (dispatcher: IDispatcher) => ({
 });
 
 
 const InfoElementTable = MaterialTable as MaterialTableCtorType<AvailableCapabilities>;
 
 type DialogSettings = {
   dialogTitle: string,
   dialogDescription: string,
   cancelButtonText: string,
 }
 
 const settings: { [key: string]: DialogSettings } = {
   [InfoNetworkElementDialogMode.None]: {
     dialogTitle: "",
     dialogDescription: "",
     cancelButtonText: "",
   },
   [InfoNetworkElementDialogMode.InfoNetworkElement]: {
     dialogTitle: "Yang capabilities of the network element",
     dialogDescription: "",
     cancelButtonText: "OK",
   }
 }
 
 type InfoNetworkElementDialogComponentProps = Connect<undefined, typeof mapDispatch> & {
   mode: InfoNetworkElementDialogMode;
   initialNetworkElement: NetworkElementConnection;
   onClose: () => void;
 };
 
 type InfoNetworkElementDialogComponentState = NetworkElementConnection;
 
 class InfoNetworkElementDialogComponent extends React.Component<InfoNetworkElementDialogComponentProps, InfoNetworkElementDialogComponentState> {
   constructor(props: InfoNetworkElementDialogComponentProps) {
     super(props);
 
     this.state = {
       nodeId: this.props.initialNetworkElement.nodeId,
       isRequired: false,
       host: this.props.initialNetworkElement.host,
       port: this.props.initialNetworkElement.port,
     };
   }
 
   render(): JSX.Element {
     const setting = settings[this.props.mode];
     const availableCapabilities = this.props.state.connect.elementInfo.elementInfo["netconf-node-topology:available-capabilities"]["available-capability"];
     let yangFeatures = this.props.state.connect.elementFeatureInfo.elementFeatureInfo;
     let yangCapabilities: AvailableCapabilities[] = [];
     
     availableCapabilities.forEach(value => {
       const capabilty = value.capability;
       const indexRevision = capabilty.indexOf("revision=");
       const indexModule = capabilty.indexOf(")", indexRevision);
       if (indexRevision > 0 && indexModule > 0) {
         let moduleName = capabilty.substr(indexModule + 1);
         let ModuleFeaturesList;
         for(let index = 0; index < yangFeatures.length; index++) {
           if(yangFeatures[index].name == moduleName) {
             ModuleFeaturesList = yangFeatures[index].feature? yangFeatures[index].feature : null;
             break;
           }
         }
         const featuresListCommaSeparated= ModuleFeaturesList? ModuleFeaturesList.toString() : ""
         let featuresList = featuresListCommaSeparated.replace(',',', ');
 
         yangCapabilities.push({
           module: moduleName,
           revision: capabilty.substr(indexRevision + 9, 10),
           features: featuresList 
          });
       }
     });
 
     yangCapabilities = yangCapabilities.sort((a,b) => a.module === b.module ? 0 : a.module > b.module ? 1 : -1);
 
     return (
       <>
         <Dialog open={this.props.mode !== InfoNetworkElementDialogMode.None}  >
           <DialogTitle id="form-dialog-title">{setting.dialogTitle + ' - ' + this.state.nodeId}</DialogTitle>
           <InfoElementTable stickyHeader tableId="info-element-table" asynchronus columns={[
             { property: "module", title: "Module", type: ColumnType.text, width:900 },
             {
               property: "revision", title: "Revision", type: ColumnType.custom, customControl: ({ rowData }) => {
                 return (
                   <div>
                     <a href={'/yang-schema/' + rowData.module + '/' + rowData.revision} target="_blank"  > {rowData.revision} </a>
                   </div>
                 )
               }
             },
             { property: "features", title: "Features", type: ColumnType.text, width:500 },
           ]} idProperty="id" rows={yangCapabilities}  >
           </InfoElementTable>
           <DialogActions>
             <Button aria-label="ok-button" onClick={(event) => {
               this.onCancel();
               event.preventDefault();
               event.stopPropagation();
             }} color="secondary"> {setting.cancelButtonText} </Button>
           </DialogActions>
         </Dialog>
       </>
     )
   }
 
   private onCancel = () => {
     this.props.onClose();
   }
 
   static getDerivedStateFromProps(props: InfoNetworkElementDialogComponentProps, state: InfoNetworkElementDialogComponentState & { _initialNetworkElement: NetworkElementConnection }): InfoNetworkElementDialogComponentState & { _initialNetworkElement: NetworkElementConnection } {
     if (props.initialNetworkElement !== state._initialNetworkElement) {
       state = {
         ...state,
         ...props.initialNetworkElement,
         _initialNetworkElement: props.initialNetworkElement,
       };
     }
     return state;
   }
 }
 
 export const InfoNetworkElementDialog = connect(undefined, mapDispatch)(InfoNetworkElementDialogComponent);
 export default InfoNetworkElementDialog;