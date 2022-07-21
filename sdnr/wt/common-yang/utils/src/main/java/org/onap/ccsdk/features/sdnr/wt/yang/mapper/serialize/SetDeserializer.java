package org.onap.ccsdk.features.sdnr.wt.yang.mapper.serialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;

public class SetDeserializer<V>
        extends JsonDeserializer<Set<V>> {

    private final Class<V> clazz;
    private final YangToolsMapper mapper;

    public SetDeserializer(Class<V> clazz) {
        super();
        this.clazz = clazz;
        this.mapper = new YangToolsMapper();
    }

    @Override
    public Set<V> deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        CollectionLikeType type = ctxt.getTypeFactory().constructCollectionType(Set.class, clazz);
        List<V> list = mapper.readValue(p, type);
        return new HashSet<>(list);
    }

}
