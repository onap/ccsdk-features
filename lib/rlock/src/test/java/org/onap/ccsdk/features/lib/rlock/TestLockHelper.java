package org.onap.ccsdk.features.lib.rlock;

import org.junit.Test;
import org.onap.ccsdk.features.lib.rlock.testutils.DbUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestLockHelper {

    private static final Logger log = LoggerFactory.getLogger(TestLockHelper.class);

    @Test
    public void test1() throws Exception {
        LockThread t1 = new LockThread("req1");
        LockThread t2 = new LockThread("req2");
        LockThread t3 = new LockThread("req3");

        t1.start();
        t2.start();
        t3.start();

        t1.join();
        t2.join();
        t3.join();
    }

    private class LockThread extends Thread {

        private String requester;

        public LockThread(String requester) {
            this.requester = requester;
        }

        @Override
        public void run() {
            LockHelperImpl lockHelper = new LockHelperImpl();
            lockHelper.setDataSource(DbUtil.getDataSource());

            lockHelper.lock("resource1", requester, 20);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Thread interrupted: " + e.getMessage(), e);
            }

            lockHelper.unlock("resource1", false);
        }
    }
}
