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
$( function() {

	var ui = window.app.ns("ui");
	var ux = window.app.ns("ux");
	var ut = window.app.ns("ut");

	window.builder = function() {
		var form = new ux.FieldCollection({
			fields: [
				new ui.CheckField({
					label: "default",
					name: "check_default"
				}),
				new ui.CheckField({
					label: "checked",
					name: "check_true",
					value: true
				}),
				new ui.CheckField({
					label: "unchecked",
					name: "check_false",
					value: false
				}),
				new ui.CheckField({
					label: "required",
					name: "check_required",
					require: true
				})
			]
		});

		return (
			{ tag: "DIV", children: form.fields.map( function( field ) {
				return { tag: "LABEL", cls: "uiPanelForm-field", children: [
					{ tag: "DIV", cls: "uiPanelForm-label", children: [ field.label, ut.require_template(field) ] },
					field
				]};
			}).concat( new ui.Button({
				label: "Evaluate Form",
				onclick: function() { console.log( "valid=" + form.validate(), form.getData() ); }
			})) }
		);
	};

});