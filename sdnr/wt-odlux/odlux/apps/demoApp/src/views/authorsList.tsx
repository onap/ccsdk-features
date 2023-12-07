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
import React from 'react';
import { withRouter, RouteComponentProps } from 'react-router-dom';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper'; // means border

import { connect } from '../../../../framework/src/flux/connect';

import { loadAllAuthorsAsync } from '../actions/authorActions';
import { IAuthor } from '../models/author';

interface IAuthorsListProps {
  authors: IAuthor[];
  busy: boolean;
  onLoadAllAuthors: () => void;
}

class AuthorsListComponent extends React.Component<RouteComponentProps & IAuthorsListProps> {

  render(): JSX.Element {
    const { authors, busy } = this.props;
    return busy
      ? (
        <Paper>
          Loading
        </Paper>
      )
      : (
      <Paper>
        <Table padding="normal" >
          <TableHead>
            <TableRow>
              <TableCell align="right">Id</TableCell>
              <TableCell >First Name</TableCell>
              <TableCell >Last Name</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {authors.map(author => (
              <TableRow key={author.id} onClick={(_e) => this.editAuthor(author)}>
                <TableCell>{author.id}</TableCell>
                <TableCell>{author.firstName}</TableCell>
                <TableCell>{author.lastName}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
      );
  }

  public componentDidMount() {
    this.props.onLoadAllAuthors();
  }

  private editAuthor = (author: IAuthor) => {
    if (author) this.props.history.push(this.props.match.path + '/' + author.id);
  };
}

export const AuthorsList = withRouter(
  connect(
    ({ demo: state }) => ({
      authors: state.listAuthors.authors,
      busy: state.listAuthors.busy,
    }),
    (dispatcher) => ({
      onLoadAllAuthors: () => {
        dispatcher.dispatch(loadAllAuthorsAsync);
      },
    }))(AuthorsListComponent));
export default AuthorsList;
