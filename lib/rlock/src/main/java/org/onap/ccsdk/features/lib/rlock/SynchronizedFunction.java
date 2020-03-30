package org.onap.ccsdk.features.lib.rlock;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class SynchronizedFunction {

	private Set<String> synchset;
	private String lockRequester;
	private int lockTimeout; // Seconds
	private LockHelper lockHelper;

	protected SynchronizedFunction(LockHelper lockHelper, Collection<String> synchset, int lockTimeout) {
		this.lockHelper = lockHelper;
		this.synchset = new HashSet<String>(synchset);
		this.lockRequester = generateLockRequester();
		this.lockTimeout = lockTimeout;
	}

	protected abstract void _exec();

	public void exec() {
		lockHelper.lock(synchset, lockRequester, lockTimeout);
		try {
			_exec();
		} finally {
			lockHelper.unlock(synchset, true);
		}
	}

	private static String generateLockRequester() {
		return "SynchronizedFunction-" + (int) (Math.random() * 1000000);
	}
}
