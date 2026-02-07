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

package xyz.eulix.platform.services.basic.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class BasicResourceTest {

    @Test
    void status_returnsOkAndVersion() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/status")
        .then()
            .statusCode(200)
            .body("status", equalTo("ok"))
            .body("version", notNullValue());
    }

    @Test
    void status_missingRequestId_fails() {
        given()
        .when()
            .get("/v2/platform/status")
        .then()
            .statusCode(400);
    }

    @Test
    void ability_returnsPlatformApis() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/ability")
        .then()
            .statusCode(200)
            .body("platformApis", notNullValue());
    }

    @Test
    void ability_containsExpectedApis() {
        given()
            .header("Request-Id", UUID.randomUUID().toString())
        .when()
            .get("/v2/platform/ability")
        .then()
            .statusCode(200)
            .body("platformApis", not(empty()));
    }

    @Test
    void ability_missingRequestId_fails() {
        given()
        .when()
            .get("/v2/platform/ability")
        .then()
            .statusCode(400);
    }

    @Test
    void status_multipleRequests_allSucceed() {
        for (int i = 0; i < 5; i++) {
            given()
                .header("Request-Id", UUID.randomUUID().toString())
            .when()
                .get("/v2/platform/status")
            .then()
                .statusCode(200)
                .body("status", equalTo("ok"));
        }
    }
}
