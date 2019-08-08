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

	ui.DraggablePanel = ui.AbstractPanel.extend({
		defaults: {
	//		title: ""   // (required) text for the panel title
		},

		_baseCls: "uiPanel",

		init: function() {
			this._super();
			this.body = $(this._body_template());
			this.title = $(this._title_template());
			this.el = $.joey( this._main_template() );
			this.el.css( { width: this.config.width } );
			this.dd = new app.ux.DragDrop({
				pickupSelector: this.el.find(".uiPanel-titleBar"),
				dragObj: this.el
			});
			// open the panel if set in configuration
			this.config.open && this.open();
		},

		setBody: function(body) {
				this.body.empty().append(body);
		},
		_body_template: function() { return { tag: "DIV", cls: "uiPanel-body", css: { height: this.config.height + (this.config.height === 'auto' ? "" : "px" ) }, children: [ this.config.body ] }; },
		_title_template: function() { return { tag: "SPAN", cls: "uiPanel-title", text: this.config.title }; },
		_main_template: function() { return (
			{ tag: "DIV", id: this.id(), cls: this._baseCls, children: [
				{ tag: "DIV", cls: "uiPanel-titleBar", children: [
					{ tag: "DIV", cls: "uiPanel-close", onclick: this._close_handler, text: "x" },
					this.title
				]},
				this.body
			] }
		); }
	});

})( this.jQuery, this.app );
