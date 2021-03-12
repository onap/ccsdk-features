package org.onap.ccsdk.features.lib.rlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * <p>
 * A simple abstract base class, providing functionality similar to the <tt>synchronized</tt> block
 * in Java. Derived class provides the set of resources that need synchronized access and the
 * processing method that is executed while the set of resources is locked (override <tt>_exec</tt>
 * method). This class uses the {@link LockHelper} service to lock the resources, execute the
 * processing method and then unlock the resources.
 * </p>
 * <p>
 * Example:
 * </p>
 *
 * <pre style="color:darkblue;">
 * public class BandwidthCheckFunction extends SynchronizedFunction {
 *
 *     private PortBandwidthDao portBandwidthDao;
 *     private int neededBandwidth;
 *     private int bandwidthLimit;
 *
 *     private boolean successful; // Output
 *
 *     public BandwidthCheckFunction(LockHelper lockHelper, PortBandwidthDao portBandwidthDao, String portId,
 *             int neededBandwidth, int bandwidthLimit) {
 *         super(lockHelper, Collections.singleton(portId + "-Bandwidth"), 60); // 60 sec lockTimeout
 *         this.portBandwidthDao = portBandwidthDao;
 *         this.neededBandwidth = neededBandwidth;
 *         this.bandwidthLimit = bandwidthLimit;
 *     }
 *
 *     {@literal @}Override
 *     protected void _exec() {
 *         int usedBandwidth = portBandwidthDao.readUsedBandwidth("Port-123");
 *         if (usedBandwidth + neededBandwidth <= bandwidthLimit) {
 *             portBandwidthDao.updateUsedBandwidth("Port-123", usedBandwidth + neededBandwidth);
 *             successful = true;
 *         } else {
 *             successful = false;
 *         }
 *     }
 *
 *     public boolean isSuccessful() {
 *         return successful;
 *     }
 * }
 *
 * ..........
 *
 *     BandwidthCheckFunction func = new BandwidthCheckFunction(lockHelper, portBandwidthDao, "Port-123", 100, 1000);
 *     func.exec();
 *     boolean success = func.isSuccessful();
 * ..........
 * </pre>
 *
 * @see LockHelper
 */
public abstract class SynchronizedFunction {

    private Set<String> syncSet;
    private String lockRequester;
    private int lockTimeout; // Seconds
    private LockHelper lockHelper;

    /**
     * @param lockHelper {@link LockHelper} service implementation
     * @param syncSet the set of resources to be locked during processing
     * @param lockTimeout the lock expiration timeout (see {@link LockHelper#lock(String, String, int)
     *        LockHelper.lock})
     */
    protected SynchronizedFunction(LockHelper lockHelper, Collection<String> syncSet, int lockTimeout) {
        this.lockHelper = lockHelper;
        this.syncSet = new HashSet<>(syncSet);
        lockRequester = generateLockRequester();
        this.lockTimeout = lockTimeout;
    }

    /**
     * Implement this method with the required processing. This method is executed while the resources
     * are locked (<tt>syncSet</tt> provided in the constructor).
     */
    protected abstract void _exec();

    /**
     * Call this method to execute the provided processing in the derived class (the implemented
     * <tt>_exec</tt> method).
     */
    public void exec() {
        lockHelper.lock(syncSet, lockRequester, lockTimeout);
        try {
            _exec();
        } finally {
            lockHelper.unlock(syncSet, true);
        }
    }

    private static String generateLockRequester() {
        return "SynchronizedFunction-" + (int) (Math.random() * 1000000);
    }
}
