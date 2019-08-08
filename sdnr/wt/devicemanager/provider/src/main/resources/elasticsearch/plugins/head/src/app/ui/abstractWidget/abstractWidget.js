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
(function( $, joey, app ) {

	var ui = app.ns("ui");
	var ux = app.ns("ux");

	ui.AbstractWidget = ux.Observable.extend({
		defaults : {
			id: null     // the id of the widget
		},

		el: null,       // this is the jquery wrapped dom element(s) that is the root of the widget

		init: function() {
			this._super();
			for(var prop in this) {       // automatically bind all the event handlers
				if(prop.contains("_handler")) {
					this[prop] = this[prop].bind(this);
				}
			}
		},

		id: function(suffix) {
			return this.config.id ? (this.config.id + (suffix ? "-" + suffix : "")) : undefined;
		},

		attach: function( parent, method ) {
			if( parent ) {
				this.el[ method || "appendTo"]( parent );
			}
			this.fire("attached", this );
			return this;
		},

		remove: function() {
			this.el.remove();
			this.fire("removed", this );
			this.removeAllObservers();
			this.el = null;
			return this;
		}
	});

	joey.plugins.push( function( obj ) {
		if( obj instanceof ui.AbstractWidget ) {
			return obj.el[0];
		}
	});

})( this.jQuery, this.joey, this.app );
