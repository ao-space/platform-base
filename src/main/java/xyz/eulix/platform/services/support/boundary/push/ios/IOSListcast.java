package xyz.eulix.platform.services.support.boundary.push.ios;

import xyz.eulix.platform.services.support.boundary.push.IOSNotification;

public class IOSListcast extends IOSNotification {
    public IOSListcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "listcast");
    }

    public void setDeviceToken(String tokens) {
        setPredefinedKeyValue("device_tokens", tokens);
    }
}
