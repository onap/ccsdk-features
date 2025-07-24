/*
 * ============LICENSE_START=======================================================
 * ONAP : ccsdk features
 * ================================================================================
 * Copyright (C) 2020 highstreet technologies GmbH Intellectual Property.
 * All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 *
 */
package org.onap.ccsdk.features.sdnr.wt.yang.mapper;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.IdentifierDeserializer;
import org.opendaylight.mdsal.dom.api.DOMEvent;
import org.opendaylight.mdsal.dom.api.DOMNotification;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressLocation;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressType;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.ItemCode;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemList;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemListBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemListKey;
import org.opendaylight.yangtools.binding.EventInstantAware;
import org.opendaylight.yangtools.binding.Notification;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;

public class TestYangToolsMapper {

    private static final YangToolsMapper MAPPER = new YangToolsMapper();

    @Before
    public void init() {
        MAPPER.addKeyDeserializer(ItemListKey.class, new IdentifierDeserializer());
    }

    @Test
    public void testYangMapperDeser2() {
        AddressLocation al = null;

        try {
            al = MAPPER.readValue(
                    "{\n"
                            + "    \"address-type\": \"OFFICE\",\n"
                            + "    \"delivery-date-time\": \"2022-03-15T11:12:13.890Z\",\n"
                            + "    \"delivery-url\": \"delivery.uri\",\n"
                            + "    \"test-id\": \"org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.ItemCodeIdentity\""
                            + "}",
                    AddressLocation.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals(AddressType.OFFICE, al.getAddressType());
        assertEquals("2022-03-15T11:12:13.890Z", al.getDeliveryDateTime().getValue());
        //TODO assertEquals(ItemCode.VALUE, al.getItemList().ke);
        System.out.println("Delivery Date = " + al.getDeliveryDateTime().getValue());
        System.out.println(al.getItemList());
        System.out.println(al.getDeliveryUrl().getValue());
    }

    @Test
    public void testYangMapperDeser() {
        AddressLocation al = null;

        try {
            al = MAPPER.readValue(
                    "{\n"
                            + "    \"address-type\": \"OFFICE\",\n"
                            + "    \"delivery-date-time\": \"2022-03-15T11:12:13.890Z\",\n"
                            + "    \"delivery-url\": \"delivery.uri\",\n"
                            + "    \"item-list\": [\n"
                            + "        {\n"
                            + "            \"item-key\": \"org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.ItemCodeIdentity\"\n"
                            + "        }\n"
                            + "    ]\n"
                            + "}",
                    AddressLocation.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals(AddressType.OFFICE, al.getAddressType());
        assertEquals("2022-03-15T11:12:13.890Z", al.getDeliveryDateTime().getValue());
        System.out.println("Delivery Date = " + al.getDeliveryDateTime().getValue());
        System.out.println(al.getItemList());
        System.out.println(al.getDeliveryUrl().getValue());
    }

    @Test
    public void testYangMapperSer() {
        Map<ItemListKey, ItemList> items = new HashMap<ItemListKey, ItemList>();
        ItemList il = new ItemListBuilder().setItemKey(ItemCode.VALUE).build();
        items.put(new ItemListKey(ItemCode.VALUE), il);

        Uri uri = new Uri("delivery.uri");

        AddressLocation al = new AddressLocationBuilder().setId("99").setAddressType(AddressType.HOME)
                .setDeliveryDateTime(new DateAndTime("2022-03-15T11:12:13.890Z")).setItemList(items)
                .setDeliveryUrl(uri).build();
        String str = null;

        try {
            str = MAPPER.writeValueAsString(al);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        assertEquals("HOME", new JSONObject(str).getString("address-type"));
        assertEquals("2022-03-15T11:12:13.890Z", new JSONObject(str).getString("delivery-date-time"));
        System.out.println(new JSONObject(str).getJSONArray("item-list"));
        System.out.println(str);
    }

    @Test
    public void testNotificationTime() {

        int LIMIT = 20;
        var now = Instant.now();
        var nowPlus2 = now.plusSeconds(20);
        var domNotif = new TestDomEvent(now);
        var notif = new TestNotification(now);
        var domNotif2 = new TestDomEvent2();
        var notif2 = new TestNotification2();
        assertEquals(now.toString().substring(0,LIMIT), YangToolsMapperHelper.getTime(notif, null).getValue().substring(0,LIMIT));
        assertEquals(now.toString().substring(0,LIMIT),YangToolsMapperHelper.getTime(domNotif, null).getValue().substring(0,LIMIT));
        assertEquals(nowPlus2.toString().substring(0,LIMIT), YangToolsMapperHelper.getTime(notif2, nowPlus2).getValue().substring(0,LIMIT));
        assertEquals(nowPlus2.toString().substring(0,LIMIT),YangToolsMapperHelper.getTime(domNotif2, nowPlus2).getValue().substring(0,LIMIT));
    }

    @Test
    public void testCamelCase(){
        assertEquals("camelCase", YangToolsMapperHelper.toCamelCase("camel-case"));
        assertEquals("camel", YangToolsMapperHelper.toCamelCase("camel"));
        assertEquals("camelCaseConverter", YangToolsMapperHelper.toCamelCase("camel-case-converter"));
        assertEquals("CamelCase", YangToolsMapperHelper.toCamelCaseClassName("camel-case"));
    }

    static class TestDomEvent implements DOMNotification, DOMEvent {

        private final Instant instant;

        TestDomEvent(Instant instant) {
            this.instant = instant;
        }

        @Override
        public Instant getEventInstant() {
            return this.instant;
        }

        @Override
        public SchemaNodeIdentifier.Absolute getType() {
            return null;
        }

        @Override
        public @NonNull ContainerNode getBody() {
            return null;
        }
    }
    static class TestDomEvent2 implements DOMNotification{

        @Override
        public SchemaNodeIdentifier.Absolute getType() {
            return null;
        }

        @Override
        public ContainerNode getBody() {
            return null;
        }
    }
    static class TestNotification implements Notification, EventInstantAware {
        private final Instant instant;

        TestNotification(Instant instant) {
            this.instant = instant;
        }

        @Override
        public @NonNull Instant eventInstant() {
            return this.instant;
        }

        @Override
        public Class implementedInterface() {
            return null;
        }
    }
    static class TestNotification2 implements Notification {

        @Override
        public Class implementedInterface() {
            return null;
        }
    }
}
