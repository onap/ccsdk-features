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
(function( $, app, i18n ) {

	var ui = app.ns("ui");

	ui.RefreshButton = ui.SplitButton.extend({
		defaults: {
			timer: -1
		},
		init: function( parent ) {
			this.config.label = i18n.text("General.RefreshResults");
			this._super( parent );
			this.set( this.config.timer );
		},
		set: function( value ) {
			this.value = value;
			window.clearInterval( this._timer );
			if( this.value > 0 ) {
				this._timer = window.setInterval( this._refresh_handler, this.value );
			}
		},
		_click_handler: function() {
			this._refresh_handler();
		},
		_select_handler: function( el, event ) {
			this.set( event.value );
			this.fire("change", this );
		},
		_refresh_handler: function() {
			this.fire("refresh", this );
		},
		_getItems: function() {
			return [
				{ text: i18n.text("General.ManualRefresh"), value: -1 },
				{ text: i18n.text("General.RefreshQuickly"), value: 100 },
				{ text: i18n.text("General.Refresh5seconds"), value: 5000 },
				{ text: i18n.text("General.Refresh1minute"), value: 60000 }
			];
		}
	});

})( this.jQuery, this.app, this.i18n );
