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

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryResetInfo;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryResult;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class RegistryResourceTest {

    @Test
    void registry() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID(bid);
        }
        final BoxRegistryResult result = given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);
        Assertions.assertNotNull(result);
    }

    @Test
    void registryDuplicated() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID(bid);
        }
        final BoxRegistryResult result = given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);

        given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/box")
                .then()
                .statusCode(406);
    }

    @Test
    void reset() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID(bid);
        }
        final BoxRegistryResult result = given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);

        BoxRegistryResetInfo reset = new BoxRegistryResetInfo();
        {
            reset.setBoxUUID(bid);
            reset.setBoxRegKey(result.getBoxRegKey());
        }

        given()
                .header("Request-Id", "uuid")
                .body(reset)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/reset/box")
                .then()
                .statusCode(200)
                .body(containsString(bid));
    }

    @Test
    void verifyBox() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID(bid);
        }
        final BoxRegistryResult result = given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/platform/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);

        given()
                .header("Request-Id", "uuid")
                .queryParam("box_uuid", bid)
                .queryParam("box_reg_key", result.getBoxRegKey())
                .when().get("/platform/v1/api/registry/verify/box")
                .then()
                .statusCode(200);
    }
}