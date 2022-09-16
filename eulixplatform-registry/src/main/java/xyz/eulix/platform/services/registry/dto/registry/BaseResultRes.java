package xyz.eulix.platform.services.registry.dto.registry;

import lombok.Data;

@Data(staticConstructor = "of")
public class BaseResultRes {
    private final Boolean result;
}
