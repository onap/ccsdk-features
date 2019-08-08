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

	ui.ResultTable = ui.Table.extend({
		defaults: {
			width: 500,
			height: 400
		},

		init: function() {
			this._super();
			this.on("rowClick", this._showPreview_handler);
			this.selectedRow = null;
			$(document).bind("keydown", this._nav_handler);
		},
		remove: function() {
			$(document).unbind("keydown", this._nav_handler);
			this._super();
		},
		attach: function(parent) {
			if(parent) {
				var height = parent.height() || ( $(document).height() - parent.offset().top - 41 ); // 41 = height in px of .uiTable-tools + uiTable-header
				var width = parent.width();
				this.el.width( width );
				this.body.width( width ).height( height );
			}
			this._super(parent);
		},
		showPreview: function(row) {
			row.addClass("selected");
			this.preview = new app.ui.JsonPanel({
				title: i18n.text("Browser.ResultSourcePanelTitle"),
				json: row.data("row")._source,
				onClose: function() { row.removeClass("selected"); }
			});
		},
		_nav_handler: function(jEv) {
			if(jEv.keyCode !== 40 && jEv.keyCode !== 38) {
				return;
			}
			this.selectedRow && this.preview && this.preview.remove();
			if(jEv.keyCode === 40) { // up arrow
				this.selectedRow = this.selectedRow ? this.selectedRow.next("TR") : this.body.find("TR:first");
			} else if(jEv.keyCode === 38) { // down arrow
				this.selectedRow = this.selectedRow ? this.selectedRow.prev("TR") : this.body.find("TR:last");
			}
			this.selectedRow && this.showPreview(this.selectedRow);
		},
		_showPreview_handler: function(obj, data) {
			this.showPreview(this.selectedRow = data.row);
		}
	});

})( this.jQuery, this.app );
