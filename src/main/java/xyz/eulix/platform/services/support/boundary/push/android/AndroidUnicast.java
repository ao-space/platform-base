package xyz.eulix.platform.services.support.boundary.push.android;

import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidUnicast extends AndroidNotification {
    public AndroidUnicast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "unicast");
    }

    public void setDeviceToken(String token) {
        setPredefinedKeyValue("device_tokens", token);
    }

}