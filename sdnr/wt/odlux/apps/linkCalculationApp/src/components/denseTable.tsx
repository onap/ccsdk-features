import * as React from 'react';
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import { makeStyles, Button, Tooltip } from '@material-ui/core';

type props = { headers: string[], width: number, height:number,  navigate?(applicationName: string, path?: string):void, onLinkClick?(id: string): void, data: any[], hover: boolean, onClick?(id: string): void, actions?:boolean  };


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
        <Paper style={{borderRadius:"0px", width:props.width, height:props.height}} className={classes.container}>
       
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

                        
                        var filteredRows = Object.keys(row).filter(function(e) { if(e!=="simulatorId") return row });
                     
                        //var filteredRows = Object.keys(row).filter(function(e) { if(e!=="simulatorId") return row[e] });
                        var values = Object.keys(row).map(function(e) {  if(e!=="simulatorId"){ return row[e];} else return undefined });
                       

                        return (
                            <TableRow key={index} hover={props.hover} onMouseOver={e => handleHover(e,row.name)} onClick={ e =>  handleClick(e, row.name)}>

                                {
                                    values.map((data:any) => {
                                       
                                        if(data!== undefined)
                                        return <TableCell >  {data} </TableCell>
                                        else
                                        return null;
                                    })
                                }
                                {

                                    props.actions && <TableCell >  
<div style={{display:"flex"}}>                                           
    <Tooltip title="Configure">
    <Button className={classes.button} disabled={row.status!=="connected"} onClick={(e: any) =>{ e.preventDefault(); e.stopPropagation(); props.navigate && props.navigate("configuration", row.simulatorId ? row.simulatorId : row.name)}}>C</Button>
    </Tooltip>
    <Tooltip title="Fault">
    <Button className={classes.button} onClick={(e: any) =>{ e.preventDefault(); e.stopPropagation(); props.navigate && props.navigate("fault", row.simulatorId ? row.simulatorId : row.name)}}>F</Button>
    </Tooltip>
    </div> 
    </TableCell>
    
                                }
                            </TableRow>)
                    })
                    }

                </TableBody>
            </Table>
       
        </Paper>
    );

}

export default DenseTable;