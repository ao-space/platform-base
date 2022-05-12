package xyz.eulix.platform.services.support.boundary.push.ios;


import xyz.eulix.platform.services.support.boundary.push.IOSNotification;

public class IOSFilecast extends IOSNotification {
	public IOSFilecast(String appkey, String appMasterSecret) {
			setAppMasterSecret(appMasterSecret);
			setPredefinedKeyValue("appkey", appkey);
			this.setPredefinedKeyValue("type", "filecast");	
	}
	
	public void setFileId(String fileId) {
    	setPredefinedKeyValue("file_id", fileId);
    }
}
