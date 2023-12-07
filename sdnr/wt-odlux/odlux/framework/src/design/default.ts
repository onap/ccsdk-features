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
/******************************************************************************
 * Copyright 2018 highstreet technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *****************************************************************************/

import { createTheme, adaptV4Theme } from '@mui/material/styles';
import onapLogo from '../assets/images/onapLogo.gif'

const theme = createTheme(adaptV4Theme({
  design: {
    id: "onap",
    name: "Open Networking Automation Plattform (ONAP)",
    url: onapLogo,
    height: 49,
    width: 229,
    logoHeight: 32,
  },
  palette: {
    primary: {
      light: "#eeeeee",
      main: "#ffffff",
      dark: "#e0e0e0",
      contrastText: "#07819B"
    },
    secondary: {
      light: "rgba(7, 129, 155, 94)",
      main: "rgba(7, 129, 155, 201)",
      dark: "#07819B",
      contrastText: "#ffffff"
    },
  },
  overrides: { //temp fix for labels turning white after material new version (palette primary color)
    MuiFormLabel: {
      root: {
        "&.Mui-focused": {
          color: "rgba(143,143,143,1)"
        }
      },

      focused: {}
    },
    MuiInput: {
      underline: {

        "&:after": {
          borderBottom: "2px solid #444444"
        }
      }
    }
  },
}));

export default theme;