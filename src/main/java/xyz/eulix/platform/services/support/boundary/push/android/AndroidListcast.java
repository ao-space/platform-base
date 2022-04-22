package xyz.eulix.platform.services.support.boundary.push.android;

import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidListcast extends AndroidNotification {
    public AndroidListcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "listcast");
    }

    public void setDeviceTokens(String tokens) {
        setPredefinedKeyValue("device_tokens", tokens);
    }

}