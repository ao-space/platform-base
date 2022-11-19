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
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.CommonUtils;

import javax.persistence.MappedSuperclass;

@Getter
@Setter
@ToString
@MappedSuperclass
public class BoxExcelModel {
  @ExcelProperty("序号") private String number;
  @ExcelProperty("IP")  private String ip;
  @ExcelProperty("版本") private String version;
  @ExcelProperty("MAC") private String mac;
  @ExcelProperty("CPU数") private String cpuCount;
  @ExcelProperty("CPU ID") private String cpuId;
  @ExcelProperty("内存（M）") private String memory;
  @ExcelProperty("Wi-Fi") private String wifi;
  @ExcelProperty("蓝牙") private String bluetooth;
  @ExcelProperty("USB") private String usb;
  @ExcelProperty("操作用户") private  String operateUser;
  @ExcelProperty("其他") private String other;
  @ExcelProperty("时间") private String time;

  private String boxqrcode;
  private String btid;
  private String boxUuid;
  private String btidHash;

  public boolean isNUllOrEmpty(){
    if(CommonUtils.isNullOrEmpty(this.number) && CommonUtils.isNullOrEmpty(this.ip) && CommonUtils.isNullOrEmpty(this.version) &&
       CommonUtils.isNullOrEmpty(this.mac) && CommonUtils.isNullOrEmpty(this.cpuCount) && CommonUtils.isNullOrEmpty(this.cpuId) &&
       CommonUtils.isNullOrEmpty(this.memory) && CommonUtils.isNullOrEmpty(this.wifi) && CommonUtils.isNullOrEmpty(this.bluetooth) &&
       CommonUtils.isNullOrEmpty(this.usb) && CommonUtils.isNullOrEmpty(this.operateUser) && CommonUtils.isNullOrEmpty(this.other) &&
       CommonUtils.isNullOrEmpty(this.time)){
      return true;
    } else {
      return false;
    }
  }
 }
