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

import * as React from "react";
import makeStyles from '@mui/styles/makeStyles';

export const getTypeName = (obj: any): string => {
  if (obj == null) {
    return obj === undefined ? "Undefined" : "Null";
  }
  return Object.prototype.toString.call(obj).slice(8, -1);
};

const isObjectLike = (obj: any) => {
  return typeof obj === "object" && obj !== null;
};

const isBoolean = (obj: any) => {
  return obj === true || obj === false ||
    (isObjectLike(obj) && getTypeName(obj) === "Boolean");
};

const isNumber = (obj: any) => {
  return typeof obj === "number" ||
    (isObjectLike(obj) && getTypeName(obj) === "Number");
};

const isString = (obj: any) => {
  return typeof obj === "string" ||
    (isObjectLike(obj) && getTypeName(obj) === "String");
};

const isNull = (obj: any) => {
  return obj === null;
};

const isDate = (obj: any): boolean => {
  return isObjectLike(obj) && (obj instanceof Date);
};

const useSimpleTableStyles = makeStyles({
  root: {
  },
  table: {
    fontFamily: "verdana, arial, helvetica, sans-serif",
    borderSpacing: "3px",
    borderCollapse: "separate",
    marginLeft: "30px"
  },
  label: {
    cursor: "pointer",
  },
  th: {
    textAlign: "left",
    color: "white",
    padding: "5px",
    backgroundColor: "#cccccc",

  },
  td: {
    verticalAlign: "top",
    padding: "0.5rem 1rem",
    border: "2px solid #DDD"
  },
  object: {
  },
  objectTh: {
    backgroundColor: "#cccccc",
  },
  objectTd: {
    padding: "0.5rem 1rem",
    border: "2px solid #DDD"
  },
  chevron: {
    '&:before': {
      borderStyle: 'solid',
      borderWidth: '0.25em 0.25em 0 0',
      content: '\'\'',
      display: 'inline-block',
      height: '0.45em',
      left: '0.15em',
      position: 'relative',
      top: '0.15em',
      transform: 'rotate(-45deg)',
      transition: 'all 0.3s',
      verticalAlign: 'top',
      width: '0.45em',
    }

  },
    right: {
    '&:before': {
      left: '0',
      transform: 'rotate(45deg)',
    }
  },
  bottom: {
    '&:before': {
      left: '0',
      transform: 'rotate(135deg)',
    }
  },
});


type SimpleTableProps = {
  classNameTh?: string;
  label?: JSX.Element | string | null;
  cols?: number;
  expand?: boolean;
  ariaLabel?: string;
}

const SimpleTable: React.FC<SimpleTableProps> = (props) => {
  const { label = '', cols = 2, expand = true, classNameTh, children } = props;
  const [isExpanded, setIsExpanded] = React.useState(expand);

  const classes = useSimpleTableStyles();

  React.useEffect(() => {
    setIsExpanded(expand);
  }, [expand]);

  const handleClick = () => {
    setIsExpanded(!isExpanded);
  };

  return (
    <table className={`${classes.root} ${classes.table}`} aria-label={props.ariaLabel? props.ariaLabel+'-table' : 'table'}>
      {label && (<thead>
        <tr aria-label={props.ariaLabel? props.ariaLabel+'-title-row' : 'title row'}>
          <th className={`${classes.th} ${classes.label} ${classNameTh ? classNameTh : ''}`} colSpan={cols} onClick={handleClick}>
            <span className={`${classes.chevron} ${isExpanded ? classes.bottom : classes.right }`}></span> { label }
          </th>
        </tr>
      </thead>) || null
      }
      {isExpanded && <tbody >{children}</tbody> || null}
    </table>
  );
};


type ObjectRendererProps = {
  className?: string;
  label?: JSX.Element | string | null;
  expand?: boolean;
  object: { [key: string]: any };
  ariaLabel?: string;
};

const ObjectRenderer: React.FC<ObjectRendererProps> = (props) => {
  const { object, className, label = 'Object', expand = true } = props;
  const classes = useSimpleTableStyles();

  return (
    <SimpleTable ariaLabel={props.ariaLabel} classNameTh={classes.objectTh} label={getTypeName(object) || label} expand={expand}>
      {
        Object.keys(object).map(key => {
          return (
            <tr key={String(key)} aria-label={props.ariaLabel? props.ariaLabel+'-row': 'row'}>
              <td className={`${classes.td} ${classes.objectTd}`} aria-label="object-title">{String(key)} </td>
              <td className={`${classes.td}`} aria-label="object-details">{renderObject(object[key], "sub-element")}</td>
            </tr>
          );
        })
      }
    </SimpleTable>
  );
};


type ArrayRendererProps = {
  label?: JSX.Element | string | null;
  extraRenderer?: { [label: string]: React.ComponentType<{ label?: JSX.Element | string | null; object: any; }> };
  description?: string;
  object: any;
};

const ArrayRenderer: React.FC<ArrayRendererProps> = (props) => {

  return null;
};

export const renderObject = (object: any, ariaLabel?: string): JSX.Element | string => {
  if (isString(object) || isNumber(object) || isBoolean(object)) {
    return String(object);
  }
  return <ObjectRenderer object={object} ariaLabel={ariaLabel} />;
};
