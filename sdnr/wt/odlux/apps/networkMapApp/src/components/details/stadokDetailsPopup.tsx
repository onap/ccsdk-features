/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2021 highstreet technologies GmbH Intellectual Property. All rights reserved.
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

import MuiDialogTitle from '@material-ui/core/DialogTitle';
import { AppBar, Dialog, DialogContent, IconButton, Tab, Tabs, TextField, Typography } from '@material-ui/core';
import CloseIcon from '@material-ui/icons/Close';
import { withStyles, WithStyles, createStyles, Theme, makeStyles } from '@material-ui/core/styles';


import StadokSite from '../../model/stadokSite';
import { LatLonToDMS } from '../../utils/mapUtils';
import DenseTable from '../../components/denseTable';
import { requestRest } from '../../../../../framework/src/services/restService';
import { OrderToDisplay, StadokOrder } from '../../model/stadokOrder';
import { CSSProperties } from '@material-ui/core/styles/withStyles';
import { SITEDOC_URL } from '../../config';


type props = { site: StadokSite; onClose(): void; open:boolean };

const styles = (theme: Theme) => createStyles({
  root: {
    margin: 0,
    padding: theme.spacing(2),
  },
  closeButton: {
    position: 'absolute',
    right: theme.spacing(1),
    top: theme.spacing(1),
    color: theme.palette.grey[500],
  },
});

const useStyles = makeStyles({
  largeImage:{cursor:'pointer', width:300},
  smallImage:{cursor:'pointer', width: 50, marginTop:'10px', marginLeft:'10px'}
});

const StadokDetailsPopup: React.FunctionComponent<props> = (props) => {
  const classes = useStyles();

  const [open, setOpen] = React.useState(props.open);
  const [value, setValue] = React.useState("devices");
  const [orders, setOrders] = React.useState<OrderToDisplay[]|null>(null);

  const DialogTitle = withStyles(styles)((props: any) => {
    const { children, classes, onClose, ...other } = props;
    return (
      <MuiDialogTitle disableTypography className={classes.root} {...other}>
        <Typography variant="h6">{children}</Typography>
        {onClose ? (
          <IconButton aria-label="close" style={{position: 'absolute', top:0, right:0, color: 'black'}} onClick={onClose}>
            <CloseIcon />
          </IconButton>
        ) : null}
      </MuiDialogTitle>
    );
  });

  const getContacts = (site: StadokSite) =>{
    const contacts = [];

    if(site.createdBy){
      contacts.push({h: "Site Creator",col1: site.createdBy.firstName, col2: site.createdBy.lastName, col3: site.createdBy.email, col4: site.createdBy.telephoneNumber });
    }
  
    if(site.contacts.manager){
      contacts.push({h: "Manager",col1: site.contacts.manager.firstName, col2: site.contacts.manager.lastName, col3: site.contacts.manager.email, col4: site.contacts.manager.telephoneNumber });
    }
  
    if(site.contacts.owner){
      contacts.push({h: "Owner",col1: site.contacts.owner.firstName, col2: site.contacts.owner.lastName, col3: site.contacts.owner.email, col4: site.contacts.owner.telephoneNumber });
    }
    return contacts;
  }

  const onClose = () =>{
   // setOpen(false);
     props.onClose()
  }

  //todo: use a set 'panelId' -> which values are allowed
  const onHandleTabChange = (event: React.ChangeEvent<{}>, newValue: string) => {
    setValue(newValue);
}
console.log(props.site)
  const contacts = getContacts(props.site);
  
  const orderUrl=`${SITEDOC_URL}/site/${props.site.siteId}/orders`;

  if(orders==null){
    requestRest<StadokOrder[]>(orderUrl,{ method: "GET"}).then(result =>{
      if(result){
        const orderList = result.map(order =>{
          return OrderToDisplay.parse(order);
        });
        setOrders(orderList);

      }else{
        setOrders([]);
      }
    });
  }

  const createOrderInfo = () => {

    if (orders === null) {
      return (<div style={{ height: 300 }}>
        <Typography variant="body1" style={{ marginTop: '10px' }}>
          Loading orders
        </Typography>
      </div>)
    } else if (orders.length === 0) {
      return (<div style={{ height: 300 }}>
        <Typography variant="body1" style={{ marginTop: '10px' }}>
          No orders available
      </Typography>
      </div>)
    } else {
      return <DenseTable data={orders} height={300} headers={["Person", "State", "Current Task"]} hover={false} ariaLabelRow="activity-log-table" />
    }
  }

  const displayImages = () => {

    if (props.site.images.length === 1) {
      return stadokImage(props.site.siteId, props.site.images[0],"large")
    } else {
      return <>
        {
          stadokImage(props.site.siteId, props.site.images[0], "large")
        }
        <div style={{ display: 'flex', flexDirection: 'row', flexWrap:'wrap' }}>

          {
            props.site.images.length<=9 ?
              props.site.images.slice(1, props.site.images.length).map(image =>
                stadokImage(props.site.siteId, image, "small")
              )
              :
              <>
              {
                props.site.images.slice(1, 9).map(image =>
                  stadokImage(props.site.siteId, image, "small")
                )
              }
              
              </>
          }
        </div>
      </>
    }

  }

  const stadokImage = (siteId: string, imagename: string, size: 'large' | 'small') => {
    const url = `${SITEDOC_URL}/site/${siteId}/files/${imagename}`;
    const className = size === "small" ? classes.smallImage : classes.largeImage;
    return <img className={className} src={url} onClick={e => window.open(url)} />

  }


  return (<Dialog  onClose={onClose} fullWidth maxWidth="md" aria-labelledby="customized-dialog-title" open={open}>
    <DialogTitle id="customized-dialog-title" onClose={onClose}>
      {props.site.siteId}
    </DialogTitle>
    <DialogContent style={{minWidth:'900px'}} dividers>
      <div style={{ display: 'flex', flexDirection: 'row', flexGrow: 1 }}>
        <div style={{ width: '60%', display:'flex', flexDirection: 'column' }}>
         
        <TextField inputProps={{ 'aria-label': 'type' }} disabled={true} value={props.site.updatedOn} label="Updated on" style={{ marginTop: "5px" }} />

         
          {
            props.site.type !== undefined && props.site.type.length > 0 &&
            <TextField inputProps={{ 'aria-label': 'type' }} disabled={true} value={props.site.type} label="Type" style={{ marginTop: "5px" }} />
          }


          <TextField inputProps={{ 'aria-label': 'adress' }} disabled={true} value={`${props.site.address.streetAndNr}, ${props.site.address.zipCode !== null ? props.site.address.zipCode : ''} ${props.site.address.city}`} label="Address" style={{ marginTop: "5px" }} />


          <TextField inputProps={{ 'aria-label': 'latitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.location.lat)} label="Latitude" />
          <TextField inputProps={{ 'aria-label': 'longitude' }} style={{ marginTop: "5px" }} disabled={true} value={LatLonToDMS(props.site.location.lon, true)} label="Longitude" />
          <AppBar position="static" style={{ marginTop: "5px", background: '#2E3B55' }}>
            <Tabs id="site-tabs" variant="scrollable" scrollButtons="on" value={value} onChange={onHandleTabChange} aria-label="simple tabs example">
                <Tab label="Devices" value="devices" />
                <Tab label="Contacts" value="contacts" />
                <Tab label="Saftey" value="safteyInfo" />
                <Tab label="Logs" value="logs" />
                <Tab label="Orders" value="orders" />
            </Tabs>
        </AppBar>
        {
          value == "devices" && (props.site.devices?.length>0 ?
        <DenseTable data={props.site.devices} height={300} headers={["Device", "Antenna"]} hover={false} ariaLabelRow="devices-table" />
        :
        <div style={{height:300}}>
        <Typography variant="body1" style={{ marginTop: '10px' }}>
          No devices available
        </Typography>
        </div>)
        }
        {
           value == "contacts" && (contacts.length>0 ?
            <DenseTable data={contacts} height={300} headers={["Person", "Firstname", "Lastname", "Email", "Phone No."]} hover={false} ariaLabelRow="contacts-table" ariaLabelColumn={["person", "firstname", "lastname", "email", "phoneno"]} />
            :
            <div style={{height:300}}>
            <Typography variant="body1" style={{ marginTop: '10px' }}>
              No contacts available
            </Typography>
            </div>)
        }
        {
          value == "safteyInfo" && (props.site.safteyNotices.length>0 ?
            <DenseTable data={props.site.safteyNotices} height={300} headers={["Note"]} hover={false} ariaLabelRow="saftey-info-table"  />
            :
            <div style={{height:300}}>
            <Typography variant="body1" style={{ marginTop: '10px' }}>
              No saftey notices applicable
            </Typography>
            </div>)
        }
        {
           value == "logs" && (props.site.logs.length>0 ?
            <DenseTable data={props.site.logs} height={300} headers={["Date","Person", "Activity"]} hover={false} ariaLabelRow="activity-log-table"  />
            :
            <div style={{height:300}}>
            <Typography variant="body1" style={{ marginTop: '10px' }}>
              No activity log available
            </Typography>
            </div>)
        }

        {
          value ==="orders" && createOrderInfo()
        }
        
        </div>
        <div style={{padding: '10px', display: 'flex', alignItems:'center', flexDirection:'column', justifyContent: 'start', width:'40%'}}>
          {
            props.site.images.length == 0 ? 
            <Typography variant="body1" style={{ marginTop: '10px' }}>
            No images available
          </Typography>
          : displayImages()
          }
        </div>
      </div>

    </DialogContent>
  </Dialog>)

}

export default StadokDetailsPopup;