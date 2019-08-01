package org.onap.ccsdk.features.sdnr.wt.apigateway.test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.DatabaseEntryProvider;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.DatabaseHttpClient;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.MediatorServerInfo;
import org.onap.ccsdk.features.sdnr.wt.apigateway.database.http.BaseHTTPResponse;

public class TestDatabaseHttpClient {

	private static final String ID_1="AWwscPepjf5-TrAFEdUD";
	private static final String HOSTNAME_1="http://192.168.178.89:7070";
	private static final String NAME_1="internal mediatorserver";
	private static final String ID_2="AWwscSCWjf5-TrAFEdUE";
	private static final String HOSTNAME_2="http://192.168.178.89:7071";
	private static final String NAME_2="internal mediatorserver2";
	private static final String ID_3="AWwscSCWjf5-TrAFEdsE";
	private static final String HOSTNAME_3="http://192.168.178.88:7371";
	private static final String NAME_3="test mediatorserver2";
	
	private static final String RESPONSE_VALID="{\n" + 
			"\"took\": 3,\n" + 
			"\"timed_out\": false,\n" + 
			"\"_shards\": {\n" + 
			"\"total\": 5,\n" + 
			"\"successful\": 5,\n" + 
			"\"failed\": 0\n" + 
			"},\n" + 
			"\"hits\": {\n" + 
			"\"total\": 2,\n" + 
			"\"max_score\": 1,\n" + 
			"\"hits\": [\n" + 
			"{\n" + 
			"\"_index\": \"mwtn_v1\",\n" + 
			"\"_type\": \"mediator-server\",\n" + 
			"\"_id\": \"AWwscPepjf5-TrAFEdUD\",\n" + 
			"\"_score\": 1,\n" + 
			"\"_source\": {\n" + 
			"\"name\": \"internal mediatorserver\",\n" + 
			"\"url\": \"http://192.168.178.89:7070\"\n" + 
			"}\n" + 
			"}\n" + 
			",\n" + 
			"{\n" + 
			"\"_index\": \"mwtn_v1\",\n" + 
			"\"_type\": \"mediator-server\",\n" + 
			"\"_id\": \"AWwscSCWjf5-TrAFEdUE\",\n" + 
			"\"_score\": 1,\n" + 
			"\"_source\": {\n" + 
			"\"name\": \"internal mediatorserver2\",\n" + 
			"\"url\": \"http://192.168.178.89:7071\"\n" + 
			"}\n" + 
			"}\n" + 
			"]\n" + 
			"}\n" + 
			"}";
	
	private class PublicDatabaseHttpClient extends DatabaseHttpClient{

		public PublicDatabaseHttpClient(String base, boolean trustAllCerts) {
			super(base, trustAllCerts);
		}
		@Override
		public BaseHTTPResponse sendRequest(String uri, String method, String body, Map<String, String> headers) {
			return new BaseHTTPResponse(200, RESPONSE_VALID);
		}
	}
	public class PublicDatabaseEntryProvider extends DatabaseEntryProvider {

		protected PublicDatabaseEntryProvider() {
			super(new PublicDatabaseHttpClient("http://localhost/",false), 60);
		}

	}
	@Test
	public void test() {
		
		final PublicDatabaseHttpClient client = new PublicDatabaseHttpClient("http://localhost/",false);
		Map<String, MediatorServerInfo> entries = client.requestEntries();
		assertEquals("result size is not correct",2,entries.size());
		assertEquals("hostname 1 is not correct",HOSTNAME_1,entries.get(ID_1).getHost());
		assertEquals("name 1 is not correct",NAME_1,entries.get(ID_1).getName());
		assertEquals("hostname 2 is not correct",HOSTNAME_2,entries.get(ID_2).getHost());
		assertEquals("name 2 is not correct",NAME_2,entries.get(ID_2).getName());
	}
	@Test
	public void test2() {
		final PublicDatabaseEntryProvider provider  = new PublicDatabaseEntryProvider();
		boolean reloaded = provider.triggerReloadSync();
		assertTrue("data were not reloaded",reloaded);
		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {
			Thread.interrupted();
		}			
	
		System.out.println(provider.printEntries());
		assertEquals("provider has not loaded data",HOSTNAME_1,provider.getHostUrl(ID_1));
		assertEquals("provider has not loaded data",HOSTNAME_2,provider.getHostUrl(ID_2));
		Map<String, MediatorServerInfo> entries2 = new HashMap<String,MediatorServerInfo>();
		entries2.put(ID_3, new MediatorServerInfo(NAME_3, HOSTNAME_3));
		provider.setEntries(entries2);	
		assertEquals("provider has not loaded data",HOSTNAME_3,provider.getHostUrl(ID_3));
		try {
			provider.close();
		} catch (Exception e) {
			
		}
		
		
	}
}
