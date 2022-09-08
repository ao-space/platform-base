package xyz.eulix.platform.common.support.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import lombok.SneakyThrows;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.codec.binary.Hex;
import org.jboss.logging.Logger;

import xyz.eulix.platform.common.support.service.ServiceError;
import xyz.eulix.platform.common.support.service.ServiceOperationException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Singleton
public class OperationUtils {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ObjectMapper objectMapper;

    @SneakyThrows
    public String objectToJson(Object object) {
        return objectMapper.writeValueAsString(object);
    }

    @SneakyThrows
    public <T> T jsonToObject(String json, Class<T> clz) {
        return objectMapper.readValue(json, clz);
    }

    /**
     * 利用Apache的工具类实现SHA-256加密
     * 所需jar包下載 http://pan.baidu.com/s/1nuKxYGh
     *
     * @param str 加密前的报文
     * @return
     */
    public String string2SHA256(String str) {
        MessageDigest messageDigest;
        var encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(str.getBytes(StandardCharsets.UTF_8));
            encodeStr = Hex.encodeHexString(hash);
        } catch (NoSuchAlgorithmException e) {
            Log.error("string2SHA256 throw error, exception:", e);
        }
        return encodeStr;
    }

    public Response downLoadFile(String urlString){
        String[] fileName = urlString.split("/");
        try {
            OkHttpClient httpClient = new OkHttpClient();
            var requestBuilder = new Request.Builder()
                    .url(HttpUrl.parse(urlString).newBuilder().build());
            var response = httpClient.newCall(requestBuilder.build()).execute();
            byte b[] = response.body().source().inputStream().readAllBytes();
            return Response.ok(b).header("Content-Disposition", response.header("Content-Disposition", "attachment;filename=" + fileName[fileName.length-1]))
                    .header("Content-Length", b.length).build();
        } catch (IOException e) {
            LOG.error("download template failed, exception is:", e);
            throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
        }
    }
}
