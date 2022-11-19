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

package xyz.eulix.platform.services.auth.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "pkey_auth")
public class PkeyAuthEntity extends BaseEntity {
    // 二维码pkey值
    @NotBlank
    @Column(name = "pkey")
    private String pkey;

    // pkey超时时间
    @NotNull
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    // 登录box端的key
    @Column(name = "bkey")
    private String bkey;

    // 盒子域名
    @Column(name = "user_domain")
    private String userDomain;

    // 盒子公钥
    @Column(name = "box_pub_key")
    private String boxPubKey;
}
