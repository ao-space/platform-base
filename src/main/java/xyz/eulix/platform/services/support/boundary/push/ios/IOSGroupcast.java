package xyz.eulix.platform.services.support.boundary.push.ios;

import com.fasterxml.jackson.databind.node.ObjectNode;
import xyz.eulix.platform.services.support.boundary.push.IOSNotification;

public class IOSGroupcast extends IOSNotification {
    public IOSGroupcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "groupcast");
    }

    public void setFilter(ObjectNode filter) {
        setPredefinedKeyValue("filter", filter);
    }
}
