package xyz.eulix.platform.services.network.entity;

import lombok.*;
import xyz.eulix.platform.common.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "network_client_server_route")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class NetworkRouteEntity extends BaseEntity {
    @NotBlank
    @Column(name = "network_client_id")
    private String clientId;

    @NotNull
    @Column(name = "network_server_id")
    private Long serverId;
}
