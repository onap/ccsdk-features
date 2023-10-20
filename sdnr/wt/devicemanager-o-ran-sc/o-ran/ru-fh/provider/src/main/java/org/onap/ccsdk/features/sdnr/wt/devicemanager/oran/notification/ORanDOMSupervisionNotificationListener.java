package org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.model.DataProvider;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.config.ORanDMConfig;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.dataprovider.ORanDOMToInternalDataModel;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDMDOMUtility;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.util.ORanDeviceManagerQNames;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.oran.vesmapper.ORanDOMSupervisionNotifToVESMapper;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.VESCollectorService;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESCommonEventHeaderPOJO;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESMessage;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.types.VESStndDefinedFieldsPOJO;
import org.onap.ccsdk.features.sdnr.wt.netconfnodestateservice.NetconfDomAccessor;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.mdsal.dom.api.DOMNotificationListener;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ORanDOMSupervisionNotificationListener implements DOMNotificationListener, ORanNotificationReceivedService {
    private static final Logger LOG = LoggerFactory.getLogger(ORanDOMSupervisionNotificationListener.class);

    private @NonNull NetconfDomAccessor netconfDomAccessor;
    private @NonNull DataProvider databaseService;
    private @NonNull VESCollectorService vesCollectorService;
    private List<ORanNotificationObserver> notificationObserverList;
    private Integer counter; //Local counter is assigned to Notifications

    private @NonNull ORanDOMSupervisionNotifToVESMapper mapper;

    public ORanDOMSupervisionNotificationListener(@NonNull NetconfDomAccessor netconfDomAccessor,
            @NonNull VESCollectorService vesCollectorService, @NonNull DataProvider databaseService,
            ORanDMConfig oranSupervisionConfig) {
        this.netconfDomAccessor = netconfDomAccessor;
        this.databaseService = databaseService;
        this.vesCollectorService = vesCollectorService;
        this.mapper = new ORanDOMSupervisionNotifToVESMapper(netconfDomAccessor.getNodeId(), vesCollectorService, "o-ran-supervision");
        notificationObserverList = new ArrayList<>();
        this.counter = 0;
    }

    public void setComponentList(Collection<MapEntryNode> componentList) {
        for (MapEntryNode component : ORanDOMToInternalDataModel.getRootComponents(componentList)) {
            mapper.setMfgName(
                    ORanDMDOMUtility.getLeafValue(component, ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MFG_NAME));
            mapper.setUuid(ORanDMDOMUtility.getLeafValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID) != null
                            ? ORanDMDOMUtility.getLeafValue(component,
                                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_UUID)
                            : netconfDomAccessor.getNodeId().getValue());
            mapper.setModelName(ORanDMDOMUtility.getLeafValue(component,
                    ORanDeviceManagerQNames.IETF_HW_COMPONENT_LIST_MODEL_NAME));
        }
    }

    @Override
    public void onNotification(@NonNull DOMNotification notification) {
        LOG.trace("Notification Type = {}", notification.getType().toString());
        notifyObservers();
        Instant eventTimeInstant = ORanDMDOMUtility.getNotificationInstant(notification);
        try {
            if (vesCollectorService.getConfig().isVESCollectorEnabled()) {
                VESCommonEventHeaderPOJO header = mapper.mapCommonEventHeader(notification, eventTimeInstant, counter);
                VESStndDefinedFieldsPOJO body = mapper.mapStndDefinedFields(eventTimeInstant);
                VESMessage vesMsg = vesCollectorService.generateVESEvent(header, body);
                vesCollectorService.publishVESMessage(vesMsg);
                LOG.debug("VES Message is  {}", vesMsg.getMessage());
            }
        } catch (JsonProcessingException | DateTimeParseException e) {
            LOG.debug("Cannot convert event into VES message {}", notification, e);
        }
    }

    private void notifyObservers() {
        Iterator<ORanNotificationObserver> it = notificationObserverList.iterator();
        while (it.hasNext()) {
            ORanNotificationObserver o = it.next();
            new Thread() {
                @Override
                public void run() {
                    o.observer();
                }
            }.start();
        }
    }

    @Override
    public void registerForNotificationReceivedEvent(ORanNotificationObserver o) {
        notificationObserverList.add(o);
    }

    @Override
    public void deregisterNotificationReceivedEvent(ORanNotificationObserver o) {
        notificationObserverList.remove(o);
    }

}
