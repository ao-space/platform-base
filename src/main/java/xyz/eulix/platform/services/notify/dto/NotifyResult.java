package xyz.eulix.platform.services.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data(staticConstructor = "of")
public class NotifyResult<T> {
    @NotBlank
    private final int code;

    @NotBlank
    private final T data;
}
