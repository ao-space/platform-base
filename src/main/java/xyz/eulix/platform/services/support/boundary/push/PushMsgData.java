package xyz.eulix.platform.services.support.boundary.push;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PushMsgData {
    @JsonProperty("msg_id")
    private String msgId;

    @JsonProperty("error_code")
    private String errorCode;

    @JsonProperty("error_msg")
    private String errorMsg;
}
