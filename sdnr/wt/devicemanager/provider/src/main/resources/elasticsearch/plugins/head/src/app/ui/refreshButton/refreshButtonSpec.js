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
describe("app.ui.RefreshButton", function() {

	var RefreshButton = window.app.ui.RefreshButton;

	var r, refresh_handler, change_handler;

	function openMenuPanel( button, label ) {
		button.el.find("BUTTON").eq(1).click();
		$(".uiMenuPanel-label:contains(" + label + ")").click();
		test.clock.tick(); // menuPanel -> bind _close_handler
	}


	beforeEach( function() {
		test.clock.steal();
		refresh_handler = jasmine.createSpy("refresh_handler");
		change_handler = jasmine.createSpy("change_handler");
		r = new RefreshButton({
			onRefresh: refresh_handler,
			onChange: change_handler
		});
		r.attach( document.body );
	});

	afterEach( function() {
		r.remove();
		test.clock.restore();
	});

	it("should have an initial default value", function() {
		expect( r.value ).toBe( -1 );
	});

	it("should fire a refresh event after clicking the refresh button ", function() {
		r.el.find("BUTTON").eq(0).click();

		expect( refresh_handler ).toHaveBeenCalled();
	});

	it("should change the refresh rate when set it called", function() {
		r.set( 100 );
		expect( r.value ).toBe( 100 );
	});

	it("should set an interval when rate is set to a positive value", function() {
		r.set( 100 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 1 );
	});

	it("should not set an interval when rate is set to a non positive value", function() {
		r.set( -1 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 0 );
	});

	it("should fire a refresh event on intervals if refresh menu item is set to quickly", function() {
		openMenuPanel( r, "quickly" );

		expect( refresh_handler.calls.count() ).toBe( 0 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 1 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 2 );
	});

	it("should not fire refresh events when user selects Manual", function() {

		openMenuPanel( r, "quickly" );

		expect( refresh_handler.calls.count() ).toBe( 0 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 1 );

		openMenuPanel( r, "Manual" );

		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 1 );
		test.clock.tick();
		expect( refresh_handler.calls.count() ).toBe( 1 );
	});

	it("should fire a change event when a new refresh rate is selected", function() {
		openMenuPanel( r, "quickly" );
		expect( change_handler.calls.count() ).toBe( 1 );
		expect( r.value ).toBe( 100 );
		openMenuPanel( r, "Manual" );
		expect( change_handler.calls.count() ).toBe( 2 );
		expect( r.value ).toBe( -1 );
	});

});
