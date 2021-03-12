package org.onap.ccsdk.features.lib.rlock;

/**
 * <p>
 * This exception is thrown by {@link LockHelper#lock(String, String, int) LockHelper.lock} methods,
 * if a requested resource cannot be locked. This might happen, because another threads keeps a
 * resource locked for a long time or there is a stale lock that hasn't expired yet.
 * </p>
 * <p>
 * The exception message will contain the locked resource and what lock requester (thread) holds the
 * resource locked.
 * </p>
 *
 * @see LockHelperImpl
 */
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
        return "Failed to lock [" + lockName + "] for [" + lockRequester + "]. Currently locked by [" + lockHolder
                + "].";
    }
}
