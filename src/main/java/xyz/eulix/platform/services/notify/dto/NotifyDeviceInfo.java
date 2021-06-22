package xyz.eulix.platform.services.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of register a device.
 */
@Data
public class NotifyDeviceInfo {
    @NotBlank
    private final String clientUUID;

    @NotBlank
    private final String clientRegKey;

    @NotBlank
    private final String deviceId;

    private final String deviceToken;

    @NotBlank
    private final String platform;
}