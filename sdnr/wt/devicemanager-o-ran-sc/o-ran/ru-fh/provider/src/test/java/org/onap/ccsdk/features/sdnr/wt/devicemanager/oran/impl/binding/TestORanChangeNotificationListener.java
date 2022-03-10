/*
 * ============LICENSE_START========================================================================
 * ONAP : ccsdk feature sdnr wt
 * =================================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property. All rights reserved.
 * =================================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 * ============LICENSE_END==========================================================================
 */

package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.impl.binding.ORanChangeNotificationListener;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.DeviceManagerServiceProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorCfgService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfBindingAccessor;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.netconf.base._1._0.rev110601.EditOperationType;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.NetconfConfigChange;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.Edit;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.netconf.notifications.rev120206.netconf.config.change.EditBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev201110.EventlogEntity;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.PathArgument;

@RunWith(MockitoJUnitRunner.class)
public class TestORanChangeNotificationListener {

    private static final String NODEID = "node1";

    @Mock
    DeviceManagerServiceProvider serviceProvider;
    @Mock
    NetconfBindingAccessor netconfAccessor;
    @Mock
    DataProvider databaseService;
    @Mock
    VESCollectorService vesCollectorService;
    @Mock
    VESCollectorCfgService vesCfgService;
    @Mock
    NotificationProxyParser notifProxyParser;
    @Mock
    static NetconfConfigChange change;

    @Test
    public void test() {

        when(vesCollectorService.getConfig()).thenReturn(vesCfgService);
        when(netconfAccessor.getNodeId()).thenReturn(new NodeId(NODEID));
        when(vesCfgService.isVESCollectorEnabled()).thenReturn(true);
        when(serviceProvider.getDataProvider()).thenReturn(databaseService);
        when(serviceProvider.getVESCollectorService()).thenReturn(vesCollectorService);
        when(vesCollectorService.getNotificationProxyParser()).thenReturn(notifProxyParser);

        ORanChangeNotificationListener notifListener =
                new ORanChangeNotificationListener(netconfAccessor, serviceProvider);

        Iterable<? extends PathArgument> pathArguments = Arrays.asList(new PathArgument() {

            @Override
            public int compareTo(PathArgument arg0) {
                return 0;
            }

            @Override
            public Class<? extends DataObject> getType() {
                return DataObject.class;
            }
        });
        InstanceIdentifier<?> target = InstanceIdentifier.create(pathArguments);
        NetconfConfigChange confChangeNotification = createNotification(EditOperationType.Create, target);
        when(notifProxyParser.getTime(confChangeNotification)).thenReturn(Instant.now());
        notifListener.onNetconfConfigChange(confChangeNotification);
        verify(databaseService).writeEventLog(any(EventlogEntity.class));
    }

    /**
     * @param type
     * @return
     */
    private static NetconfConfigChange createNotification(EditOperationType type, InstanceIdentifier<?> target) {
        @SuppressWarnings("null")
        final @NonNull List<Edit> edits = Arrays.asList(new EditBuilder().setOperation(type).setTarget(target).build());
        when(change.nonnullEdit()).thenReturn(edits);
        return change;
    }
}
