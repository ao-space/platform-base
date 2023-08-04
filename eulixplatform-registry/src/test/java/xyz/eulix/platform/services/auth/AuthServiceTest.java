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

package xyz.eulix.platform.services.auth;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.auth.dto.GenPkeyRsp;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class AuthServiceTest {

    @Test
    public void testUUID() {
        // 生成uuid
        String uuid = UUID.randomUUID().toString();
        Pattern pattern = Pattern.compile( "[a-zA-Z0-9-]{36}");
        Matcher matcher = pattern.matcher(uuid);
        Assertions.assertTrue(matcher.matches());
    }

    @Test
    public void genPkey() {
        final String bid = UUID.randomUUID().toString();
        final GenPkeyRsp result = given()
                .header("Request-Id", bid)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/pkeys")
                .body()
                .as(GenPkeyRsp.class);

        Assertions.assertNotNull(result);
    }

    @Test
    public void boxinfoTrans() {
        final String bid = UUID.randomUUID().toString();
        final GenPkeyRsp pkeyRsp = given()
                .header("Request-Id", bid)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/pkeys")
                .body()
                .as(GenPkeyRsp.class);
        Assertions.assertNotNull(pkeyRsp);

        String pkey = pkeyRsp.getPkey();
        TransBoxInfoReq info = new TransBoxInfoReq();
        {
            info.setBkey("b_key");
            info.setUserDomain("user_domain");
            info.setBoxPubKey("box_pub_key");
        }
        final String bid2 = UUID.randomUUID().toString();
        final PollPkeyRsp result = given()
                .header("Request-Id", bid2)
                .body(info)
                .contentType(ContentType.JSON)
                .when().post(String.format("/v2/platform/pkeys/%s/boxinfo", pkey))
                .as(PollPkeyRsp.class);
        Assertions.assertNotNull(result);
    }

    @Test
    public void pkeyPoll() {
        final String bid = UUID.randomUUID().toString();
        final GenPkeyRsp pkeyRsp = given()
                .header("Request-Id", bid)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/pkeys")
                .body()
                .as(GenPkeyRsp.class);
        Assertions.assertNotNull(pkeyRsp);

        String pkey = pkeyRsp.getPkey();
        final String bid2 = UUID.randomUUID().toString();
        final PollPkeyRsp result = given()
                .header("Request-Id", bid2)
                .contentType(ContentType.JSON)
                .when().get(String.format("/v2/platform/pkeys/%s/boxinfo", pkey))
                .as(PollPkeyRsp.class);
        Assertions.assertNotNull(result);
    }
}
