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

package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "box_info")
public class BoxInfoEntity extends BaseEntity {
  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @Column(name = "description")
  private String desc;

  // 扩展信息，json结构
  @Column(name = "extra")
  private String extra;

  @Column(name = "box_pub_key")
  private String boxPubKey;

  @Column(name = "auth_type")
  @ValueOfEnum(enumClass = AuthTypeEnum.class, valueMethod = "getName")
  private String authType;

  @OneToOne(cascade=CascadeType.ALL,fetch=FetchType.EAGER)
  @JoinColumn(name ="box_uuid",referencedColumnName="box_uuid",insertable=false,updatable=false)
  private RegistryBoxEntity registryBoxEntity;

}
