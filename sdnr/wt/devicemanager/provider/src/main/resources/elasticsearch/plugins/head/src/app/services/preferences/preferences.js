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
	
	var ux = app.ns("ux");
	var services = app.ns("services");

	services.Preferences = ux.Singleton.extend({
		init: function() {
			this._storage = window.localStorage;
			this._setItem("__version", 1 );
		},
		get: function( key ) {
			return this._getItem( key );
		},
		set: function( key, val ) {
			return this._setItem( key, val );
		},
		_getItem: function( key ) {
			try {
				return JSON.parse( this._storage.getItem( key ) );
			} catch(e) {
				console.warn( e );
				return undefined;
			}
		},
		_setItem: function( key, val ) {
			try {
				return this._storage.setItem( key, JSON.stringify( val ) );
			} catch(e) {
				console.warn( e );
				return undefined;
			}
		}
	});

})( this.app );
