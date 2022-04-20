package xyz.eulix.platform.services.support.boundary.push;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.HashSet;

public abstract class IOSNotification extends UmengNotification {

    // Keys can be set in the aps level
    protected static final HashSet<String> APS_KEYS = new HashSet<String>(Arrays.asList(new String[]{
            "alert", "badge", "sound", "content-available"
    }));

    @Override
    public boolean setPredefinedKeyValue(String key, Object value) {
        if (ROOT_KEYS.contains(key)) {
            // This key should be in the root level
            rootJson.putPOJO(key, value);
        } else if (APS_KEYS.contains(key)) {
            // This key should be in the aps level
            ObjectNode apsJson = null;
            ObjectNode payloadJson = null;
            if (rootJson.has("payload")) {
                payloadJson = (ObjectNode) rootJson.get("payload");
            } else {
                payloadJson = objectMapper.createObjectNode();
                rootJson.set("payload", payloadJson);
            }
            if (payloadJson.has("aps")) {
                apsJson = (ObjectNode) payloadJson.get("aps");
            } else {
                apsJson = objectMapper.createObjectNode();
                payloadJson.set("aps", apsJson);
            }
            apsJson.putPOJO(key, value);
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
            if (key == "payload" || key == "aps" || key == "policy") {
                throw new IllegalArgumentException("You don't need to set value for " + key + " , just set values for the sub keys in it.");
            } else {
                throw new IllegalArgumentException("Unknownd key: " + key);
            }
        }

        return true;
    }

    // Set customized key/value for IOS notification
    public boolean setCustomizedField(String key, String value) {
        //rootJson.put(key, value);
        ObjectNode payloadJson = null;
        if (rootJson.has("payload")) {
            payloadJson = (ObjectNode) rootJson.get("payload");
        } else {
            payloadJson = objectMapper.createObjectNode();
            rootJson.set("payload", payloadJson);
        }
        payloadJson.put(key, value);
        return true;
    }

    public void setAlert(String token) {
        setPredefinedKeyValue("alert", token);
    }

    public void setAlert(String title, String subtitle, String body) {
        ObjectNode object = objectMapper.createObjectNode();
        object.put("title", title);
        object.put("subtitle", subtitle);
        object.put("body", body);
        setPredefinedKeyValue("alert", object);
    }

    public void setBadge(Integer badge) {
        setPredefinedKeyValue("badge", badge);
    }

    public void setSound(String sound) {
        setPredefinedKeyValue("sound", sound);
    }

    public void setContentAvailable(Integer contentAvailable) {
        setPredefinedKeyValue("content-available", contentAvailable);
    }
}
