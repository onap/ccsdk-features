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

	ui.TextField = ui.AbstractField.extend({
		init: function() {
			this._super();
		},
		_keyup_handler: function() {
			this.fire("change", this );
		},
		_main_template: function() {
			return { tag: "DIV", id: this.id(), cls: "uiField uiTextField", children: [
				{ tag: "INPUT",
					type: "text",
					name: this.config.name,
					placeholder: this.config.placeholder,
					onkeyup: this._keyup_handler
				}
			]};
		}
	});

})( this.app );
