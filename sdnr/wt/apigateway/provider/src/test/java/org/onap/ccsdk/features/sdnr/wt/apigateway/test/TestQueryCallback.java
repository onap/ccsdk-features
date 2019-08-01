package org.onap.ccsdk.features.sdnr.wt.apigateway.test;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.onap.ccsdk.features.sdnr.wt.apigateway.EsServlet.IRequestCallback;
import org.onap.ccsdk.features.sdnr.wt.apigateway.MyProperties;
import org.onap.ccsdk.features.sdnr.wt.apigateway.test.helper.HelpEsServlet;
import org.onap.ccsdk.features.sdnr.wt.apigateway.test.helper.HelpServletBase;

public class TestQueryCallback extends HelpServletBase{

	private static final int PORT = 40011;
	
	public TestQueryCallback() {
		super("/database",PORT);
	}

	final String LR = "\n";

	
	private boolean hasCallback=false;
	
	@Test
	public void test() throws ServletException, IOException {

		String tmpFilename = "tmp1.cfg";
		File tmpFile = new File(tmpFilename);
		if (tmpFile.exists())
			tmpFile.delete();
		MyProperties properties = MyProperties.Instantiate(tmpFile,true);
		String query = "{\"query\":{\"match_all\":{}}}";
		String tmpconfigcontent2 = "aai=off" + LR + "aaiHeaders=[]" + LR + "database=http://" + HOST + ":" + PORT + LR
				+ "insecure=1" + LR + "cors=1";
		HelpEsServlet servlet = new HelpEsServlet();
		this.setServlet(servlet);
		HelpEsServlet.registerRequestCallback("/mwtn/mediator-server", new IRequestCallback() {
			
			@Override
			public void onRequest(String uri, String method) {
				hasCallback=true;
				
			}
		});
		properties.load(new ByteArrayInputStream(tmpconfigcontent2.getBytes()));
		testrequest("/database/mwtn/mediator-server/_search",HTTPMETHOD_POST, query, HelpEsServlet.RESPONSE_POST, true);
		int wait=10;
		while(wait-->0) {
			if(hasCallback) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}			
		}
		assertTrue("no request callback received",hasCallback);
		hasCallback=false;
		testrequest("/database/mwtn/mediatr-server/_search",HTTPMETHOD_POST, query, HelpEsServlet.RESPONSE_POST, true);
		wait=5;
		while(wait-->0) {
			if(hasCallback) {
				break;
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				Thread.interrupted();
			}			
		}
		assertFalse("received request callback",hasCallback);
		
		 
		
		if (tmpFile.exists())
			tmpFile.delete();
		
		
	}
	@Before
	public void init() throws IOException{	
		HelpServletBase.initEsTestWebserver(PORT,"/database");
	}
	@After
	public void deinit() {
		HelpServletBase.stopTestWebserver();
	}

}
