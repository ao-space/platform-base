package xyz.eulix.platform.services.registry.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Entity;
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
 }
