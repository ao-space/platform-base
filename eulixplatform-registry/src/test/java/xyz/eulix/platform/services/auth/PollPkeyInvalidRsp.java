package xyz.eulix.platform.services.auth;

import lombok.Data;

@Data(staticConstructor = "of")
public class PollPkeyInvalidRsp {
    private String requestId;
    private String code;
    private String message;
}
