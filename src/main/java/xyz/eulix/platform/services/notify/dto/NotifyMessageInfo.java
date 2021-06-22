package xyz.eulix.platform.services.notify.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.HashMap;

/**
 * Used to define a data transfer object as REST request for holding related parameters
 * of creating a message.
 */
@Data(staticConstructor = "of")
public class NotifyMessageInfo {
    @NotBlank
    private final String title;

    @NotBlank
    private final String body;

    @NotBlank
    private final String clientUUID;

    private final HashMap<String, Object> extParameters;

}