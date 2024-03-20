package org.onap.ccsdk.features.sdnr.wt.oauthprovider.data;

public class UnableToConfigureOAuthService extends Exception {

    public UnableToConfigureOAuthService(String configUrl){
        super(String.format("Unable to configure OAuth service from url %s", configUrl));
    }
    public UnableToConfigureOAuthService(String configUrl, int responseCode){
        super(String.format("Unable to configure OAuth service from url %s. bad response with code %d", configUrl, responseCode));
    }

}
