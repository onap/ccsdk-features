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
describe("clusterConnect", function() {

	var ClusterConnect = window.app.ui.ClusterConnect;

	describe("when created", function() {

		var prefs, success_callback, cluster, clusterConnect;

		beforeEach( function() {
			prefs = {
				set: jasmine.createSpy("set")
			};
			spyOn( window.app.services.Preferences, "instance" ).and.callFake( function() {
				return prefs;
			});
			cluster = {
				get: jasmine.createSpy("get").and.callFake( function(uri, success) {
					success_callback = success;
				})
			};
			clusterConnect = new ClusterConnect({
				base_uri: "http://localhost:9200",
				cluster: cluster
			});
		});

		it("should test the connection to the cluster", function() {
			expect( cluster.get ).toHaveBeenCalled();
		});

		it("should store successful connection in preferences", function() {
			success_callback("fakePayload");
			expect( prefs.set ).toHaveBeenCalled();
		});

	});

});
