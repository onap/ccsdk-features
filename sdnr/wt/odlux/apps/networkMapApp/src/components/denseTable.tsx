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
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles, Button, Tooltip } from '@material-ui/core';

type props = { headers: string[], height:number,  navigate?(applicationName: string, path?: string):void, onLinkClick?(id: string): void, data: any[], hover: boolean, ariaLabel: string, onClick?(id: string): void, actions?:boolean  };


const styles = makeStyles({
    container: {
        overflow:"auto"
    },
    button: {
        margin: 0,
        padding: "6px 6px",
        minWidth: 'unset'
      }
    
  });
  

const DenseTable: React.FunctionComponent<props> = (props) => {

    const classes = styles();

    const handleClick = (event: any, id: string) =>{
        event.preventDefault();
        props.onClick !== undefined && props.onClick(id);

    }

    const handleHover = (event: any, id: string) =>{
        event.preventDefault();

    }

    return (
        <Paper style={{borderRadius:"0px"}}>
       <div style={{ height:props.height, overflow:"auto"}}>
            <Table stickyHeader size="small" aria-label="a dense table" >
                <TableHead>
                    <TableRow>
                        {
                            props.headers.map((data) => {
                                return <TableCell>{data}</TableCell>
                            })
                        }
                    </TableRow>
                </TableHead>
                <TableBody>
                    {props.data.map((row, index) => {  
                        var values = Object.keys(row).map(function(e) { return row[e] });
                        return (
                            <TableRow aria-label={props.ariaLabel} key={index} hover={props.hover} onMouseOver={e => handleHover(e,row.name)} onClick={ e =>  handleClick(e, row.name)}>

                                {
                                    values.map((data:any) => {
                                       
                                        if(data!== undefined)
                                        return <TableCell>  {data} </TableCell>
                                        else
                                        return null;
                                    })
                                }
                                {

                                    props.actions && <TableCell >  
<div style={{display:"flex"}}>
    <Tooltip title="Configure">
    <Button className={classes.button} disabled={row.status!=="connected"} onClick={(e: any) =>{ e.preventDefault(); e.stopPropagation(); props.navigate && props.navigate("configuration", row.name)}}>C</Button>
    </Tooltip>
    <Tooltip title="Fault">
    <Button className={classes.button} disabled={row.status!=="connected"} onClick={(e: any) =>{ e.preventDefault(); e.stopPropagation(); props.navigate && props.navigate("fault", row.name)}}>F</Button>
    </Tooltip>
    </div> 
    </TableCell>
    
                                }
                            </TableRow>)
                    })
                    }

                </TableBody>
            </Table>
            </div>
        </Paper>
    );

}

export default DenseTable;