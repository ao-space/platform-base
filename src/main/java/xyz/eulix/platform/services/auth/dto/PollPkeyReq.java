package xyz.eulix.platform.services.auth.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class PollPkeyReq {
    // 二维码pkey值
    @NotBlank
    private String pkey;

}
