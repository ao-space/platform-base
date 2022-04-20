package xyz.eulix.platform.services.support.boundary.push;

import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.client.exception.ResteasyWebApplicationException;
import xyz.eulix.platform.services.config.QuarkusProperties;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class PushClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    @RestClient
    UPushService uPushService;

    @Inject
    QuarkusProperties properties;

	// The post path
	protected static final String postPath = "/api/send";

    public Boolean sendMessage(UmengNotification msg) {
        String timestamp = Integer.toString((int)(System.currentTimeMillis() / 1000));
        msg.setPredefinedKeyValue("timestamp", timestamp);
        String url = properties.getUPushHost() + postPath;
        try {
            String sign = DigestUtils.md5Hex(("POST" + url + msg.getPostBody() + msg.getAppMasterSecret()).getBytes("utf8"));
            PushMsgRes pushMsgRes = uPushService.pushMessage(msg.getPostBody(), sign);
            if ("SUCCESS".equals(pushMsgRes.getRet())) {
                LOG.infov("Notification sent successfully. Msg Id:{0}", pushMsgRes.getData().getMsgId());
                return true;
            }
            LOG.errorv("Failed to send the notification! Error code:{0}, error msg:{1}", pushMsgRes.getData().getErrorCode(),
                    pushMsgRes.getData().getErrorMsg());
        } catch (ResteasyWebApplicationException e) {
            // 外部服务4xx、5xx异常
            String errorMsg = e.unwrap().getResponse().readEntity(String.class);
            LOG.errorv("Call UPush API failed. Error msg:{0}", errorMsg);
        } catch (Exception e) {
            // UnknownHostException等异常
            LOG.errorv(e, "Call UPush API failed.");
        }
        return false;
    }
}




