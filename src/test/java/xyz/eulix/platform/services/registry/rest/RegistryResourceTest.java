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
    final String subdomain = UUID.randomUUID().toString();
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
        .when().post("/v1/api/registry")
        .body()
        .as(RegistryResult.class);
    assertEquals(result.getUserDomain(), info.getSubdomain());

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
  }

  @Test
  void registryDuplicated() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = UUID.randomUUID().toString();
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
        .when().post("/v1/api/registry")
        .body()
        .as(RegistryResult.class);

    assertEquals(result.getUserDomain(), info.getSubdomain());

    given()
        .header("Request-Id", "uuid")
        .body(info)
        .contentType(ContentType.JSON)
        .when().post("/v1/api/registry")
        .then()
        .statusCode(406);

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
  }

  @Test
  void reset() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = UUID.randomUUID().toString();
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
  }

  @Test
  void verifyBox() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = UUID.randomUUID().toString();
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
        .queryParam("box_uuid", bid)
        .queryParam("box_reg_key", result.getBoxRegKey())
        .when().get("/v1/api/registry/verify/box")
        .then()
        .statusCode(200);

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
  }

  @Test
  void verifyClient() {
    final String bid = UUID.randomUUID().toString();
    final String cid = UUID.randomUUID().toString();
    final String subdomain = UUID.randomUUID().toString();
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
        .queryParam("box_uuid", bid)
        .queryParam("client_uuid", cid)
        .queryParam("client_reg_key", result.getClientRegKey())
        .when().get("/v1/api/registry/verify/client")
        .then()
        .statusCode(200);


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
  }
}