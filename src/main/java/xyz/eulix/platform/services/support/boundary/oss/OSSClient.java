package xyz.eulix.platform.services.support.boundary.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectResult;
import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * OSS 客户端
 *
 * For more information:
 * <a href="ttps://help.aliyun.com/document_detail/31827.html">ttps://help.aliyun.com/document_detail/31827.html</a>.
 *
 */
@ApplicationScoped
public class OSSClient {
    private static final Logger LOG = Logger.getLogger("app.log");

    // Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com
    private String endpoint = null;
    // Bucket名称
    private String bucketName = null;

    private String accessKeyId = null;
    private String accessKeySecret = null;

    private OSS ossClient = null;

    @Inject
    ApplicationProperties properties;

    @PostConstruct
    private void init() {
        endpoint = properties.getOSSEndpoint();
        bucketName = properties.getOSSBucketName();
        accessKeyId = properties.getAliAccessKeyId();
        accessKeySecret = properties.getAliAccessKeySecret();
        // 创建OSSClient实例
        ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
    }

    @PreDestroy
    private void destroy() {
        // 关闭OSSClient
        ossClient.shutdown();
    }

    /**
     * 上传文件至 oss
     *
     * @param objectName Object完整路径
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult fileUpload(String objectName, File file) {
        try {
            return ossClient.putObject(bucketName, objectName, file);
        } catch (OSSException e) {
            LOG.error("OSS upload failed, exception", e);
            throw new ServiceOperationException(ServiceError.UPLOAD_FILE_FAILED);
        }
    }

    /**
     * 判断文件是否存在
     *
     * @param objectName Object完整路径
     * @return 是否存在
     */
    public boolean fileExistOrNot(String objectName) {
        try {
            return ossClient.doesObjectExist(bucketName, objectName);
        } catch (OSSException e) {
            LOG.error("OSS check object exist failed, exception", e);
            throw new ServiceOperationException(ServiceError.FILE_NOT_FOUND);
        }
    }

    /**
     * 从 oss 下载文件
     *
     * @param objectName Object完整路径
     * @return 文件流
     */
    public InputStream fileDownload(String objectName) {
        try {
            // 调用ossClient.getObject返回一个OSSObject实例，该实例包含文件内容及文件元信息。
            OSSObject ossObject = ossClient.getObject(bucketName, objectName);
            // 调用ossObject.getObjectContent获取文件输入流，可读取此输入流获取其内容。
            return ossObject.getObjectContent();
        } catch (OSSException e) {
            LOG.error("OSS download failed, exception", e);
            throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
        }
    }

    /**
     * 获取授权访问地址
     *
     * @param objectName Object完整路径
     * @return GET方法访问的签名URL
     */
    public String getFileUrl(String objectName) {
        // 设置签名URL过期时间为30天：30L * 24 * 3600秒（1小时）。
        Date expiration = new Date(new Date().getTime() + 30L * 24 * 3600 * 1000);
        // 生成以GET方法访问的签名URL，访客可以直接通过浏览器访问相关内容。
        URL fileUrl = ossClient.generatePresignedUrl(bucketName, objectName, expiration);
        return fileUrl.toString();
    }
}
