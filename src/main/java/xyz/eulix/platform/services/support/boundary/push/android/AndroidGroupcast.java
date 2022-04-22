package xyz.eulix.platform.services.support.boundary.push.android;

import com.fasterxml.jackson.databind.node.ObjectNode;
import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidGroupcast extends AndroidNotification {
    public AndroidGroupcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "groupcast");
    }

    public void setFilter(ObjectNode filter) {
        setPredefinedKeyValue("filter", filter);
    }
}
