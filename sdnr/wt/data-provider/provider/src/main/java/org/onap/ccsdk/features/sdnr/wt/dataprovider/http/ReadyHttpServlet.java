package org.onap.ccsdk.features.sdnr.wt.dataprovider.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadyHttpServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(ReadyHttpServlet.class);
	private boolean status;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
	
		if(this.isReady()) {
			resp.setStatus(HttpServletResponse.SC_OK);
		}
		else {
		
			resp.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	private boolean isReady() {
		return this.status;
	}

	public void setStatus(boolean status) {
		this.status = status;
		LOG.info("status is set to ready: {}",status);
	}
}
