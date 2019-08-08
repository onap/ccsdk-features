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
(function( $, app ) {

	var data = app.ns("data");
	var ux = app.ns("ux");

	data.Model = ux.Observable.extend({
		defaults: {
			data: null
		},
		init: function() {
			this.set( this.config.data );
		},
		set: function( key, value ) {
			if( arguments.length === 1 ) {
				this._data = $.extend( {}, key );
			} else {
				key.split(".").reduce(function( ptr, prop, i, props) {
					if(i === (props.length - 1) ) {
						ptr[prop] = value;
					} else {
						if( !(prop in ptr) ) {
							ptr[ prop ] = {};
						}
						return ptr[prop];
					}
				}, this._data );
			}
		},
		get: function( key ) {
			return key.split(".").reduce( function( ptr, prop ) {
				return ( ptr && ( prop in ptr ) ) ? ptr[ prop ] : undefined;
			}, this._data );
		},
	});
})( this.jQuery, this.app );
