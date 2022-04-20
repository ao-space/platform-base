package xyz.eulix.platform.services.support.boundary.push;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.HashSet;

public abstract class UmengNotification {
    // This JSONObject is used for constructing the whole request string.
    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final ObjectNode rootJson = objectMapper.createObjectNode();

    // The app master secret
    protected String appMasterSecret;

    // Keys can be set in the root level
    protected static final HashSet<String> ROOT_KEYS = new HashSet<String>(Arrays.asList(new String[]{
            "appkey", "timestamp", "type", "device_tokens", "alias", "alias_type", "file_id",
            "filter", "production_mode", "feedback", "description", "thirdparty_id", "mipush", "mi_activity", "channel_properties"}));

    // Keys can be set in the policy level
    protected static final HashSet<String> POLICY_KEYS = new HashSet<String>(Arrays.asList(new String[]{
            "start_time", "expire_time", "max_send_num"
    }));

    // Set predefined keys in the rootJson, for extra keys(Android) or customized keys(IOS) please
    // refer to corresponding methods in the subclass.
    public abstract boolean setPredefinedKeyValue(String key, Object value);

    public void setAppMasterSecret(String secret) {
        appMasterSecret = secret;
    }

    public String getPostBody() {
        return rootJson.toString();
    }

    protected final String getAppMasterSecret() {
        return appMasterSecret;
    }

    public void setProductionMode(Boolean prod) {
        setPredefinedKeyValue("production_mode", prod.toString());
    }

    // 正式模式
    public void setProductionMode() {
        setProductionMode(true);
    }

    // 测试模式
    public void setTestMode() {
        setProductionMode(false);
    }

    // 发送消息描述，建议填写。
    public void setDescription(String description) {
        setPredefinedKeyValue("description", description);
    }

    // 定时发送时间，若不填写表示立即发送。格式: "YYYY-MM-DD hh:mm:ss"。
    public void setStartTime(String startTime) {
        setPredefinedKeyValue("start_time", startTime);
    }

    // 消息过期时间,格式: "YYYY-MM-DD hh:mm:ss"。
    public void setExpireTime(String expireTime) {
        setPredefinedKeyValue("expire_time", expireTime);
    }

    // 发送限速，每秒发送的最大条数。
    public void setMaxSendNum(Integer num) {
        setPredefinedKeyValue("max_send_num", num);
    }

    // 厂商弹窗activity
    public void setChannelActivity(String activity) {
        setPredefinedKeyValue("mipush", "true");
        setPredefinedKeyValue("mi_activity", activity);
    }

    // 厂商属性配置
    public void setChannelProperties(String xiaoMiChannelId) {
        ObjectNode object = new ObjectMapper().createObjectNode();
        object.put("xiaomi_channel_id", xiaoMiChannelId);
        setPredefinedKeyValue("channel_properties", object);
    }

}
