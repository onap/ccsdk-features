package org.onap.ccsdk.features.sdnr.wt.devicemanager.onf14.util;

import java.time.Instant;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Absolute;

public class NetconfDeviceNotification implements DOMNotification, DOMEvent {
    private final ContainerNode content;
    private final Absolute schemaPath;
    private final Instant eventTime;

    public NetconfDeviceNotification(final ContainerNode content, final Instant eventTime) {
        this.content = content;
        this.eventTime = eventTime;
        this.schemaPath = Absolute.of(content.name().getNodeType());
    }

    NetconfDeviceNotification(final ContainerNode content, final Absolute schemaPath, final Instant eventTime) {
        this.content = content;
        this.eventTime = eventTime;
        this.schemaPath = schemaPath;
    }

    @Override
    public Absolute getType() {
        return schemaPath;
    }

    @Override
    public ContainerNode getBody() {
        return content;
    }

    @Override
    public Instant getEventInstant() {
        return eventTime;
    }
}
