package xyz.eulix.platform.services.support.boundary.push;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import xyz.eulix.platform.services.config.QuarkusProperties;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.boundary.push.android.AndroidFilecast;
import xyz.eulix.platform.services.support.boundary.push.ios.IOSFilecast;
import xyz.eulix.platform.services.support.serialization.OperationUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
public class PushClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    @RestClient
    UPushService uPushService;

    @Inject
    QuarkusProperties properties;

    @Inject
    OperationUtils operationUtils;

    // The post path
    protected static final String POST_PATH = "/api/send";

    protected static final String UPLOAD_PATH = "/upload";

    /**
     * device_token不能超过500个
     *
     * @param msg msg
     * @return 是否成功
     */
    public Boolean sendMessage(UmengNotification msg) {
        String timestamp = Integer.toString((int) (System.currentTimeMillis() / 1000));
        msg.setPredefinedKeyValue("timestamp", timestamp);
        String url = properties.getUPushHost() + POST_PATH;
        try {
            String sign = DigestUtils.md5Hex(("POST" + url + msg.getPostBody() + msg.getAppMasterSecret()).getBytes("utf8"));
            PushMsgRes pushMsgRes = uPushService.pushMessage(msg.getPostBody(), sign);
            if ("SUCCESS".equals(pushMsgRes.getRet())) {
                LOG.infov("Notification sent successfully. Msg Id:{0}. Or Task Id:{1}", pushMsgRes.getData().getMsgId(), pushMsgRes.getData().getTaskId());
                return true;
            }
            LOG.errorv("Failed to send the notification! Error code:{0}, error msg:{1}", pushMsgRes.getData().getErrorCode(),
                    pushMsgRes.getData().getErrorMsg());
        } catch (ResteasyWebApplicationException e) {
            // 外部服务4xx、5xx异常
            String errorMsg = e.unwrap().getResponse().readEntity(String.class);
            LOG.errorv("Call UPush API failed. Error msg:{0}", errorMsg);
        } catch (Exception e) {
            // UnknownHostException、ProcessingException、HttpHostConnectException等异常
            LOG.errorv(e, "Call UPush API failed.");
        }
        return false;
    }

    // Upload file with device_tokens to Umeng
    public String uploadContents(String appkey, String appMasterSecret, String contents) {
        // Construct the json string
        UploadReq uploadReq = new UploadReq();
        uploadReq.setAppkey(appkey);
        String timestamp = Integer.toString((int) (System.currentTimeMillis() / 1000));
        uploadReq.setTimestamp(timestamp);
        uploadReq.setContent(contents);

        // Send the post request and get the response
        String url = properties.getUPushHost() + UPLOAD_PATH;
        try {
            String sign = DigestUtils.md5Hex(("POST" + url + operationUtils.objectToJson(uploadReq) + appMasterSecret).getBytes("utf8"));
            PushMsgRes pushMsgRes = uPushService.uploadContents(uploadReq, sign);
            if ("SUCCESS".equals(pushMsgRes.getRet())) {
                LOG.infov("Upload file successfully. File Id:{0}.", pushMsgRes.getData().getFileId());
                return pushMsgRes.getData().getFileId();
            }
            LOG.errorv("Failed to upload file! Error code:{0}, error msg:{1}", pushMsgRes.getData().getErrorCode(),
                    pushMsgRes.getData().getErrorMsg());
        } catch (ResteasyWebApplicationException e) {
            // 外部服务4xx、5xx异常
            String errorMsg = e.unwrap().getResponse().readEntity(String.class);
            LOG.errorv("Call UPush API failed. Error msg:{0}", errorMsg);
        } catch (Exception e) {
            // UnknownHostException、ProcessingException、HttpHostConnectException等异常
            LOG.errorv(e, "Call UPush API failed.");
        }
        return null;
    }

    /**
     * device_token超过500个
     *
     * @param appKey appKey
     * @param msg msg
     * @param deviceTokens deviceTokens
     * @return 是否成功
     */
    public Boolean batchSendMessage(String appKey, AndroidFilecast msg, List<String> deviceTokens) {
        LOG.infov("Batch send android message, device tokens num :{0}.", deviceTokens.size());
        String fileId = this.uploadContents(appKey, msg.appMasterSecret, String.join("\n", deviceTokens));
        if (CommonUtils.isNotNull(fileId)) {
            msg.setFileId(fileId);
            return sendMessage(msg);
        }
        return false;
    }


    /**
     * device_token超过500个
     *
     * @param appKeyIOS appKeyIOS
     * @param msg msg
     * @param deviceTokens deviceTokens
     * @return 是否成功
     */
    public Boolean batchSendMessageIOS(String appKeyIOS, IOSFilecast msg, List<String> deviceTokens) {
        LOG.infov("Batch send ios message, device tokens num :{0}.", deviceTokens.size());
        String fileId = this.uploadContents(appKeyIOS, msg.appMasterSecret, String.join("\n", deviceTokens));
        if (CommonUtils.isNotNull(fileId)) {
            msg.setFileId(fileId);
            return sendMessage(msg);
        }
        return false;
    }
}




