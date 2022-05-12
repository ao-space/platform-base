package xyz.eulix.platform.services.support.boundary.push;

import lombok.Data;

@Data
public class UploadReq {
    // 必填, 应用唯一标识
    private String appkey;

    // 必填, 时间戳，10位或者13位均可, 时间戳有效期为10分钟
    private String timestamp;

    // 必填, 文件内容, 多个device_token/alias请用回车符"\n"分隔
    private String content;
}
