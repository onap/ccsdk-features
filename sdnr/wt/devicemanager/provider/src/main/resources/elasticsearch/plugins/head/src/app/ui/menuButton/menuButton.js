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

	var ui = app.ns("ui");

	ui.MenuButton = app.ui.Button.extend({
		defaults: {
			menu: null
		},
		_baseCls: "uiButton uiMenuButton",
		init: function(parent) {
			this._super(parent);
			this.menu = this.config.menu;
			this.on("click", this.openMenu_handler);
			this.menu.on("open", function() { this.el.addClass("active"); }.bind(this));
			this.menu.on("close", function() { this.el.removeClass("active"); }.bind(this));
		},
		openMenu_handler: function(jEv) {
			this.menu && this.menu.open(jEv);
		}
	});

})( this.jQuery, this.app );
