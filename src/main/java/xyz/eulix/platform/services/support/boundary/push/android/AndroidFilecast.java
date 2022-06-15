package xyz.eulix.platform.services.support.boundary.push.android;

import xyz.eulix.platform.services.support.boundary.push.AndroidNotification;

public class AndroidFilecast extends AndroidNotification {
	public AndroidFilecast(String appkey, String appMasterSecret) {
			setAppMasterSecret(appMasterSecret);
			setPredefinedKeyValue("appkey", appkey);
			this.setPredefinedKeyValue("type", "filecast");	
	}
	
	public void setFileId(String fileId) {
    	setPredefinedKeyValue("file_id", fileId);
    }
}