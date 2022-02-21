package xyz.eulix.platform.services.support.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Hex;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Singleton
public class OperationUtils {

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
   *  利用Apache的工具类实现SHA-256加密
   *  所需jar包下載 http://pan.baidu.com/s/1nuKxYGh
   * @param str 加密前的报文
   * @return
   */
  public String string2SHA256(String str){
    MessageDigest messageDigest;
    var encodeStr = "";
    try {
      messageDigest = MessageDigest.getInstance("SHA-256");
      byte[] hash = messageDigest.digest(str.getBytes(StandardCharsets.UTF_8));
      encodeStr = Hex.encodeHexString(hash);
    } catch (NoSuchAlgorithmException e) {
      Log.error("exception:"+e.toString());
    }
    return encodeStr;
  }
}
