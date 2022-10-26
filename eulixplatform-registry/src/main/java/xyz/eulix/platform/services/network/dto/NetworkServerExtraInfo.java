package xyz.eulix.platform.services.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data(staticConstructor = "of")
public class NetworkServerExtraInfo {
    @JsonProperty("stun_addr")
    private String stunAddress;
}
