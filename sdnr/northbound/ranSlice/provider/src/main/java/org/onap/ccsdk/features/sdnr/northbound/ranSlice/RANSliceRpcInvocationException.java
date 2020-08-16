package org.onap.ccsdk.features.sdnr.northbound.ranSlice;

import org.onap.ccsdk.sli.core.sli.SvcLogicException;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.common.header.CommonHeader;
import org.opendaylight.yang.gen.v1.org.onap.ccsdk.rev200806.status.Status;

public class RANSliceRpcInvocationException extends SvcLogicException {

	private Status status;
	private CommonHeader commonHeader;

	public RANSliceRpcInvocationException(Status status, CommonHeader commonHeader) {
		this.status = status;
		this.commonHeader = commonHeader;
	}

	public Status getStatus() {
		return status;
	}

	public CommonHeader getCommonHeader() {
		return commonHeader;
	}

}
