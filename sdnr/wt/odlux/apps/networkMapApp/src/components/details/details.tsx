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

import * as React from 'react'

import connect, { IDispatcher, Connect } from '../../../../../framework/src/flux/connect';

import { site, Device } from '../../model/site';
import Typography from '@material-ui/core/Typography';
import { link } from '../../model/link';
import { Breadcrumbs, Link, Paper } from '@material-ui/core';
import SiteDetails from './siteDetails';
import LinkDetails from './linkDetails';
import { URL_API, URL_BASEPATH } from '../../config';
import { SelectSiteAction, SelectLinkAction, AddToHistoryAction, ClearHistoryAction, CheckDeviceList, ClearDetailsAction } from '../../actions/detailsAction';
import { HistoryEntry } from '../../model/historyEntry';
import { HighlightLinkAction, HighlightSiteAction, RemoveHighlightingAction } from '../../actions/mapActions';
import { isSite } from '../../utils/utils';
import { IApplicationStoreState } from '../../../../../framework/src/store/applicationStore';
import { NavigateToApplication } from '../../../../../framework/src/actions/navigationActions';
import { RouteComponentProps, withRouter } from 'react-router-dom';


const Details: React.FunctionComponent<porps> = (props) => {

    const [message, setMessage] = React.useState("No data selected.");


    //on mount
    React.useEffect(() => {
        const detailsId = getDetailsIdFromUrl();
        if (detailsId !== null && props.data?.name !== detailsId) {
            loadDetailsData(detailsId)
        }

    }, []);

    // if url changed
    React.useEffect(() => {
        const detailsId = getDetailsIdFromUrl();
        console.log(detailsId)
        if (detailsId !== null && props.data?.name !== detailsId) {
            loadDetailsData(detailsId)
        }
        else if(detailsId===null){
            setMessage("No data selected.");
            props.clearDetails();
            props.undoMapSelection();
        }

    }, [props.location.pathname]);

    //update url if new element loaded
    React.useEffect(() => {
        if (props.data !== null) {
            const currentUrl = window.location.href;
            const parts = currentUrl.split(URL_BASEPATH);
            const detailsPath = parts[1].split("/details/");
            props.history.replace(`/${URL_BASEPATH}${detailsPath[0]}/details/${props.data.name}`)
        }

    }, [props.data])

    const onLinkClick = async (id: string) => {
        const result = await fetch(`${URL_API}/link/${id}`);
        if(result.ok){
            const resultAsJson = await result.json();
            const link = resultAsJson as link;
            props.selectLink(link);
            props.addHistory({ id: props.data!.name, data: props.data! });
            props.highlightLink(link);

        }
    }

    const backClick = (e: any) => {
        if (isSite(props.breadcrumbs[0].data)) {
            props.selectSite(props.breadcrumbs[0].data)
            props.highlightSite(props.breadcrumbs[0].data);

        } else {
            props.selectLink(props.breadcrumbs[0].data);
            props.highlightLink(props.breadcrumbs[0].data);

        }

        props.clearHistory();
        e.preventDefault();
    }

    const createDetailPanel = (data: site | link) => {

        if (isSite(data)) {
            return <SiteDetails navigate={props.navigateToApplication} updatedDevices={props.updatedDevices} loadDevices={props.loadDevices} site={data} onLinkClick={onLinkClick} />
        } else {
            return <LinkDetails link={data} />
        }
    }

    const getDetailsIdFromUrl = () =>{
        const currentUrl = window.location.href;
        const parts = currentUrl.split(URL_BASEPATH);
        const detailsPath = parts[1].split("/details/")
        return detailsPath[1] ? detailsPath[1] : null;
    }

    const loadDetailsData = (id: string) =>{

        fetch(`${URL_API}/link/${id}`)
                .then(res => {
                    if (res.ok)
                        return res.json()
                    else
                        return Promise.reject()

                })
                .then(result => {
                    props.selectLink(result)
                    props.highlightLink(result);

                })
                .catch(error => {

                    fetch(`${URL_API}/site/${id}`)
                        .then(res => {
                            if (res.ok)
                                return res.json()
                            else return Promise.reject();
                        })
                        .then(result => { 
                            props.selectSite(result); 
                            props.highlightSite(result);
                         })
                        .catch(error =>{
                            setMessage("No element with name " + id + " found");
                            props.clearDetails();
                            props.undoMapSelection();
                        });
                })
    }


    return (<div style={{ width: '30%', background: "#bbbdbf", padding: "20px", alignSelf:"stretch" }}>
        <Paper style={{ height:"100%"}} id="site-details-panel"  >
            {
                props.breadcrumbs.length > 0 &&
                <Breadcrumbs style={{ marginLeft: "15px", marginTop: "5px" }} aria-label="breadcrumb">
                    <Link color="inherit" href="/" onClick={backClick}>
                        {props.breadcrumbs[0].id}
                    </Link>
                    <Link>
                        {props.data?.name}
                    </Link>
                </Breadcrumbs>
            }
            {
                props.data !== null ?
                    createDetailPanel(props.data)
                    : <Typography style={{ marginTop: "5px" }} align="center" variant="body1">{message}</Typography>

            }
        </Paper>
    </div>)
}

type porps = RouteComponentProps & Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

//select always via details?
const mapStateToProps = (state: IApplicationStoreState) => ({
    data: state.network.details?.data,
    breadcrumbs: state.network.details.history,
    updatedDevices: state.network.details.checkedDevices
});

const mapDispatchToProps = (dispatcher: IDispatcher) => ({
    selectSite: (site: site) => dispatcher.dispatch(new SelectSiteAction(site)),
    selectLink: (link: link) => dispatcher.dispatch(new SelectLinkAction(link)),
    clearDetails: () => dispatcher.dispatch(new ClearDetailsAction()),
    addHistory: (newEntry: HistoryEntry) => dispatcher.dispatch(new AddToHistoryAction(newEntry)),
    clearHistory: () => dispatcher.dispatch(new ClearHistoryAction()),
    highlightLink: (link: link) => dispatcher.dispatch(new HighlightLinkAction(link)),
    highlightSite: (site: site) => dispatcher.dispatch(new HighlightSiteAction(site)),
    loadDevices: async (networkElements: Device[]) => { await dispatcher.dispatch(CheckDeviceList(networkElements)) },
    navigateToApplication: (applicationName: string, path?: string) => dispatcher.dispatch(new NavigateToApplication(applicationName, path, "test3")),
    undoMapSelection: () => dispatcher.dispatch(new RemoveHighlightingAction())

})


export default withRouter(connect(mapStateToProps, mapDispatchToProps)(Details));