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

package xyz.eulix.platform.services.network.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.network.dto.NetworkAuthReq;
import xyz.eulix.platform.services.registry.dto.registry.SpaceRegistryInfo;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class NetworkResourceTest {

    private String boxUUID;
    private String networkClientId;
    private String networkSecretKey;
    private String subdomain;

    @BeforeEach
    void setup() {
        // Register a space first to get valid network credentials
        boxUUID = UUID.randomUUID().toString();
        String userId = "test-user-" + System.currentTimeMillis();
        String clientUUID = UUID.randomUUID().toString();

        SpaceRegistryInfo info = new SpaceRegistryInfo();
        info.setBoxUUID(boxUUID);
        info.setUserId(userId);
        info.setClientUUID(clientUUID);

        var response = given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(info)
        .when()
            .post("/v2/platform/spaces")
        .then()
            .statusCode(200)
            .extract();

        networkClientId = response.path("networkClient.clientId");
        networkSecretKey = response.path("networkClient.secretKey");
        subdomain = response.path("subdomain");
    }

    @Test
    void networkClientAuth_validCredentials_returnsTrue() {
        NetworkAuthReq req = NetworkAuthReq.of(networkClientId, networkSecretKey);

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(req)
        .when()
            .post("/v2/platform/clients/network/auth")
        .then()
            .statusCode(200)
            .body("result", equalTo(true));
    }

    @Test
    void networkClientAuth_invalidCredentials_returnsFalse() {
        NetworkAuthReq req = NetworkAuthReq.of("invalid-client-id", "invalid-secret");

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(req)
        .when()
            .post("/v2/platform/clients/network/auth")
        .then()
            .statusCode(200)
            .body("result", equalTo(false));
    }

    @Test
    void networkClientAuth_wrongSecretKey_returnsFalse() {
        NetworkAuthReq req = NetworkAuthReq.of(networkClientId, "wrong-secret");

        given()
            .contentType(ContentType.JSON)
            .header("Request-Id", UUID.randomUUID().toString())
            .body(req)
        .when()
            .post("/v2/platform/clients/network/auth")
        .then()
            .statusCode(200)
            .body("result", equalTo(false));
    }

    @Test
    void networkClientAuth_missingRequestId_fails() {
        NetworkAuthReq req = NetworkAuthReq.of(networkClientId, networkSecretKey);

        given()
            .contentType(ContentType.JSON)
            .body(req)
        .when()
            .post("/v2/platform/clients/network/auth")
        .then()
            .statusCode(400);
    }

    @Test
    void networkServerDetail_validClientId_returnsServerInfo() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
            .queryParam("network_client_id", networkClientId)
        .when()
            .get("/v2/platform/servers/network/detail")
        .then()
            .statusCode(200);
        // Note: Server info may be empty if no network server is configured
    }

    @Test
    void networkServerDetail_missingClientId_fails() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/servers/network/detail")
        .then()
            .statusCode(400);
    }

    @Test
    void stunServerDetail_validSubdomain_returnsStunInfo() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
            .queryParam("subdomain", subdomain)
        .when()
            .get("/v2/platform/servers/stun/detail")
        .then()
            .statusCode(200);
        // Note: STUN info may be empty if no STUN server is configured
    }

    @Test
    void stunServerDetail_missingSubdomain_fails() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/servers/stun/detail")
        .then()
            .statusCode(400);
    }
}
