package xyz.eulix.platform.services.network.entity;

import lombok.*;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "network_server_info")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class NetworkServerEntity extends BaseEntity {

    @Column(name = "server_protocol")
    private String protocol;

    @NotNull
    @Column(name = "server_addr")
    private String addr;

    @Column(name = "server_port")
    private Integer port;
}
