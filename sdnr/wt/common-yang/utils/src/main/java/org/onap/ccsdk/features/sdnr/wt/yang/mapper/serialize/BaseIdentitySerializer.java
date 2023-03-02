package org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.opendaylight.yangtools.yang.binding.BaseIdentity;

import java.io.IOException;

public class BaseIdentitySerializer extends JsonSerializer<BaseIdentity> {

    @Override
    public void serialize(BaseIdentity value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String clsName = value.getClass().getName();
        int idx = clsName.indexOf("$");
        if(idx>0){
            clsName = clsName.substring(0,idx);
        }
        gen.writeString(clsName);
    }
}