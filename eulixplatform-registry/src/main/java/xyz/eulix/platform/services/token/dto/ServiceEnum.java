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

package xyz.eulix.platform.services.token.dto;

import java.util.Arrays;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ServiceEnum {
    REGISTRY("10001", "官方空间平台"),
    OPSTAGE("10002", "官方产品服务平台"),
    ;

    @Getter
    final String serviceId;
    final String serviceName;

    public static ServiceEnum fromValue(String value) {
        return Arrays.stream(values()).filter(serviceType -> serviceType.getServiceId().equals(value)).findFirst().orElseThrow();
    }
}
