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

package xyz.eulix.platform.services.registry.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryResult;
import xyz.eulix.platform.services.token.entity.BoxTokenEntity;
import xyz.eulix.platform.services.token.repository.BoxTokenEntityRepository;

@QuarkusTest
class RegistryResourceTest {

    @Inject
    BoxTokenEntityRepository boxTokenEntityRepository;
    @BeforeEach
    @Transactional
    void setUp() {
        var boxTokenEntity = new BoxTokenEntity();
        boxTokenEntity.setBoxUUID("box_uuid");
        boxTokenEntity.setServiceId("10001");
        boxTokenEntity.setServiceName("官方空间平台");
        boxTokenEntity.setBoxRegKey("box_reg_key");
        boxTokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(1));
        boxTokenEntityRepository.persist(boxTokenEntity);
    }

    @AfterEach
    @Transactional
    void tearDown() {
        boxTokenEntityRepository.delete("box_uuid", "box_uuid");
    }

    @Test
    void registryBox() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID("box_uuid");
        }
        final BoxRegistryResult result = given()
                .header("Request-Id", bid)
                .header("Box-Reg-Key", "box_reg_key")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/boxes")
                .body()
                .as(BoxRegistryResult.class);
        Assertions.assertNotNull(result);

        given()
                .header("Request-Id", "uuid")
                .header("Box-Reg-Key", "box_reg_key")
                .pathParam(  "box_uuid","box_uuid")
                .contentType(ContentType.JSON)
                .when().delete("/v2/platform/boxes/{box_uuid}")
                .then()
                .statusCode(204);
    }

    @Test
    void registryDuplicated() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID("box_uuid");
        }
        final BoxRegistryResult result = given()
            .header("Request-Id", bid)
            .header("Box-Reg-Key", "box_reg_key")
            .body(info)
            .contentType(ContentType.JSON)
            .when().post("/v2/platform/boxes")
            .body()
            .as(BoxRegistryResult.class);

        final int statusCode = given()
                .header("Request-Id", bid + "-1")
                .header("Box-Reg-Key", "box_reg_key")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/boxes")
                .getStatusCode();
        Assertions.assertNotNull(result);
        Assertions.assertEquals(406, statusCode);

        given()
                .header("Request-Id", "uuid")
                .header("Box-Reg-Key", "box_reg_key")
                .pathParam(  "box_uuid","box_uuid")
                .contentType(ContentType.JSON)
                .when().delete("/v2/platform/boxes/{box_uuid}")
                .then()
                .statusCode(204);
    }

    @Test
    void resetBox() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID("box_uuid");
        }
        given()
            .header("Request-Id", bid)
            .header("Box-Reg-Key", "box_reg_key")
            .body(info)
            .contentType(ContentType.JSON)
            .when().post("/v2/platform/boxes")
            .body()
            .as(BoxRegistryResult.class);


        given()
                .header("Request-Id", "uuid")
                .header("Box-Reg-Key", "box_reg_key")
                .pathParam(  "box_uuid","box_uuid")
                .contentType(ContentType.JSON)
                .when().delete("/v2/platform/boxes/{box_uuid}")
                .then()
                .statusCode(204);
    }
}