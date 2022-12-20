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

package xyz.eulix.platform.services.registry.dto.registry;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
public enum RegistryTypeEnum {
    BOX("box", "傲来盒子"),
    USER_ADMIN("user_admin", "用户管理员"),
    USER_MEMBER("user_member", "用户成员"),
    CLIENT_BIND("client_bind", "绑定类型客户端"),
    CLIENT_AUTH("client_auth", "授权类型客户端"),
    ;

    @Getter
    private final String name;

    @Getter
    private final String desc;

    public static RegistryTypeEnum fromValue(String value) {
        return Arrays.stream(values()).filter(appType -> appType.getName().equals(value)).findFirst().orElseThrow();
    }
}
