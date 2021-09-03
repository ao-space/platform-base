package xyz.eulix.platform.services.registry.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.registry.dto.registry.RegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.RegistryResetInfo;
import xyz.eulix.platform.services.registry.dto.registry.RegistryResult;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class RegistryResourceTest {

  @Test
  void registry() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = "hello";
    RegistryInfo info = new RegistryInfo();
    {
      info.setBoxUUID(bid);
      info.setSubdomain(subdomain);
      info.setClientUUID(cid);
    }
    given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry")
        .then()
        .statusCode(200)
        .body(containsString("hello"));
  }

  @Test
  void registryDuplicated() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = "hello";
    RegistryInfo info = new RegistryInfo();
    {
      info.setBoxUUID(bid);
      info.setSubdomain(subdomain);
      info.setClientUUID(cid);
    }
    given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry")
        .then()
        .statusCode(200)
        .body(containsString("hello"));

    given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry")
        .then()
        .statusCode(406);
  }

  @Test
  void reset() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = "hello";
    RegistryInfo info = new RegistryInfo();
    {
      info.setBoxUUID(bid);
      info.setSubdomain(subdomain);
      info.setClientUUID(cid);
    }

    final RegistryResult result = given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry").body().as(RegistryResult.class);

    RegistryResetInfo reset = new RegistryResetInfo();
    {
      reset.setBoxUUID(bid);
      reset.setBoxRegKey(result.getBoxRegKey());
    }

    given()
        .header("Request-Id", "uuid")
        .body(reset)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry/reset")
        .then()
        .statusCode(200)
        .body(containsString(bid));

    given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry")
        .then()
        .statusCode(200)
        .body(containsString("hello"));
  }

  @Test
  void verifyBox() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = "hello";
    RegistryInfo info = new RegistryInfo();
    {
      info.setBoxUUID(bid);
      info.setSubdomain(subdomain);
      info.setClientUUID(cid);
    }

    final RegistryResult result = given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry").body().as(RegistryResult.class);

    given()
        .header("Request-Id", "uuid")
        .queryParam("uuid", bid)
        .queryParam("key", result.getBoxRegKey())
        .when().get("/v1/api/registry/verify/box")
        .then()
        .statusCode(200);
  }

  @Test
  void verifyClient() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = "hello";
    RegistryInfo info = new RegistryInfo();
    {
      info.setBoxUUID(bid);
      info.setSubdomain(subdomain);
      info.setClientUUID(cid);
    }

    final RegistryResult result = given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry").body().as(RegistryResult.class);

    given()
        .header("Request-Id", "uuid")
        .queryParam("uuid", cid)
        .queryParam("key", result.getClientRegKey())
        .when().get("/v1/api/registry/verify/client")
        .then()
        .statusCode(200);
  }
}