package org.onap.ccsdk.features.lib.rlock;

import java.util.Date;

public class ResourceLock {

    public long id;
    public String resourceName;
    public String lockHolder;
    public int lockCount;
    public Date lockTime;
    public Date expirationTime;
}
