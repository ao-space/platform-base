/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.services.network.entity;

import lombok.*;
import xyz.eulix.platform.services.network.dto.NetworkServerStateEnum;
import xyz.eulix.platform.common.support.model.BaseEntity;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;

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

    // 扩展信息，json结构
    @Column(name = "extra")
    private String extra;
}
