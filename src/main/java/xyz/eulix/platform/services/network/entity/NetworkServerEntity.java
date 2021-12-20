package xyz.eulix.platform.services.network.entity;

import lombok.*;
import xyz.eulix.platform.services.network.dto.NetworkServerStateEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "network_server_info")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class NetworkServerEntity extends BaseEntity {
    @NotBlank
    @Column(name = "server_protocol")
    private String protocol;

    @NotBlank
    @Column(name = "server_addr")
    private String addr;

    @NotNull
    @Column(name = "server_port")
    private Integer port;

    @NotBlank
    @Column(name = "identifier")
    private String identifier;

    @NotNull
    @Column(name = "state")
    @ValueOfEnum(enumClass = NetworkServerStateEnum.class, valueMethod = "getState")
    private Integer state;
}
