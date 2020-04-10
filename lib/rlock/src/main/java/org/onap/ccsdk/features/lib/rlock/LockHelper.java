package org.onap.ccsdk.features.lib.rlock;

import java.util.Collection;

public interface LockHelper {

	void lock(String resourceName, String lockRequester, int lockTimeout /* Seconds */);

	void unlock(String resourceName, boolean force);

	void lock(Collection<String> resourceNameList, String lockRequester, int lockTimeout /* Seconds */);

	void unlock(Collection<String> resourceNameList, boolean force);
}
