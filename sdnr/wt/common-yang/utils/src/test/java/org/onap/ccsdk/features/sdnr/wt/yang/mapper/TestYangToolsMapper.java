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
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize.IdentifierDeserializer;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressLocation;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressLocationBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.AddressType;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.ItemCodeIdentity;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemList;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemListBuilder;
import org.opendaylight.yang.gen.v1.urn.test.yang.utils.norev.address.location.entity.ItemListKey;

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
        assertEquals(ItemCodeIdentity.VALUE, al.getTestId());
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
        ItemList il = new ItemListBuilder().setItemKey(ItemCodeIdentity.VALUE).build();
        items.put(new ItemListKey(ItemCodeIdentity.VALUE), il);

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
}
