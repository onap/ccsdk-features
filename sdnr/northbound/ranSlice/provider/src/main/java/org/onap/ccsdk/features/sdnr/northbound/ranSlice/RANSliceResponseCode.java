/*-
 * ============LICENSE_START=======================================================
 * openECOMP : SDN-C
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights
 * 			reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.onap.ccsdk.features.sdnr.northbound.ranSlice;

public enum RANSliceResponseCode {

		// Accepted category
		ACCEPT_ACCEPTED(100),
		// Error category
		ERROR_UNEXPECTED_ERROR(200),
		// Rejected category
		REJECT_REJECTED(300),
		REJECT_INVALID_INPUT(301),
		REJECT_MISSING_PARAM(302),
		REJECT_PARSING_FAILED(303),
		REJECT_NO_TRANSITION(304),
		REJECT_ACTION_NOT_SUPPORTED(305),
		REJECT_VNF_NOT_FOUND(306),
		REJECT_DG_NOT_FOUND(307),
		REJECT_WORKFLOW_NOT_FOUND(308),
		REJECT_UNSTABLE_VNF(309),
		REJECT_LOCKING_FAILURE(310),
		REJECT_EXPIRED_REQUEST(311),
		REJECT_DUPLICATE_REQUEST(312),
		REJECT_MISSING_AAI_DATA(313),
		REJECT_MULTIPLE_REQUESTS_FOR_SEARCH(315),
		REJECT_POLICY_VALIDATION_FAILURE(316),
		// Success category
		SUCCESS(400),
		// Failure category
		FAILURE_DG_FAILURE(401),
		FAILURE_NO_TRANSITION(402),
		FAILURE_AAI_FAILURE(403),
		FAILURE_EXPIRED_REQUEST(404),
		FAILURE_UNEXPECTED_FAILURE(405),
		FAILURE_UNSTABLE_VNF(406),
		FAILURE_REQUEST_NOT_SUPPORTED(450),
		// Partial success
		PARTIAL_SUCCESS(500);



		private int value;
		private RANSliceResponseCode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}


}
