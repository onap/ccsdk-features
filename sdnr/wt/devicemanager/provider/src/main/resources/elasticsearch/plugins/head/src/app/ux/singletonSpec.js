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
describe("app.ux.singleton", function(){

var Singleton = window.app.ux.Singleton;

	describe("creating a singleton", function() {
		var X = Singleton.extend({
			foo: function() {
				return "bar";
			}
		});

		var Y = Singleton.extend({
			bar: function() {
				return "baz";
			}
		});

		it("should have properties like a normal class", function() {
			var a = X.instance();

			expect( a instanceof X ).toBe( true );
			expect( a.foo() ).toBe( "bar" );
		});

		it("should return single instance each time instance() is called", function() {
			var a = X.instance();
			var b = X.instance();

			expect( a ).toBe( b );
		});

		it("should not share instances with different singletons", function() {
			var a = X.instance();
			var c = Y.instance();

			expect( a ).not.toBe( c );
		});

	});

});
