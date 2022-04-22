package xyz.eulix.platform.services.support.boundary.push.android;

import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidCustomizedcast extends AndroidNotification {
    public AndroidCustomizedcast(String appkey, String appMasterSecret) {
        setAppMasterSecret(appMasterSecret);
        setPredefinedKeyValue("appkey", appkey);
        this.setPredefinedKeyValue("type", "customizedcast");
    }

    public void setAlias(String alias, String aliasType) {
        setPredefinedKeyValue("alias", alias);
        setPredefinedKeyValue("alias_type", aliasType);
    }

    public void setFileId(String fileId, String aliasType) {
        setPredefinedKeyValue("file_id", fileId);
        setPredefinedKeyValue("alias_type", aliasType);
    }
}
