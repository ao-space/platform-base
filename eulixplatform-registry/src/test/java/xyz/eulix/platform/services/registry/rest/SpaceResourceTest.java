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
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.registry.dto.registry.SpaceRegistryInfo;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SpaceResourceTest {

    @Test
    void registerSpace_success() {
        String boxUUID = UUID.randomUUID().toString();
        String userId = "test-user-" + System.currentTimeMillis();
        String clientUUID = UUID.randomUUID().toString();

        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setBoxUUID(boxUUID);
        info.setUserId(userId);
        info.setClientUUID(clientUUID);

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200)
            .body("boxUUID", equalTo(boxUUID))
            .body("userId", equalTo(userId))
            .body("clientUUID", equalTo(clientUUID))
            .body("subdomain", notNullValue())
            .body("userDomain", notNullValue())
            .body("userType", equalTo("user_admin"))
            .body("networkClient.clientId", notNullValue())
            .body("networkClient.secretKey", notNullValue());
    }

    @Test
    void registerSpace_withSubdomain() {
        String boxUUID = UUID.randomUUID().toString();
        String userId = "test-user-" + System.currentTimeMillis();
        String clientUUID = UUID.randomUUID().toString();
        String subdomain = "test" + System.currentTimeMillis() % 1000000;

        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setBoxUUID(boxUUID);
        info.setUserId(userId);
        info.setClientUUID(clientUUID);
        info.setSubdomain(subdomain);

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200)
            .body("subdomain", equalTo(subdomain));
    }

    @Test
    void registerSpace_duplicateBox_reuses() {
        String boxUUID = UUID.randomUUID().toString();
        String userId1 = "user1-" + System.currentTimeMillis();
        String userId2 = "user2-" + System.currentTimeMillis();
        String clientUUID1 = UUID.randomUUID().toString();
        String clientUUID2 = UUID.randomUUID().toString();

        // First registration
        SpaceRegistryInfo info1 = new SpaceRegistryInfo();
        info1.setBoxUUID(boxUUID);
        info1.setUserId(userId1);
        info1.setClientUUID(clientUUID1);

        String networkClientId = given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info1)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200)
            .extract().path("networkClient.clientId");

        // Second registration with same box
        SpaceRegistryInfo info2 = new SpaceRegistryInfo();
        info2.setBoxUUID(boxUUID);
        info2.setUserId(userId2);
        info2.setClientUUID(clientUUID2);

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info2)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200)
            .body("networkClient.clientId", equalTo(networkClientId)); // Same network client
    }

    @Test
    void registerSpace_missingBoxUUID_fails() {
        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setUserId("user");
        info.setClientUUID(UUID.randomUUID().toString());

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(400);
    }

    @Test
    void deleteSpace_success() {
        // First register
        String boxUUID = UUID.randomUUID().toString();
        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setBoxUUID(boxUUID);
        info.setUserId("user");
        info.setClientUUID(UUID.randomUUID().toString());

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200);

        // Then delete
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .delete("/v2/platform/spaces/" + boxUUID)
        .then()
            .statusCode(204);
    }

    @Test
    void deleteSpace_notFound() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .delete("/v2/platform/spaces/" + UUID.randomUUID())
        .then()
            .statusCode(404);
    }

    @Test
    void getSpace_success() {
        // First register
        String boxUUID = UUID.randomUUID().toString();
        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setBoxUUID(boxUUID);
        info.setUserId("user");
        info.setClientUUID(UUID.randomUUID().toString());

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200);

        // Then get
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/spaces/" + boxUUID)
        .then()
            .statusCode(200)
            .body("networkClientId", notNullValue());
    }
}
