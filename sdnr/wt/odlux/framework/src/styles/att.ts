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

import { createTheme, adaptV4Theme } from '@mui/material/styles';

const theme = createTheme(adaptV4Theme({
  design: {
    id: "att",
    name: "AT&T",
    url: "https://pmcvariety.files.wordpress.com/2016/04/att_logo.jpg?w=1000&h=563&crop=1",
    height: 70,
    width: 150,
    logoHeight: 60,
  },
  palette: {
    primary: {
      light: "#f2f2f29c",
      main: "#f2f2f2",
      dark: "#d5d5d5",
      contrastText: "#0094d3"
    },
    secondary: {
      light: "#f2f2f2",
      main: "rgba(51, 171, 226, 1)",
      dark: "rgba(41, 159, 213, 1)",
      contrastText: "#0094d3"
    }
  },
}));

export default theme;
