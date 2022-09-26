package xyz.eulix.platform.services.registry.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import javax.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.common.support.CommonUtils;

@Getter
@Setter
@ToString
@MappedSuperclass
public class BoxExcelModelV2 {
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
  @ExcelProperty("授权类型") private String authType;
  @ExcelProperty("盒子公钥") private String boxPubKey;

  private String boxqrcode;
  private String btid;
  private String boxUuid;
  private String btidHash;

  public boolean isNUllOrEmpty(){
    return CommonUtils.isNullOrEmpty(this.number) && CommonUtils.isNullOrEmpty(this.ip)
        && CommonUtils.isNullOrEmpty(this.version) &&
        CommonUtils.isNullOrEmpty(this.mac) && CommonUtils.isNullOrEmpty(this.cpuCount)
        && CommonUtils.isNullOrEmpty(this.cpuId) &&
        CommonUtils.isNullOrEmpty(this.memory) && CommonUtils.isNullOrEmpty(this.wifi)
        && CommonUtils.isNullOrEmpty(this.bluetooth) &&
        CommonUtils.isNullOrEmpty(this.usb) && CommonUtils.isNullOrEmpty(this.operateUser)
        && CommonUtils.isNullOrEmpty(this.other) &&
        CommonUtils.isNullOrEmpty(this.time) && CommonUtils.isNullOrEmpty(this.authType)
        && CommonUtils.isNullOrEmpty(this.boxPubKey);
  }
 }
