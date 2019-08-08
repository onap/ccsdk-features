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

	ui.Button = ui.AbstractWidget.extend({
		defaults : {
			label: "",                 // the label text
			disabled: false,           // create a disabled button
			autoDisable: false         // automatically disable the button when clicked
		},

		_baseCls: "uiButton",

		init: function(parent) {
			this._super();
			this.el = $.joey(this.button_template())
				.bind("click", this.click_handler);
			this.config.disabled && this.disable();
			this.attach( parent );
		},

		click_handler: function(jEv) {
			if(! this.disabled) {
				this.fire("click", jEv, this);
				this.config.autoDisable && this.disable();
			}
		},

		enable: function() {
			this.el.removeClass("disabled");
			this.disabled = false;
			return this;
		},

		disable: function(disable) {
			if(disable === false) {
					return this.enable();
			}
			this.el.addClass("disabled");
			this.disabled = true;
			return this;
		},

		button_template: function() { return (
			{ tag: 'BUTTON', type: 'button', id: this.id(), cls: this._baseCls, children: [
				{ tag: 'DIV', cls: 'uiButton-content', children: [
					{ tag: 'DIV', cls: 'uiButton-label', text: this.config.label }
				] }
			] }
		); }
	});

})( this.jQuery, this.joey, this.app );
