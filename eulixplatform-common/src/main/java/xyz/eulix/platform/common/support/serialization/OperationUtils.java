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

package xyz.eulix.platform.common.support.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.logging.Log;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;
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
     * 所需jar包下載 <a href="http://pan.baidu.com/s/1nuKxYGh">...</a>
     *
     * @param str 加密前的报文
     * @return 加密后的报文
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
                    .url(Objects.requireNonNull(HttpUrl.parse(urlString)).newBuilder().build());
            var response = httpClient.newCall(requestBuilder.build()).execute();
            assert response.body() != null;
            byte[] b = response.body().source().inputStream().readAllBytes();
            return Response.ok(b).header("Content-Disposition", response.header("Content-Disposition", "attachment;filename=" + fileName[fileName.length-1]))
                    .header("Content-Length", b.length).build();
        } catch (IOException e) {
            LOG.error("download template failed, exception is:", e);
            throw new ServiceOperationException(ServiceError.DOWNLOAD_FILE_FAILED);
        }
    }

    @SneakyThrows
    public String signUsingBoxPrivateKey(String data, String privateKey) {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(getRSAPrivateKey(privateKey));
        privateSignature.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    @SneakyThrows
    public boolean verifySignUsingBoxPublicKey(String data, String signature, String publicKey) {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initVerify(getRSAPublicKey(publicKey));
        sign.update(data.getBytes(StandardCharsets.UTF_8));
        return sign.verify(Base64.getDecoder().decode(signature));
    }

    /**
     * Read from pem text reader and returns a RSA private key based on PKCS#8 standard format.
     */
    public RSAPrivateKey getRSAPrivateKey(String privateKey)
        throws GeneralSecurityException {

        final String pem = privateKey.replaceAll("[\\n\\r]", "")
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(pem));
        return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
    }

    /**
     * Read from a pem text reader and returns a RSA public key based on X.509 standard format.
     */
    public RSAPublicKey getRSAPublicKey(String  publicKey) throws GeneralSecurityException {

        final String pem = publicKey.replaceAll("[\\n\\r]", "")
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "");

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(pem));
        return (RSAPublicKey) keyFactory.generatePublic(keySpec);
    }

    public String getStringFromFile(String resourceLocation) throws IOException {
        try(InputStream inputStream = Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(resourceLocation))){
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }
}
