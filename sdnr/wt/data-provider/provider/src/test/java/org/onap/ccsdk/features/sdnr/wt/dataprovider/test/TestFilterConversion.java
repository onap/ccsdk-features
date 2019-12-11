package org.onap.ccsdk.features.sdnr.wt.dataprovider.test;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.common.database.queries.QueryBuilder;
import org.onap.ccsdk.features.sdnr.wt.dataprovider.data.QueryByFilter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.Filter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.data.provider.rev190801.entity.input.FilterBuilder;

public class TestFilterConversion {

	private static final String PROPERTY = "node-id";

	@Test
	public void testQuestionMark() {
		 List<Filter> filters = Arrays.asList(new FilterBuilder().setProperty(PROPERTY).setFiltervalue("si?ba").build());
		QueryBuilder query = QueryByFilter.fromFilter(filters );
		System.out.println(query.toJSON());
	}
}
