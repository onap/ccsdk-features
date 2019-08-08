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

	var ui = app.ns("ui");

	ui.SelectMenuPanel = ui.MenuPanel.extend({
		defaults: {
			items: [],		// (required) an array of menu items
			value: null
		},
		_baseCls: "uiSelectMenuPanel uiMenuPanel",
		init: function() {
			this.value = this.config.value;
			this._super();
		},
		_getItems: function() {
			return this.config.items.map( function( item ) {
				return {
					text: item.text,
					selected: this.value === item.value,
					onclick: function( jEv ) {
						var el = $( jEv.target ).closest("LI");
						el.parent().children().removeClass("selected");
						el.addClass("selected");
						this.fire( "select", this, { value: item.value } );
						this.value = item.value;
					}.bind(this)
				};
			}, this );

		}
	});

})( this.app );
