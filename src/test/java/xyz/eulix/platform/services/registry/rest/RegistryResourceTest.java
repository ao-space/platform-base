package xyz.eulix.platform.services.registry.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
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
                .when().post("/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);
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
                .when().post("/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);

        given()
                .header("Request-Id", "uuid")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/v1/api/registry/box")
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
                .when().post("/v1/api/registry/box")
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
                .when().post("/v1/api/registry/reset/box")
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
                .when().post("/v1/api/registry/box")
                .body()
                .as(BoxRegistryResult.class);

        given()
                .header("Request-Id", "uuid")
                .queryParam("box_uuid", bid)
                .queryParam("box_reg_key", result.getBoxRegKey())
                .when().get("/v1/api/registry/verify/box")
                .then()
                .statusCode(200);
    }
}