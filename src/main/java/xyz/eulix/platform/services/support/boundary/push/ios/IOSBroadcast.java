package xyz.eulix.platform.services.support.boundary.push.ios;

import xyz.eulix.platform.services.support.boundary.push.IOSNotification;

public class IOSBroadcast extends IOSNotification {
    public IOSBroadcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "broadcast");

    }
}
