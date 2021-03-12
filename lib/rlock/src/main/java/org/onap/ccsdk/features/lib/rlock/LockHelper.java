package org.onap.ccsdk.features.lib.rlock;

import java.util.Collection;

/**
 * <p>
 * A cooperative locking service. This service can be used to synchronize access to shared
 * resources, so only one thread has access to it at any time. It is <i>cooperative</i> locking,
 * meaning the threads have to agree to explicitly lock the resource, whenever they need to access
 * it and then explicitly unlock it, when they are done with the resource. While a resource is
 * locked by a thread, no other thread can lock it, until it is unlocked.
 * </p>
 * <p>
 * The term <tt><b>resource</b></tt> represents anything that needs a synchronized access, for
 * example, message queue or connection pool or limited bandwidth capacity.
 * </p>
 * <p>
 * The implementation here provides a <i>distributed</i> locking functionality, which means threads
 * can reside in the same application instance or different application instances, on same or
 * different machines (see {@link LockHelperImpl}).
 * </p>
 * <p>
 * Example: Bandwidth check:
 * </p>
 * <pre style="color:darkblue;">
 * boolean success = false;
 * String resourceName = "Port-123-Bandwidth";
 * int neededBandwidth = 100; // 100 Mbps
 * int bandwidthLimit = 1000;
 * int lockRequester = "this-thread-id-" + (int) (Math.random() * 1000000);
 * int lockTimeout = 30; // 30 sec - the longest time, we expect to complete the check
 *
 * try {
 *     lockHelper.lock(resourceName, lockRequester, lockTimeout);
 *
 *     int usedBandwidth = portBandwidthDao.readUsedBandwidth("Port-123");
 *     if (usedBandwidth + neededBandwidth <= bandwidthLimit) {
 *         portBandwidthDao.updateUsedBandwidth("Port-123", usedBandwidth + neededBandwidth);
 *         success = true;
 *     }
 * } finally {
 *     lockHelper.unlock(resourceName, true);
 * }
 * </pre>
 *
 * @see SynchronizedFunction
 * @see LockHelperImpl
 * @see ResourceLockedException
 */
public interface LockHelper {

    /**
     * <p>
     * Locks the specified resource. Lock requester identifies the thread that is requesting the lock.
     * If the resource is already locked by another lock requester (another thread), then the thread is
     * blocked until the resource is available. If the resource is available or locked by the same
     * thread, the lock succeeds.<br/>
     * Usually lock requester can be generated using the thread name and some random number.
     * </p>
     * <p>
     * Lock timeout specifies how long (in seconds) to keep the lock in case the thread misses to unlock
     * the resource. The lock will expire after this time in case the resource is never unlocked. This
     * time should be set to the maximum time expected for the processing of the resource, so the lock
     * does not expire until the thread is done with the resource. The lock timeout is supposed to avoid
     * permanently locking a resource in case of application crash in the middle of processing.
     * </p>
     *
     * @param resourceName Identifies the resource to be locked
     * @param lockRequester Identifies the thread requesting the lock
     * @param lockTimeout The expiration timeout of the lock (in seconds)
     * @throws ResourceLockedException if the resource cannot be locked
     */
    void lock(String resourceName, String lockRequester, int lockTimeout /* Seconds */);

    /**
     * <p>
     * Unlocks the specified resource. This method should always succeed including in the case, when the
     * resource is already unlocked.
     * </p>
     * <p>
     * Force parameter can be used in case the same thread might lock the same resource multiple times.
     * In case resource is locked multiple times, normally, in case force parameter is false, the thread
     * will need to unlock the resource the same number of times for the resource to become available
     * again. If the force parameter is true, then the resource will be unlocked immediately, no matter
     * how many times it has been locked.
     * </p>
     *
     * @param resourceName Identifies the resource to be unlocked
     * @param force If true, forces resource to be unlocked immediately, even if it has been locked more
     *        than once
     */
    void unlock(String resourceName, boolean force);

    /**
     * <p>
     * Locks multiple resources at once. It ensures that either all resources are successfully locked or
     * none is.
     * </p>
     *
     * @param resourceNameList The set of resources to lock
     * @param lockRequester Identifies the thread requesting the lock
     * @param lockTimeout The expiration timeout of the lock (in seconds)
     * @throws ResourceLockedException if any of the resources cannot be locked
     *
     * @see LockHelper#lock(String, String, int)
     */
    void lock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */);

    /**
     * <p>
     * Unlocks the specified set of resources. This method should always succeed including in the case,
     * when any of the resources are already unlocked.
     * </p>
     *
     * @param resourceNameList The set of resources to unlock
     * @param force If true, forces all resources to be unlocked immediately, even if any of them have
     *        been locked more than once
     *
     * @see LockHelper#unlock(String, boolean)
     */
    void unlock(Collection<String> resourceNameList, boolean force);
}
