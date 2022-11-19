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

package xyz.eulix.platform.services.network.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

@AllArgsConstructor
public enum NetworkServerStateEnum {
    OFFLINE(0, "未启用"),
    ONLINE(1, "已上线")
    ;

    @Setter @Getter
    private Integer state;

    @Setter @Getter
    private String desc;

    public static NetworkServerStateEnum fromValue(Integer value) {
        return Arrays.stream(values()).filter(state -> {
            if (state.getState().equals(value)) {
                return true;
            }
            return false;
        }).findFirst().orElseThrow();
    }
}
