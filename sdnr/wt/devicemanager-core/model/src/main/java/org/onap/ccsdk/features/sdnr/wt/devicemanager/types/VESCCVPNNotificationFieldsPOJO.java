
package org.onap.ccsdk.features.sdnr.wt.devicemanager.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class VESCCVPNNotificationFieldsPOJO {

    private String changeIdentifier;
    private String changeType;
    private String ccvpnNotificationFieldsVersion = "1.0"; // Only mandatory field;
    private ArrayList<HashMap<String, Object>> arrayOfNamedHashMap = new ArrayList<HashMap<String, Object>>();

    public String getChangeIdentifier() {
        return changeIdentifier;
    }

    public void setChangeIdentifier(String changeIdentifier) {
        this.changeIdentifier = changeIdentifier;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getCcvpnNotificationFieldsVersion() {
        return ccvpnNotificationFieldsVersion;
    }

    public void setCcvpnNotificationFieldsVersion(String ccvpnNotificationFieldsVersion) {
        this.ccvpnNotificationFieldsVersion = ccvpnNotificationFieldsVersion;
    }

    public ArrayList<HashMap<String, Object>> getArrayOfNamedHashMap() {
        return arrayOfNamedHashMap;
    }

    public void setArrayOfNamedHashMap(ArrayList<HashMap<String, Object>> arrayOfNamedHashMap) {
        this.arrayOfNamedHashMap = arrayOfNamedHashMap;
    }

}