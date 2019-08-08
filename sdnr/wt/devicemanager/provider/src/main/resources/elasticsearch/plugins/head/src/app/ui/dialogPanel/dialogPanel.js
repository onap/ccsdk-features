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

	ui.DialogPanel = ui.DraggablePanel.extend({
		_commit_handler: function(jEv) {
			this.fire("commit", this, { jEv: jEv });
		},
		_main_template: function() {
			var t = this._super();
			t.children.push(this._actionsBar_template());
			return t;
		},
		_actionsBar_template: function() {
			return { tag: "DIV", cls: "pull-right", children: [
				new app.ui.Button({ label: "Cancel", onclick: this._close_handler }),
				new app.ui.Button({ label: "OK", onclick: this._commit_handler })
			]};
		}
	});

})( this.app );
