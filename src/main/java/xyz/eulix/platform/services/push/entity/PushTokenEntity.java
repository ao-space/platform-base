package xyz.eulix.platform.services.push.entity;

import lombok.*;
import xyz.eulix.platform.services.push.dto.DeviceTypeEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "push_token")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class PushTokenEntity extends BaseEntity {
    @NotBlank
    @Column(name = "client_uuid")
    private String clientUUID;

    @NotBlank
    @Column(name = "device_token")
    private String deviceToken;

    @NotBlank
    @ValueOfEnum(enumClass = DeviceTypeEnum.class, valueMethod = "getName")
    @Column(name = "device_type")
    private String deviceType;

    @NotBlank
    @Column(name = "extra")
    private String extra;
}
