package xyz.eulix.platform.services.support.boundary.push;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.HashSet;

public abstract class AndroidNotification extends UmengNotification {
    // Keys can be set in the payload level
    protected static final HashSet<String> PAYLOAD_KEYS = new HashSet<String>(Arrays.asList(new String[]{
            "display_type"}));

    // Keys can be set in the body level
    protected static final HashSet<String> BODY_KEYS = new HashSet<String>(Arrays.asList(new String[]{
            "ticker", "title", "text", "builder_id", "icon", "largeIcon", "img", "play_vibrate", "play_lights", "play_sound",
            "sound", "after_open", "url", "activity", "custom"}));

    // Set key/value in the rootJson, for the keys can be set please see ROOT_KEYS, PAYLOAD_KEYS, BODY_KEYS and POLICY_KEYS.
    @Override
    public boolean setPredefinedKeyValue(String key, Object value) {
        if (ROOT_KEYS.contains(key)) {
            // This key should be in the root level
            rootJson.putPOJO(key, value);
        } else if (PAYLOAD_KEYS.contains(key)) {
            // This key should be in the payload level
            ObjectNode payloadJson = null;
            if (rootJson.has("payload")) {
                payloadJson = (ObjectNode) rootJson.get("payload");
            } else {
                payloadJson = objectMapper.createObjectNode();
                rootJson.set("payload", payloadJson);
            }
            payloadJson.putPOJO(key, value);
        } else if (BODY_KEYS.contains(key)) {
            // This key should be in the body level
            ObjectNode bodyJson = null;
            ObjectNode payloadJson = null;
            // 'body' is under 'payload', so build a payload if it doesn't exist
            if (rootJson.has("payload")) {
                payloadJson = (ObjectNode) rootJson.get("payload");
            } else {
                payloadJson = objectMapper.createObjectNode();
                rootJson.set("payload", payloadJson);
            }
            // Get body JSONObject, generate one if not existed
            if (payloadJson.has("body")) {
                bodyJson = (ObjectNode) payloadJson.get("body");
            } else {
                bodyJson = objectMapper.createObjectNode();
                payloadJson.set("body", bodyJson);
            }
            bodyJson.putPOJO(key, value);
        } else if (POLICY_KEYS.contains(key)) {
            // This key should be in the body level
            ObjectNode policyJson = null;
            if (rootJson.has("policy")) {
                policyJson = (ObjectNode) rootJson.get("policy");
            } else {
                policyJson = objectMapper.createObjectNode();
                rootJson.set("policy", policyJson);
            }
            policyJson.putPOJO(key, value);
        } else {
            if (key == "payload" || key == "body" || key == "policy" || key == "extra") {
                throw new IllegalArgumentException("You don't need to set value for " + key + " , just set values for the sub keys in it.");
            } else {
                throw new IllegalArgumentException("Unknown key: " + key);
            }
        }
        return true;
    }

    // Set extra key/value for Android notification
    public boolean setExtraField(String key, String value) {
        ObjectNode payloadJson = null;
        ObjectNode extraJson = null;
        if (rootJson.has("payload")) {
            payloadJson = (ObjectNode) rootJson.get("payload");
        } else {
            payloadJson = objectMapper.createObjectNode();
            rootJson.set("payload", payloadJson);
        }

        if (payloadJson.has("extra")) {
            extraJson = (ObjectNode) payloadJson.get("extra");
        } else {
            extraJson = objectMapper.createObjectNode();
            payloadJson.set("extra", extraJson);
        }
        extraJson.put(key, value);
        return true;
    }

    // 通知类型
    public void setDisplayType(String d) {
        setPredefinedKeyValue("display_type", d);
    }

    // 通知栏提示文字
    public void setTicker(String ticker) {
        setPredefinedKeyValue("ticker", ticker);
    }

    // 通知标题
    public void setTitle(String title) {
        setPredefinedKeyValue("title", title);
    }

    // 通知文字描述
    public void setText(String text) {
        setPredefinedKeyValue("text", text);
    }

    // 用于标识该通知采用的样式。使用该参数时, 必须在SDK里面实现自定义通知栏样式。
    public void setBuilderId(Integer builder_id) {
        setPredefinedKeyValue("builder_id", builder_id);
    }

    // 状态栏图标ID, R.drawable.[smallIcon],如果没有, 默认使用应用图标。
    public void setIcon(String icon) {
        setPredefinedKeyValue("icon", icon);
    }

    // 通知栏拉开后左侧图标ID
    public void setLargeIcon(String largeIcon) {
        setPredefinedKeyValue("largeIcon", largeIcon);
    }

    // 通知栏大图标的URL链接。该字段的优先级大于largeIcon。该字段要求以http或者https开头。
    public void setImg(String img) {
        setPredefinedKeyValue("img", img);
    }

    // 收到通知是否震动,默认为"true"
    public void setPlayVibrate(Boolean play_vibrate) {
        setPredefinedKeyValue("play_vibrate", play_vibrate.toString());
    }

    // 收到通知是否闪灯,默认为"true"
    public void setPlayLights(Boolean play_lights) {
        setPredefinedKeyValue("play_lights", play_lights.toString());
    }

    // 收到通知是否发出声音,默认为"true"
    public void setPlaySound(Boolean play_sound) {
        setPredefinedKeyValue("play_sound", play_sound.toString());
    }

    // 通知声音，R.raw.[sound]. 如果该字段为空，采用SDK默认的声音
    public void setSound(String sound) {
        setPredefinedKeyValue("sound", sound);
    }

    // 收到通知后播放指定的声音文件
    public void setPlaySound(String sound) {
        setPlaySound(true);
        setSound(sound);
    }

    // 点击"通知"的后续行为，默认为打开app。
    public void goAppAfterOpen() {
        setAfterOpenAction(AfterOpenAction.GO_APP);
    }

    public void goUrlAfterOpen(String url) {
        setAfterOpenAction(AfterOpenAction.GO_URL);
        setUrl(url);
    }

    public void goActivityAfterOpen(String activity) {
        setAfterOpenAction(AfterOpenAction.GO_ACTIVITY);
        setActivity(activity);
    }

    public void goCustomAfterOpen(String custom) {
        setAfterOpenAction(AfterOpenAction.GO_CUSTOM);
        setCustomField(custom);
    }

    public void goCustomAfterOpen(ObjectNode custom) {
        setAfterOpenAction(AfterOpenAction.GO_CUSTOM);
        setCustomField(custom);
    }

    // 点击"通知"的后续行为，默认为打开app。原始接口
    public void setAfterOpenAction(AfterOpenAction action) {
        setPredefinedKeyValue("after_open", action.getName());
    }

    public void setUrl(String url) {
        setPredefinedKeyValue("url", url);
    }

    public void setActivity(String activity) {
        setPredefinedKeyValue("activity", activity);
    }

    // can be a string of json
    public void setCustomField(String custom) {
        setPredefinedKeyValue("custom", custom);
    }

    public void setCustomField(ObjectNode custom) {
        setPredefinedKeyValue("custom", custom);
    }

}
