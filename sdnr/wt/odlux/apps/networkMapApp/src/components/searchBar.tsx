/**
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt odlux
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
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
import { makeStyles, Paper, InputBase, IconButton, Divider, Popover, Typography } from '@material-ui/core';
import SearchIcon from '@material-ui/icons/Search';

import { URL_API } from '../config';
import { isSite } from '../utils/utils';
import { site } from '../model/site';
import { link } from '../model/link';
import { SelectSiteAction, SelectLinkAction } from '../actions/detailsAction';
import { HighlightLinkAction, HighlightSiteAction, ZoomToSearchResultAction } from '../actions/mapActions';
import { calculateMidPoint } from '../utils/mapUtils';
import { SetSearchValueAction } from '../actions/searchAction';
import connect,{ Connect, IDispatcher } from '../../../../framework/src/flux/connect';
import { IApplicationStoreState } from '../../../../framework/src/store/applicationStore';






const styles =  makeStyles({
    root: {
      //{ padding:5, position: 'absolute', display:'flex', flexDirection:"column",top: 150, width: 200}
      padding: '2px 4px',
      position: 'absolute',
      display:'flex',
      alignItems: 'center',
      top: 15,
      marginLeft: 5,
      width: 400,
    },
    input: {
      flex: 1,
      marginLeft: 5
    },
    iconButton: {
      padding: 10,
    },
    divider: {
      height: 28,
      margin: 4,
    },
  });


const SearchBar: React.FunctionComponent<searchBarProps> = (props) =>{

    const classes = styles();
    const [anchorEl, setAnchorEl] = React.useState<any>(null);
    const [errorMessage, setErrorMessage] = React.useState("");

    const divRef = React.useRef();

    const handleClick = (e: any) =>{

      setAnchorEl(null);
      if(props.searchterm.length>0){

        const siteResult = fetch(`${URL_API}/site/${props.searchterm}`)

        const linkResult = fetch(`${URL_API}/link/${props.searchterm}`);
  
           Promise.all([ siteResult, linkResult]).then((result)=>{
              const suceededResults = result.filter(el=> el!==undefined);
  
             if(suceededResults.length==0){
              setAnchorEl(divRef.current);
              setErrorMessage("No element found.")
              //hide element after x secs

             }else{
              suceededResults[0].json().then(result =>{
                if(isSite(result)){
                  props.selectSite(result);
                  props.highlightSite(result);
                  props.zoomToSearchResult(result.geoLocation.lat, result.geoLocation.lon);
                }else{
                  props.selectLink(result);
                  props.highlightLink(result);
                  const midPoint = calculateMidPoint(result.locationA.lat, result.locationA.lon, result.locationB.lat, result.locationB.lon);
                  props.zoomToSearchResult(midPoint[1], midPoint[0])
                }
              });
      }  
    });
  }
  e.preventDefault();
}

    const open = Boolean(anchorEl);

    const reachabe = props.isTopoServerReachable && props.isTileServerReachable;

    return (
      <>
        <Paper ref={divRef} component="form" className={classes.root}>
          <InputBase
          disabled={!reachabe}
            className={classes.input}
            placeholder="Find sites or links by name"
            inputProps={{ 'aria-label': 'search sites or links' }}
            value={props.searchterm}
            onChange={e=> props.setSearchTerm(e.currentTarget.value)}
          />
          <Divider className={classes.divider} orientation="vertical" />
          <IconButton type="submit" className={classes.iconButton} aria-label="search" onClick={handleClick}>
            <SearchIcon />
          </IconButton>
        </Paper>
        <Popover open={open} onClose={e=> setAnchorEl(null)} anchorEl={anchorEl} anchorOrigin={{
          vertical: "bottom",
          horizontal: "left"
        }}>
          <Paper style={{width: 380, padding:10}}>
      <Typography variant="body1">{errorMessage}</Typography>
          </Paper>
        </Popover>
        </>
      );
}

const mapStateToProps = (state: IApplicationStoreState) => ({
  searchterm: state.network.search.value,
  isTopoServerReachable: state.network.connectivity.isToplogyServerAvailable,
  isTileServerReachable: state.network.connectivity.isTileServerAvailable

});

type searchBarProps = Connect<typeof mapStateToProps, typeof mapDispatchToProps>;


const mapDispatchToProps = (dispatcher: IDispatcher) => ({ 
  selectSite:(site: site)=> dispatcher.dispatch(new SelectSiteAction(site)), 
  selectLink:(link: link) => dispatcher.dispatch(new SelectLinkAction(link)), 
  highlightLink:(link: link)=> dispatcher.dispatch(new HighlightLinkAction(link)),
  highlightSite: (site: site) => dispatcher.dispatch(new HighlightSiteAction(site)),
  setSearchTerm: (value: string) => dispatcher.dispatch(new SetSearchValueAction(value)),
  zoomToSearchResult: (lat: number, lon: number) => dispatcher.dispatch(new ZoomToSearchResultAction(lat, lon)),
});;

export default (connect(mapStateToProps,mapDispatchToProps)(SearchBar))