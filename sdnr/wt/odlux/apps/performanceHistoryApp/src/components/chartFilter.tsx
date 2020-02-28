import * as React from 'react';
import { makeStyles, TextField, Typography, Select, MenuItem, FormControl, InputLabel } from '@material-ui/core';

const styles = makeStyles({
    filterInput: {
        marginRight: "15px"
    },
    filterContainer: {
        marginLeft: "90px"
    }
});

type filterProps = { isVisible: boolean, onFilterChanged: (property: string, filterTerm: string) => void, filters: any };
//put chart visibility into redux
const ChartFilter: React.FunctionComponent<filterProps> = (props) => {

    //get filter from redux state (just pass da object?), onfilterchange
    const classes = styles();

    return (
        <>
            {
                props.isVisible &&
                <div className={classes.filterContainer}>
                    <TextField className={classes.filterInput} label="Radio Signal" value={props.filters.radioSignalId || ''} onChange={(event) => props.onFilterChanged("radioSignalId", event.target.value)} InputLabelProps={{
                        shrink: true,
                    }} />
                    <TextField className={classes.filterInput} label="Scanner ID" value={props.filters.scannerId || ''} onChange={(event) => props.onFilterChanged("scannerId", event.target.value)} InputLabelProps={{
                        shrink: true,
                    }} />
                    <TextField className={classes.filterInput} label="End Time" value={props.filters.timeStamp || ''} onChange={(event) => props.onFilterChanged("timeStamp", event.target.value)} InputLabelProps={{
                        shrink: true,
                    }} />
                    <FormControl>
                        <InputLabel id="suspect-interval-label" shrink>Suspect Interval</InputLabel>

                        <Select labelId="suspect-interval-label" value={props.filters.suspectIntervalFlag || ''} onChange={(event) => props.onFilterChanged("suspectIntervalFlag", event.target.value as string)}>
                            <MenuItem value={undefined}>None</MenuItem>
                            <MenuItem value={"true"}>true</MenuItem>
                            <MenuItem value={"false"}>false</MenuItem>
                        </Select>
                    </FormControl>
                </ div>
            }
        </>
    )

}

export default ChartFilter;