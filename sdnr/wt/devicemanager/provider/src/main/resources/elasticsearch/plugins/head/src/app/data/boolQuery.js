/**
 * Copyright 2010-2013 Ben Birch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this software except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function( app ) {

	var data = app.ns("data");
	var ux = app.ns("ux");

	data.BoolQuery = ux.Observable.extend({
		defaults: {
			size: 50		// size of pages to return
		},
		init: function() {
			this._super();
			this.refuid = 0;
			this.refmap = {};
			this.search = {
				query: { bool: { must: [], must_not: [], should: [] } },
				from: 0,
				size: this.config.size,
				sort: [],
				aggs: {}
			};
			this.defaultClause = this.addClause();
		},
		setSize: function(size) {
			this.search.size = parseInt( size, 10 );
		},
		setPage: function(page) {
			this.search.from = this.config.size * (page - 1) + 1;
		},
		addClause: function(value, field, op, bool) {
			bool = bool || "should";
			op = op || "match_all";
			field = field || "_all";
			var clause = this._setClause(value, field, op, bool);
			var uqid = "q-" + this.refuid++;
			this.refmap[uqid] = { clause: clause, value: value, field: field, op: op, bool: bool };
			if(this.search.query.bool.must.length + this.search.query.bool.should.length > 1) {
				this.removeClause(this.defaultClause);
			}
			this.fire("queryChanged", this, { uqid: uqid, search: this.search} );
			return uqid; // returns reference to inner query object to allow fast updating
		},
		removeClause: function(uqid) {
			var ref = this.refmap[uqid],
				bool = this.search.query.bool[ref.bool];
			var clauseIdx = bool.indexOf(ref.clause);
			// Check that this clause hasn't already been removed
			if (clauseIdx >=0) {
				bool.splice(clauseIdx, 1);
			}
		},
		_setClause: function(value, field, op, bool) {
			var clause = {}, query = {};
			if(op === "match_all") {
			} else if(op === "query_string") {
				query["default_field"] = field;
				query["query"] = value;
			} else if(op === "missing") {
				op = "constant_score"
				var missing = {}, filter = {};
				missing["field"] = field;
				filter["missing"] = missing
				query["filter"] = filter;
			} else {
				query[field.substring(field.indexOf(".")+1)] = value;
			}
			clause[op] = query;
			this.search.query.bool[bool].push(clause);
			return clause;
		},
		getData: function() {
			return JSON.stringify(this.search);
		}
	});

})( this.app );