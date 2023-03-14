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
package org.onap.ccsdk.features.sdnr.wt.devicemanager.impl.util;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.onap.ccsdk.features.sdnr.wt.devicemanager.service.NotificationProxyParser;
import org.opendaylight.yangtools.concepts.Identifier;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.EventInstantAware;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Notification;
import org.opendaylight.yangtools.yang.common.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationProxyParserImpl implements NotificationProxyParser {


    /*
     *  Converter of TR069 notifications to VES key, value hashmap.
     *  Notifications are received as cascade if proxy object.
     *  References: https://stackoverflow.com/questions/19633534/what-is-com-sun-proxy-proxy
     *
     *  Attributes are provided by getters starting with "get", "is", "key".
     *  Proxy received: "com.sun.proxy.$ProxyNN". NN is a number.
     *
     *  Example result:
     *
     * Expected output via VES in JSON
     *    {
     *      "event": {
     *         "commonEventHeader": {
     *            "domain": "notification",
     *            "eventId": "ABCD",
     *            "eventName": "Notification_LTE_Enterprise_C-RANSC_Cntrl-ACME",
     *            "eventType": "TR069_RAN_notification",
     *            "sequence": 0,
     *            "priority": "High",
     *            "reportingEntityId": "0005B942CDB4",
     *            "reportingEntityName": "ABCD",
     *            "sourceId": "0005B942CDB4",
     *            "sourceName": "ABCD",
     *            "startEpochMicrosec": 1569579510211,
     *            "lastEpochMicrosec": 1569579510211,
     *            "nfcNamingCode": "",
     *            "nfNamingCode": "",
     *            "nfVendorName": "",
     *            "timeZoneOffset": "+00:00",
     *            "version": "4.0.1",
     *            "vesEventListenerVersion": "7.0.1"
     *         },
     *         "notificationFields": {
     *         "arrayOfNamedHashMap": [
     *          {
     *           "name": "VALUECHANGE",
     *           "hashMap": {
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/serial-number": "0005B94238A0",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/software-version": "4.3.00.244",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/hardware-version": "1",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/provisioning-code": "",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/manufacturer": "ACME",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/product-class": "LTE_Enterprise_C-RANSC_Cntrl",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/device-info/manufacturer-oui": "0005B9",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[1]/index": "1",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[1]/fap-control/lte/rf-tx-status": "false",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[1]/fap-control/lte/op-state": "true",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[2]/index": "2",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[2]/fap-control/lte/rf-tx-status": "false",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[2]/fap-control/lte/op-state": "true",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/services/fap-service[2]/cell-config/lte/ran/rf/phy-cell-id": "201",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/management-server/connection-request-url": "http://10.220.68.2/acscall",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/management-server/parameter-key": "none",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/management-server/connection-request-password": "password",
     *               "/notification/VALUECHANGE[@xmlns=\"urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notification\"]/device/management-server/connection-request-username": "0005B9-LTE_Enterprise_C-RANSC_Cntrl-0005B94238A0"
     *           },
     *          }
     *         ],
     *         "changeContact": "",
     *         "changeIdentifier": "SessionID",
     *         "changeType": "ValueChange",
     *         "newState": "",
     *         "oldState": "",
     *         "stateInterface": "",
     *         "notificationFieldsVersion": "2.0",
     *         }
     *      }
     *    }
     *
     */
        private static final Logger log = LoggerFactory.getLogger(NotificationProxyParserImpl.class);
        private Notification notification;

        @Override
        public HashMap<String, String> parseNotificationProxy(Notification notification)
        /*throws ORanNotificationMapperException*/ {

            try {
                return extractFields(notification);
            } catch (IllegalArgumentException | SecurityException | NoSuchFieldException | IllegalAccessException
                    | InvocationTargetException e) {
                //throw new ORanNotificationMapperException("Mapping/JSON Creation problem", e);
                log.info("Exception in performMapping method {}",e);
                return null;
            }

        }

        private HashMap<String, String> extractFields(Object o) throws IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchFieldException, SecurityException {
            String start = "/notification/" + getExtendedInterfaceName(Notification.class, o) + getXmlNameSpace(o);
            return recurseExtractData(o, start, 0, new HashMap<String, String>());
        }

        private HashMap<String, String> recurseExtractData(Object o, String namePath, int level,
                HashMap<String, String> result)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            log.debug("In recurseExtractData - {} {} {}", namePath, level, log.isTraceEnabled() ? result : result.size());
            if (level > 20) {
                log.warn("Level to deep protection ended the recusive loop.");
            } else {
                if (o != null) {
                    Class<?> classz = o.getClass();
                    //notification/VALUECHANGE$$$eventInstantAware[@xmlns=urn:org:onap:ccsdk:features:sdnr:northbound:onecell-notificationon] 0 {}
                    //org.opendaylight.yang.gen.v1.urn.org.onap.ccsdk.features.sdnr.northbound.onecell.notification.rev200622.VALUECHANGE$$$eventInstantAware
                    //if (Proxy.isProxyClass(classz)) {
                    handleInterface(classz, o, namePath, level, result);
                    //}
                } else {
                    log.warn("Null not expected here.");
                }
            }
            return result;
        }

        private void handleInterface(Class<?> clazz, Object o, String namePath, int level, HashMap<String, String> result)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            log.debug("In extract Interface {}", clazz);
            if (clazz == null) {
                log.warn("Loop with null class");
                return;
            }
            if (level > 20) {
                log.warn("Level to deep protection ended the recusive loop.");
            }
            if (clazz.getName().contentEquals("org.opendaylight.mdsal.binding.dom.codec.impl.AugmentableCodecDataObject")) {
                log.trace("Leave AugmentableCodecDataObject");
                return;
            }

            Method[] methods = clazz.getDeclaredMethods();
            if (methods != null) {
                for (Method method : methods) {
                    String methodName = method.getName();
                    log.trace("Method {}", methodName);
                    if (methodName.startsWith("get")) {
                        if (!methodName.equals("getImplementedInterface")) {
                            handleGetterValue(method, methodName.substring(3), namePath, o, level, result);
                        }
                    } else if (methodName.startsWith("is")) {
                        handleGetterValue(method, methodName.substring(2), namePath, o, level, result);
                    } else if (methodName.equals("key")) {
                        handleGetterValue(method, methodName, namePath, o, level, result);
                    }
                }
            }
            Class<?> sc = clazz.getSuperclass();  //Sodium
            log.trace("Superclass is - {}", sc);
            if (sc != null && !(sc.getName().contains("java.lang.reflect.Proxy")) && !Proxy.isProxyClass(sc)) {
                handleInterface(sc, o, namePath, level + 1, result);
            }
        }

        private void handleGetterValue(Method method, String name, String namePath, Object o, int level,
                HashMap<String, String> result)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            log.debug("Begin: {}-{}-{}-{}", method.getName(), name, namePath, level);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            Object value = method.invoke(o);
            namePath += "/" + convertCamelToKebabCase(name);
            log.trace("Namepath {}", namePath);
            if (value != null) {
                Class<?> type = value.getClass();
                log.trace("Class {}", type.getSimpleName());
                if (List.class.isAssignableFrom(type)) {
                    int idx = 0;
                    String keyString;
                    for (Object listObject : (List<?>) value) {
                        if (listObject != null) {
                            if (Identifiable.class.isAssignableFrom(listObject.getClass())) {
                                keyString = getKeyString((Identifiable<?>) listObject);
                            } else {
                                keyString = String.valueOf(idx);
                            }
                            recurseExtractData(listObject, namePath + "[" + keyString + "]", level + 1, result);
                        } else {
                            log.warn("Null value received {} {} {}", namePath, idx, name);
                        }
                        idx++;
                    }
                } else if (DataObject.class.isAssignableFrom(type)) {
                    recurseExtractData(value, namePath, level + 1, result);
                } else if (Proxy.isProxyClass(type)) {
                    recurseExtractData(value, namePath, level + 1, result);
                } else if (Identifier.class.isAssignableFrom(type)) {
                    //don't put the key
                } else {
                    result.put(namePath, value.toString());
                }
            } else {
                log.trace("Null value");
            }
        }

        private String getExtendedInterfaceName(Class<?> assignableClazz, Object o) {
            Class<?> interfaces[] = o.getClass().getInterfaces();
            for (Class<?> oneInterface : interfaces) {
                log.trace("In getExtendedInterfaceName, oneInterface = {}", oneInterface.getClass().getName());
                if (assignableClazz.isAssignableFrom(oneInterface)) {
                    return oneInterface.getSimpleName().contains("eventInstantAware")?oneInterface.getSimpleName().substring(0, oneInterface.getSimpleName().indexOf("$")):oneInterface.getSimpleName();
                }
            }
            log.trace("In getExtendedInterfaceName, o.getClass().getName() = {}", o.getClass().getName());
            return o.getClass().getSimpleName().contains("eventInstantAware")?o.getClass().getSimpleName().substring(0, o.getClass().getSimpleName().indexOf("$")):o.getClass().getSimpleName();
        }

        private String getXmlNameSpace(Object o)
                throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
            Field f = o.getClass().getField("QNAME");
            Object couldQName = f.get(o);
            if (couldQName instanceof QName) {
                QName qname = (QName) couldQName;
                return "[@xmlns=" + qname.getNamespace().toString() + "]";
            }
            return "";
        }

        /*private String convertCamelToKebabCase(String camel) {
            KebabCaseStrategy kbCase = new KebabCaseStrategy();
            return kbCase.translate(camel);
            //return camel.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
        } */

        /**
         * @param input string in Camel Case
         * @return String in Kebab case
         * Inspiration from KebabCaseStrategy class of com.fasterxml.jackson.databind with an additional condition to handle numbers as well
         * Using QNAME would have been a more fool proof solution, however it can lead to performance problems due to usage of Java reflection
         */
        private String convertCamelToKebabCase(String input)
        {
            if (input == null) return input; // garbage in, garbage out
            int length = input.length();
            if (length == 0) {
                return input;
            }

            StringBuilder result = new StringBuilder(length + (length >> 1));

            int upperCount = 0;

            for (int i = 0; i < length; ++i) {
                char ch = input.charAt(i);
                char lc = Character.toLowerCase(ch);

                if (lc == ch) { // lower-case letter means we can get new word
                    // but need to check for multi-letter upper-case (acronym), where assumption
                    // is that the last upper-case char is start of a new word
                    if ((upperCount > 1)  ){
                        // so insert hyphen before the last character now
                        result.insert(result.length() - 1, '-');
                    } else if ((upperCount == 1) && Character.isDigit(ch) && i != length-1) {
                        result.append('-');
                    }
                    upperCount = 0;
                } else {
                    // Otherwise starts new word, unless beginning of string
                    if ((upperCount == 0) && (i > 0)) {
                        result.append('-');
                    }
                    ++upperCount;
                }
                result.append(lc);
            }
            return result.toString();
        }

        /**
         * Key format like this: "FapServiceKey{_index=2}"
         *
         * @return
         */
        private String getKeyString(Identifiable<?> indentifiableObject) {
            String keyString = (indentifiableObject.key()).toString();
            int start = keyString.indexOf("=") + 1;
            int end = keyString.length() - 1;
            if (start > 0 && start < end)
                return keyString.substring(keyString.indexOf("=") + 1, keyString.length() - 1);
            else
                throw new IllegalArgumentException("indentifiable object without key");
        }

        public Instant getTime(Notification notification) {
            @NonNull
            Instant time;
            if (notification instanceof EventInstantAware) { // If notification class extends/implements the EventInstantAware
                time = ((EventInstantAware) notification).eventInstant();
                log.debug("Event time {}", time);
            } else {
                time = Instant.now();
                log.debug("Defaulting to actual time of processing the notification - {}", time);
            }
            return time;
        }
}
