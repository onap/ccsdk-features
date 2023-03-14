package org.onap.ccsdk.features.sdnr.wt.oauthprovider;

import org.jolokia.osgi.security.Authenticator;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.InvalidConfigurationException;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.data.UnableToConfigureOAuthService;
import org.onap.ccsdk.features.sdnr.wt.oauthprovider.http.AuthHttpServlet;
import org.opendaylight.aaa.api.IdMService;
import org.opendaylight.mdsal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.aaa.app.config.rev170619.ShiroConfiguration;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;

public class Helper {

    private static final Logger LOG = LoggerFactory.getLogger(Helper.class);
    private AuthHttpServlet authServlet;

    public Helper() throws UnableToConfigureOAuthService, IOException, InvalidConfigurationException {
        this.authServlet = new AuthHttpServlet();

    }

    public void onUnbindService(HttpService httpService) {
        httpService.unregister(AuthHttpServlet.BASEURI);
        this.authServlet = null;
    }

    public void onBindService(HttpService httpService)
            throws ServletException, NamespaceException {
        if (httpService == null) {
            LOG.warn("Unable to inject HttpService into loader.");
        } else {
            httpService.registerServlet(AuthHttpServlet.BASEURI, authServlet, null, null);
            LOG.info("auth servlet registered.");
        }
    }

    public void setOdlAuthenticator(Authenticator odlAuthenticator) {
        authServlet.setOdlAuthenticator(odlAuthenticator);
    }

    public void setOdlIdentityService(IdMService odlIdentityService) {
        this.authServlet.setOdlIdentityService(odlIdentityService);
    }

    public void setShiroConfiguration(ShiroConfiguration shiroConfiguration) {
        this.authServlet.setShiroConfiguration(shiroConfiguration);
    }

    public void setDataBroker(DataBroker dataBroker) {
        this.authServlet.setDataBroker(dataBroker);
    }

    public void init() {

    }

    public void close() {

    }
}
