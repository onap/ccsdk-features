import * as React from 'react'

import { IApplicationStoreState } from "../../../../framework/src/store/applicationStore";
import connect, { IDispatcher, Connect } from "../../../../framework/src/flux/connect";
import { Paper, Typography } from "@material-ui/core";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faExclamationTriangle } from '@fortawesome/free-solid-svg-icons';


type props = Connect<typeof mapStateToProps, typeof mapDispatchToProps>;

const ConnectionInfo: React.FunctionComponent<props> = (props) => {

    return (
        (props.isCalculationServerReachable === false)?  <Paper style={{padding:5, position: 'absolute', top: 160, width: 230, left:"40%"}}>
        <div style={{display: 'flex', flexDirection: 'column'}}>
        <div style={{'alignSelf': 'center', marginBottom:5}}> <Typography> <FontAwesomeIcon icon={faExclamationTriangle} /> Connection Error</Typography></div>
        {props.isCalculationServerReachable === false && <Typography> Calculation data can't be loaded.</Typography>}
        </div>
    </Paper> : null
)}

const mapStateToProps = (state: IApplicationStoreState) => ({
    isCalculationServerReachable: state.linkCalculation.calculations.reachable
});



const mapDispatchToProps = (dispatcher: IDispatcher) => ({

    //zoomToSearchResult: (lat: number, lon: number) => dispatcher.dispatch(new ZoomToSearchResultAction(lat, lon))

});;


export default connect(mapStateToProps,mapDispatchToProps)(ConnectionInfo)

