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

	var services = app.ns("services");
	var ux = app.ns("ux");

	services.ClusterState = ux.Observable.extend({
		defaults: {
			cluster: null
		},
		init: function() {
			this._super();
			this.cluster = this.config.cluster;
			this.clusterState = null;
			this.status = null;
			this.nodeStats = null;
			this.clusterNodes = null;
		},
		refresh: function() {
			var self = this, clusterState, status, nodeStats, clusterNodes, clusterHealth;
			function updateModel() {
				if( clusterState && status && nodeStats && clusterNodes && clusterHealth ) {
					this.clusterState = clusterState;
					this.status = status;
					this.nodeStats = nodeStats;
					this.clusterNodes = clusterNodes;
					this.clusterHealth = clusterHealth;
					this.fire( "data", this );
				}
			}
			this.cluster.get("_cluster/state", function( data ) {
				clusterState = data;
				updateModel.call( self );
			});
			this.cluster.get("_stats", function( data ) {
				status = data;
				updateModel.call( self );
			});
			this.cluster.get("_nodes/stats", function( data ) {
				nodeStats = data;
				updateModel.call( self );
			});
			this.cluster.get("_nodes", function( data ) {
				clusterNodes = data;
				updateModel.call( self );
			});
			this.cluster.get("_cluster/health", function( data ) {
				clusterHealth = data;
				updateModel.call( self );
			});
		},
		_clusterState_handler: function(state) {
			this.clusterState = state;
			this.redraw("clusterState");
		},
		_status_handler: function(status) {
			this.status = status;
			this.redraw("status");
		},
		_clusterNodeStats_handler: function(stats) {
			this.nodeStats = stats;
			this.redraw("nodeStats");
		},
		_clusterNodes_handler: function(nodes) {
			this.clusterNodes = nodes;
			this.redraw("clusterNodes");
		},
		_clusterHealth_handler: function(health) {
			this.clusterHealth = health;
			this.redraw("status");
		}
	});

})( this.app );
