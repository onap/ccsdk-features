package org.opendaylight.yang.gen.v1.http.org.openroadm.pm.types.rev191129;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;
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
@JsonPOJOBuilder(buildMethodName = "build", withPrefix = "with")
public class PmDataTypeBuilder {
    private Uint64 _uint64;
    private BigInteger _unint64;
    private Long _int64;
    private BigDecimal _decimal64;

    public PmDataTypeBuilder() {
        System.out.println("--  Builder");
    }

    public PmDataTypeBuilder setUnint64(String v) {
        _unint64 = new BigInteger(v);
        return this;
    }

    public PmDataTypeBuilder setUint64(String v) {
        _uint64 = Uint64.valueOf(v);
        return this;
    }

    public PmDataTypeBuilder setInt64(String v) {
        _int64 = new Long(v);
        return this;
    }

    public PmDataTypeBuilder setDecimal64(String v) {
        _decimal64 = new BigDecimal(v);
        return this;
    }

    public PmDataType build() {
        Optional<Constructor<PmDataType>> cons1;
        try {
            cons1 = Optional.of(PmDataType.class.getConstructor(BigInteger.class));
        } catch (NoSuchMethodException | SecurityException e) {
            cons1 = Optional.empty();
        }
        Optional<Constructor<PmDataType>> cons2;
        try {
            cons2 = Optional.of(PmDataType.class.getConstructor(Uint64.class));
        } catch (NoSuchMethodException | SecurityException e) {
            cons2 = Optional.empty();
        }

        try {
            if (_unint64 != null) {
                return cons1.get().newInstance(_unint64);
            } else if (_uint64 != null) {
                return cons2.get().newInstance(_uint64);
            } else if (_int64 != null) {
                return new PmDataType(_int64);
            } else {
                return new PmDataType(_decimal64);
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalArgumentException("Wrong stuff");
        }
    }

    public static PmDataType getDefaultInstance(String defaultValue) {
        return new PmDataTypeBuilder().setInt64("-1").build();
    }

}
