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
	var ut = app.ns("ut");

	ui.PanelForm = ui.AbstractWidget.extend({
		defaults: {
			fields: null	// (required) instanceof app.ux.FieldCollection
		},
		init: function(parent) {
			this._super();
			this.el = $.joey(this._main_template());
			this.attach( parent );
		},
		_main_template: function() {
			return { tag: "DIV", id: this.id(), cls: "uiPanelForm", children: this.config.fields.fields.map(this._field_template, this) };
		},
		_field_template: function(field) {
			return { tag: "LABEL", cls: "uiPanelForm-field", children: [
				{ tag: "DIV", cls: "uiPanelForm-label", children: [ field.label, ut.require_template(field) ] },
				field
			]};
		}
	});

})( this.jQuery, this.app );
