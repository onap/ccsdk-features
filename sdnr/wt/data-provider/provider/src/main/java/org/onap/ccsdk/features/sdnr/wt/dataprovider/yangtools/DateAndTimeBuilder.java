package org.onap.ccsdk.features.sdnr.wt.dataprovider.yangtools;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.DateAndTime;

public class DateAndTimeBuilder {
	 private final String _value;

     public DateAndTimeBuilder(String v) {
         this._value = v;
     }

     public DateAndTime build() {
         return new DateAndTime(_value);
     }
}
