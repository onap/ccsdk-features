package org.onap.ccsdk.features.lib.rlock;

public class ResourceLockedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private String lockName, lockHolder, lockRequester;

	public ResourceLockedException(String lockName, String lockHolder, String lockRequester) {
		this.lockName = lockName;
		this.lockHolder = lockHolder;
		this.lockRequester = lockRequester;
	}

	@Override
	public String getMessage() {
		return "Failed to lock [" + lockName + "] for [" + lockRequester + "]. Currently locked by [" + lockHolder +
		        "].";
	}
}
