package org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.common.Uint64;

/**
 * The purpose of generated class in src/main/java for Union types is to create new instances of unions from a string
 * representation. In some cases it is very difficult to automate it since there can be unions such as (uint32 -
 * uint16), or (string - uint32).
 *
 * The reason behind putting it under src/main/java is: This class is generated in form of a stub and needs to be
 * finished by the user. This class is generated only once to prevent loss of user code.
 *
 */
@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "set")
public class PmDataTypeBuilder {
    private Uint64 _uint64;
    private Long _int64;
    private BigDecimal _decimal64;

    public PmDataTypeBuilder() {}

    //Aluminium uses constructor
    public PmDataTypeBuilder(String v) {
        this.setUint64(v);
    }

    public PmDataTypeBuilder setUint64(String v) {
        _uint64 = Uint64.valueOf(v);
        return this;
    }

    public PmDataTypeBuilder setInt64(String v) {
        _int64 = Long.valueOf(v);
        return this;
    }

    public PmDataTypeBuilder setDecimal64(String v) {
        _decimal64 = new BigDecimal(v);
        return this;
    }

    public PmDataType build() {
        if (_uint64 != null) {
            return new PmDataType(_uint64);
        } else if (_int64 != null) {
            return new PmDataType(_int64);
        } else {
            return new PmDataType(_decimal64);
        }
    }

    public static PmDataType getDefaultInstance(String defaultValue) {
        return new PmDataTypeBuilder().setUint64(defaultValue).build();
    }

}
