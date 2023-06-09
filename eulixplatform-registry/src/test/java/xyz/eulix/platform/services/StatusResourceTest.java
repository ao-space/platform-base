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

package xyz.eulix.platform.services;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.config.ApplicationProperties;

import javax.inject.Inject;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class StatusResourceTest {

  @Inject
  ApplicationProperties properties;

  @Test
  void testStatusEndpoint() {
    final var requestId = UUID.randomUUID().toString();

    given()
        .header("Request-Id", requestId)
        .when()
        .get("/v2/platform/status")
        .then()
        .statusCode(200)
        .body("status", is("ok"))
        .body("version", is(properties.getVersion()));
  }
}