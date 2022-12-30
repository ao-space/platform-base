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

import com.alibaba.excel.annotation.ExcelProperty;

import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.CommonUtils;
import xyz.eulix.platform.common.support.validator.ValueOfEnum;
import xyz.eulix.platform.services.token.dto.AuthTypeEnum;

@Getter
@Setter
@ToString
@MappedSuperclass
public class BoxExcelModelV2 {
    @ExcelProperty("SN号")
    private String snNumber;
    @ExcelProperty("设备")
    private String device;
    @ExcelProperty("HW_ID")
    private String hwId;
    @ExcelProperty("BOM_ID")
    private String bomId;
    @ExcelProperty("RJ45 MAC")
    private String rj45Mac;
    @ExcelProperty("CPU数")
    private String cpuCount;
    @ExcelProperty("CPU ID")
    private String cpuId;
    @ExcelProperty("内存（M）")
    private String memory;
    @ExcelProperty("Wi-Fi MAC")
    private String wifiMac;
    @ExcelProperty("蓝牙 MAC")
    private String btMac;
    @ExcelProperty("公钥")
    private String boxPubKey;
    @ExcelProperty("信息获取时间")
    private String operateTime;

    private String boxqrcode;
    private String btid;
    private String boxUuid;
    private String btidHash;
    private String authType;

    public boolean isNUllOrEmpty() {
        return CommonUtils.isNullOrEmpty(this.snNumber)
                && CommonUtils.isNullOrEmpty(this.device)
                && CommonUtils.isNullOrEmpty(this.hwId)
                && CommonUtils.isNullOrEmpty(this.bomId)
                && CommonUtils.isNullOrEmpty(this.rj45Mac)
                && CommonUtils.isNullOrEmpty(this.cpuCount)
                && CommonUtils.isNullOrEmpty(this.cpuId)
                && CommonUtils.isNullOrEmpty(this.memory)
                && CommonUtils.isNullOrEmpty(this.wifiMac)
                && CommonUtils.isNullOrEmpty(this.btMac)
                && CommonUtils.isNullOrEmpty(this.boxPubKey)
                && CommonUtils.isNullOrEmpty(this.operateTime);
    }
}
