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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.auth.dto.GenPkeyRsp;
import xyz.eulix.platform.services.auth.dto.PollPkeyRsp;
import xyz.eulix.platform.services.auth.dto.TransBoxInfoReq;
import xyz.eulix.platform.services.auth.repository.PkeyAuthEntityRepository;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.restassured.RestAssured.given;
import static xyz.eulix.platform.common.support.service.ServiceError.PKEY_INVALID;

@QuarkusTest
public class AuthServiceTest {

    @Inject
    PkeyAuthEntityRepository pkeyAuthEntityRepository;

    @AfterEach
    @Transactional
    void tearDown() {
        pkeyAuthEntityRepository.delete("bkey", "b_key");
    }

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
        final PollPkeyRsp result = given()
                .header("Request-Id", bid)
                .body(info)
                .contentType(ContentType.JSON)
                .when().post(String.format("/v2/platform/pkeys/%s/boxinfo", pkey))
                .body()
                .as(PollPkeyRsp.class);
        Assertions.assertNotNull(result);

        final PollPkeyInvalidRsp invalidResult = given()
                .header("Request-Id", bid)
                .body(info)
                .contentType(ContentType.JSON)
                .when().post(String.format("/v2/platform/pkeys/%s/boxinfo", "4d27e0de-76b6-4f6b-871e-33c30b62ee48"))
                .body()
                .as(PollPkeyInvalidRsp.class);
        Assertions.assertNotNull(invalidResult);
        Assertions.assertEquals(String.format("SSP-%d", PKEY_INVALID.getCode()), invalidResult.getCode());
        Assertions.assertEquals(PKEY_INVALID.getMessage(), invalidResult.getMessage());
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
        final PollPkeyRsp result = given()
                .header("Request-Id", bid)
                .contentType(ContentType.JSON)
                .when().get(String.format("/v2/platform/pkeys/%s/boxinfo", pkey))
                .as(PollPkeyRsp.class);
        Assertions.assertNotNull(result);

        final PollPkeyInvalidRsp invalidResult = given()
                .header("Request-Id", bid)
                .contentType(ContentType.JSON)
                .when().get(String.format("/v2/platform/pkeys/%s/boxinfo", "4d27e0de-76b6-4f6b-871e-33c30b62ee48"))
                .body()
                .as(PollPkeyInvalidRsp.class);
        Assertions.assertNotNull(invalidResult);
        Assertions.assertEquals(String.format("SSP-%d", PKEY_INVALID.getCode()), invalidResult.getCode());
        Assertions.assertEquals(PKEY_INVALID.getMessage(), invalidResult.getMessage());
    }
}