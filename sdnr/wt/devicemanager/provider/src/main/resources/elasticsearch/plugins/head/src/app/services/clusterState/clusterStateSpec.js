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
describe("app.services.ClusterState", function() {

	var ClusterState = window.app.services.ClusterState;
	var test = window.test;

	var c;
	var dummyData = {};
	var dataEventCallback;

	function expectAllDataToBeNull() {
		expect( c.clusterState ).toBe( null );
		expect( c.status ).toBe( null );
		expect( c.nodeStats ).toBe( null );
		expect( c.clusterNodes ).toBe( null );
	}

	beforeEach( function() {
		test.cb.use();
		dataEventCallback = jasmine.createSpy("onData");
		c = new ClusterState({
			cluster: {
				get: test.cb.createSpy("get", 1, [ dummyData ] )
			},
			onData: dataEventCallback
		});
	});

	describe( "when it is initialised", function() {

		it("should have null data", function() {
			expectAllDataToBeNull();
		});

	});

	describe( "when refresh is called", function() {

		beforeEach( function() {
			c.refresh();
		});

		it("should not not update models until all network requests have completed", function() {			
			test.cb.execOne();
			expectAllDataToBeNull();
			test.cb.execOne();
			expectAllDataToBeNull();
			test.cb.execOne();
			expectAllDataToBeNull();
			test.cb.execOne();
			expectAllDataToBeNull();
			test.cb.execOne();
			expect( c.clusterState ).toBe( dummyData );
			expect( c.status ).toBe( dummyData );
			expect( c.nodeStats ).toBe( dummyData );
			expect( c.clusterNodes ).toBe( dummyData );
		});

		it("should fire a 'data' event when all data is ready", function() {
			test.cb.execAll();
			expect( dataEventCallback ).toHaveBeenCalledWith( c );
		});
	});

});
