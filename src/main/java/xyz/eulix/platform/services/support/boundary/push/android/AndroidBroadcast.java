package xyz.eulix.platform.services.support.boundary.push.android;

import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidBroadcast extends AndroidNotification {
    public AndroidBroadcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "broadcast");
    }
}
