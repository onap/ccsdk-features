package org.onap.ccsdk.features.sdnr.wt.yang.mapper.mapperextensions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapper;
import org.onap.ccsdk.features.sdnr.wt.yang.mapper.YangToolsMapperHelper;
import org.opendaylight.yangtools.yang.binding.Identifiable;
import org.opendaylight.yangtools.yang.binding.Identifier;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.Maps;

public class YangtoolsMapDesirializer<K extends Identifier<V>, V extends Identifiable<K>>
		extends JsonDeserializer<Map<K, V>> {

	private final Class<V> clazz;
	private final YangToolsMapper mapper;
	
	public YangtoolsMapDesirializer(Class<V> clazz) {
		super();
		this.clazz = clazz;
		this.mapper = new YangToolsMapper();
	}

	@Override
	public Map<K, V> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		CollectionLikeType type = ctxt.getTypeFactory().constructCollectionType(List.class, clazz);
		List<V> list = mapper.readValue(p,type);
		return YangToolsMapperHelper.toMap(list);
	}

}
