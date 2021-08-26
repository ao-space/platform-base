package xyz.eulix.platform.services.appmgt.dto;

import lombok.Data;

@Data(staticConstructor = "of")
public class BaseResultRes {
    private final Boolean result;
}
