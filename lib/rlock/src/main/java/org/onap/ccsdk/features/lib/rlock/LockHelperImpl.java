package org.onap.ccsdk.features.lib.rlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of the locking service, providing <i>distributed</i> locking functionality. It is
 * done using a table in SQL Database as a single synchronization point. Hence, for this
 * implementation, it is required that all participating threads in all participating applications
 * access the same database instance (or a distributed database that looks like one database
 * instance).
 * </p>
 * <p>
 * The following table is required in the database:
 * </p>
 *
 * <pre style="color:darkblue;">
 * CREATE TABLE IF NOT EXISTS `resource_lock` (
 *   `resource_lock_id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
 *   `resource_name` varchar(256),
 *   `lock_holder` varchar(100) NOT NULL,
 *   `lock_count` smallint(6) NOT NULL,
 *   `lock_time` datetime NOT NULL,
 *   `expiration_time` datetime NOT NULL,
 *   PRIMARY KEY (`resource_lock_id`),
 *   UNIQUE KEY `IX1_RESOURCE_LOCK` (`resource_name`)
 * );
 * </pre>
 * <p>
 * The implementation tries to insert records in the table for all the requested resources. If there
 * are already records for any of the resources, it fails and then makes several more tries before
 * giving up and throwing {@link ResourceLockedException}.
 * </p>
 * <p>
 * The class has 2 configurable parameters:
 * <ul>
 * <li><tt><b>retryCount</b></tt>: the numbers of retries, when locking a resource, default 20</li>
 * <li><tt><b>lockWait</b></tt>: the time between each retry (in seconds), default 5 seconds</li>
 * </ul>
 * The total time before locking fails would be <tt>retryCount * lockWait</tt> seconds.
 * </p>
 *
 * @see LockHelper
 * @see SynchronizedFunction
 * @see ResourceLockedException
 */
public class LockHelperImpl implements LockHelper {

    private static final Logger log = LoggerFactory.getLogger(LockHelperImpl.class);

    private int retryCount = 20;
    private int lockWait = 5; // Seconds

    private DataSource dataSource;

    @Override
    public void lock(String resourceName, String lockRequester, int lockTimeout /* Seconds */) {
        lock(Collections.singleton(resourceName), lockRequester, lockTimeout);
    }

    @Override
    public void unlock(String resourceName, boolean force) {
        unlock(Collections.singleton(resourceName), force);
    }

    @Override
    public void lock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */) {
        for (int i = 0; true; i++) {
            try {
                tryLock(resourceNameList, lockRequester, lockTimeout);
                log.info("Resources locked: " + resourceNameList);
                return;
            } catch (ResourceLockedException e) {
                if (i > retryCount) {
                    throw e;
                }
                try {
                    Thread.sleep(lockWait * 1000L);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @Override
    public void unlock(Collection<String> lockNames, boolean force) {
        if (lockNames == null || lockNames.size() == 0) {
            return;
        }

        try (ResourceLockDao resourceLockDao = new ResourceLockDao(dataSource)) {
            try {
                for (String name : lockNames) {
                    ResourceLock l = resourceLockDao.getByResourceName(name);
                    if (l != null) {
                        if (force || l.lockCount == 1) {
                            resourceLockDao.delete(l.id);
                        } else {
                            resourceLockDao.decrementLockCount(l.id);
                        }
                    }
                }
                resourceLockDao.commit();
                log.info("Resources unlocked: " + lockNames);
            } catch (Exception e) {
                resourceLockDao.rollback();
            }
        }
    }

    public void tryLock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */) {
        if (resourceNameList == null || resourceNameList.isEmpty()) {
            return;
        }

        lockRequester = generateLockRequester(lockRequester, 100);

        // First check if all requested records are available to lock

        Date now = new Date();

        try (ResourceLockDao resourceLockDao = new ResourceLockDao(dataSource)) {
            try {
                List<ResourceLock> dbLockList = new ArrayList<>();
                List<String> insertLockNameList = new ArrayList<>();
                for (String name : resourceNameList) {
                    ResourceLock l = resourceLockDao.getByResourceName(name);

                    boolean canLock = l == null || now.getTime() > l.expirationTime.getTime()
                            || lockRequester != null && lockRequester.equals(l.lockHolder) || l.lockCount <= 0;
                    if (!canLock) {
                        throw new ResourceLockedException(l.resourceName, l.lockHolder, lockRequester);
                    }

                    if (l != null) {
                        if (now.getTime() > l.expirationTime.getTime() || l.lockCount <= 0) {
                            l.lockCount = 0;
                        }
                        dbLockList.add(l);
                    } else {
                        insertLockNameList.add(name);
                    }
                }

                // Update the lock info in DB
                for (ResourceLock l : dbLockList) {
                    resourceLockDao.update(l.id, lockRequester, now, new Date(now.getTime() + lockTimeout * 1000),
                            l.lockCount + 1);
                }

                // Insert records for those that are not yet there
                for (String lockName : insertLockNameList) {
                    ResourceLock l = new ResourceLock();
                    l.resourceName = lockName;
                    l.lockHolder = lockRequester;
                    l.lockTime = now;
                    l.expirationTime = new Date(now.getTime() + lockTimeout * 1000);
                    l.lockCount = 1;

                    try {
                        resourceLockDao.add(l);
                    } catch (Exception e) {
                        throw new ResourceLockedException(l.resourceName, "unknown", lockRequester);
                    }
                }

                resourceLockDao.commit();

            } catch (Exception e) {
                resourceLockDao.rollback();
                throw e;
            }
        }
    }

    private static String generateLockRequester(String name, int maxLength) {
        if (name == null) {
            name = "";
        }
        int l1 = name.length();
        String tname = Thread.currentThread().getName();
        int l2 = tname.length();
        if (l1 + l2 + 1 > maxLength) {
            int maxl1 = maxLength / 2;
            if (l1 > maxl1) {
                name = name.substring(0, maxl1);
                l1 = maxl1;
            }
            int maxl2 = maxLength - l1 - 1;
            if (l2 > maxl2) {
                tname = tname.substring(0, 6) + "..." + tname.substring(l2 - maxl2 + 9);
            }
        }
        return tname + '-' + name;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setLockWait(int lockWait) {
        this.lockWait = lockWait;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
