package xyz.eulix.platform.services.support.boundary.push.ios;

import xyz.eulix.platform.services.support.boundary.push.IOSNotification;

public class IOSUnicast extends IOSNotification {
    public IOSUnicast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "unicast");
    }

    public void setDeviceToken(String token) {
        setPredefinedKeyValue("device_tokens", token);
    }
}
