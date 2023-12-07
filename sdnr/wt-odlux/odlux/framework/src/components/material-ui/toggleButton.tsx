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
import classNames from 'classnames';
import { Theme, alpha } from '@mui/material/styles';
import { WithStyles } from '@mui/styles';
import withStyles from '@mui/styles/withStyles';
import createStyles from '@mui/styles/createStyles';
import ButtonBase from '@mui/material/ButtonBase';


export const styles = (theme: Theme) => createStyles({
    /* Styles applied to the root element. */
    root: {
        ...theme.typography.button,
        height: 32,
        minWidth: 48,
        margin: 0,
        padding: `${theme.spacing(1 - 4)} ${theme.spacing(1.5)}`,
        borderRadius: 2,
        willChange: 'opacity',
        color: alpha(theme.palette.action.active, 0.38),
        '&:hover': {
            textDecoration: 'none',
            // Reset on mouse devices
            backgroundColor: alpha(theme.palette.text.primary, 0.12),
            '@media (hover: none)': {
                backgroundColor: 'transparent',
            },
            '&.Mui-disabled': {
                backgroundColor: 'transparent',
            },
        },
        '&:not(:first-child)': {
            borderTopLeftRadius: 0,
            borderBottomLeftRadius: 0,
        },
        '&:not(:last-child)': {
            borderTopRightRadius: 0,
            borderBottomRightRadius: 0,
        },
    },
    /* Styles applied to the root element if `disabled={true}`. */
    disabled: {
        color: alpha(theme.palette.action.disabled, 0.12),
    },
    /* Styles applied to the root element if `selected={true}`. */
    selected: {
        color: theme.palette.action.active,
        '&:after': {
            content: '""',
            display: 'block',
            position: 'absolute',
            overflow: 'hidden',
            borderRadius: 'inherit',
            width: '100%',
            height: '100%',
            left: 0,
            top: 0,
            pointerEvents: 'none',
            zIndex: 0,
            backgroundColor: 'currentColor',
            opacity: 0.38,
        },
        '& + &:before': {
            content: '""',
            display: 'block',
            position: 'absolute',
            overflow: 'hidden',
            width: 1,
            height: '100%',
            left: 0,
            top: 0,
            pointerEvents: 'none',
            zIndex: 0,
            backgroundColor: 'currentColor',
            opacity: 0.12,
        },
    },
    /* Styles applied to the `label` wrapper element. */
    label: {
        width: '100%',
        display: 'inherit',
        alignItems: 'inherit',
        justifyContent: 'inherit',
    },
});

export type ToggleButtonClassKey = 'disabled' | 'root' | 'label' | 'selected';

interface IToggleButtonProps extends WithStyles<typeof styles> {
    className?: string;
    component?: React.ReactType<IToggleButtonProps>;
    disabled?: boolean;
    disableFocusRipple?: boolean;
    disableRipple?: boolean;
    selected?: boolean;
    type?: string;
    value?: any;
    onClick?: (event: React.FormEvent<HTMLElement>, value?: any) => void;
    onChange?: (event: React.FormEvent<HTMLElement>, value?: any) => void;
}

class ToggleButtonComponent extends React.Component<IToggleButtonProps> {
    handleChange = (event: React.FormEvent<HTMLElement>) => {
        const { onChange, onClick, value } = this.props;

        event.stopPropagation();
        if (onClick) {
            onClick(event, value);
            if (event.isDefaultPrevented()) {
                return;
            }
        }

        if (onChange) {
            onChange(event, value);
        }
        event.preventDefault();
    };

    render() {
        const {
            children,
            className: classNameProp,
            classes,
            disableFocusRipple,
            disabled,
            selected,
            ...other
        } = this.props;

        const className = classNames(
            classes.root,
            {
                [classes.disabled]: disabled,
                [classes.selected]: selected,
            },
            classNameProp,
        );

        return (
            <ButtonBase
                className={className}
                disabled={disabled}
                focusRipple={!disableFocusRipple}
                onClick={this.handleChange}
                href="#"
                {...other}
            >
                <span className={classes.label}>{children}</span>
            </ButtonBase>
        );
    }
    public static defaultProps = {
        disabled: false,
        disableFocusRipple: false,
        disableRipple: false,
    };

    public static muiName = 'ToggleButton';
}

export const ToggleButton = withStyles(styles, { name: 'MuiToggleButton' })(ToggleButtonComponent);
export default ToggleButton;