package org.onap.ccsdk.features.sdnr.wt.oauthprovider.data;

import java.util.List;

public class OdlShiroConfiguration {

    private List<MainItem> main;
    private List<UrlItem> urls;



    public List<MainItem> getMain() {
        return main;
    }

    public void setMain(List<MainItem> main) {
        this.main = main;
    }
    public List<UrlItem> getUrls() {
        return urls;
    }
    public void setUrls(List<UrlItem> urls) {
        this.urls = urls;
    }
    public OdlShiroConfiguration(){

    }

    public static class BaseItem{
        private String pairKey;
        private String pairValue;

        public String getPairKey() {
            return pairKey;
        }

        public void setPairKey(String pairKey) {
            this.pairKey = pairKey;
        }

        public String getPairValue() {
            return pairValue;
        }

        public void setPairValue(String pairValue) {
            this.pairValue = pairValue;
        }

        public BaseItem(){

        }

    }

    public static class MainItem extends BaseItem{
        public MainItem(){
            super();
        }

    }
    public static class UrlItem extends BaseItem{
        public UrlItem(){
            super();
        }
    }

}
