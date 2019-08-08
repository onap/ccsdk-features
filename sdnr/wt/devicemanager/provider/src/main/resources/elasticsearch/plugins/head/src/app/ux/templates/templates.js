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

	var ut = app.ns("ut");

	ut.option_template = function(v) { return { tag: "OPTION", value: v, text: v }; };

	ut.require_template = function(f) { return f.require ? { tag: "SPAN", cls: "require", text: "*" } : null; };


	var sib_prefix = ['B','ki','Mi', 'Gi', 'Ti', 'Pi', 'Ei', 'Zi', 'Yi'];

	ut.byteSize_template = function(n) {
		var i = 0;
		while( n >= 1000 ) {
			i++;
			n /= 1024;
		}
		return (i === 0 ? n.toString() : n.toFixed( 3 - parseInt(n,10).toString().length )) + ( sib_prefix[ i ] || "..E" );
	};

	var sid_prefix = ['','k','M', 'G', 'T', 'P', 'E', 'Z', 'Y'];

	ut.count_template = function(n) {
		var i = 0;
		while( n >= 1000 ) {
			i++;
			n /= 1000;
		}
		return i === 0 ? n.toString() : ( n.toFixed( 3 - parseInt(n,10).toString().length ) + ( sid_prefix[ i ] || "..E" ) );
	};

})( this.app );
