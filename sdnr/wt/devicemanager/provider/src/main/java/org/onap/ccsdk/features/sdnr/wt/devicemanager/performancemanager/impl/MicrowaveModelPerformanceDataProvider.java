package org.onap.ccsdk.features.sdnr.wt.devicemanager.performancemanager.impl;

import org.onap.ccsdk.features.sdnr.wt.devicemanager.base.netconf.container.AllPm;


/**
 * Identify the NE as provider for performance data according to microwave model.
 *
 * @author herbert
 */

public interface MicrowaveModelPerformanceDataProvider {

    public void resetPMIterator();

    public boolean hasNext();

    public void next();

    public AllPm getHistoricalPM();

    public String pmStatusToString();

}
