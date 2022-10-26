package xyz.eulix.platform.services.registry.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import java.time.OffsetDateTime;
import java.util.UUID;
import javax.inject.Inject;
import javax.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.registry.dto.registry.BoxRegistryInfo;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainUpdateResult;
import xyz.eulix.platform.services.registry.dto.registry.v2.BoxRegistryResultV2;
import xyz.eulix.platform.services.registry.dto.registry.v2.SubdomainGenInfoV2;
import xyz.eulix.platform.services.registry.dto.registry.v2.SubdomainGenResultV2;
import xyz.eulix.platform.services.registry.dto.registry.v2.UserRegistryInfoV2;
import xyz.eulix.platform.services.registry.dto.registry.v2.UserRegistryResultV2;
import xyz.eulix.platform.services.token.entity.BoxTokenEntity;
import xyz.eulix.platform.services.token.repository.BoxTokenEntityRepository;

@QuarkusTest
class RegistryResourceV2Test {

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
        given()
                .header("Request-Id", bid)
                .header("Box-Reg-Key", "box_reg_key")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/boxes")
                .body()
                .as(BoxRegistryResultV2.class);
    }

    @Test
    void registryDuplicated() {
        final String bid = UUID.randomUUID().toString();
        BoxRegistryInfo info = new BoxRegistryInfo();
        {
            info.setBoxUUID("box_uuid");
        }
        final BoxRegistryResultV2 result = given()
            .header("Request-Id", bid)
            .header("Box-Reg-Key", "box_reg_key")
            .body(info)
            .contentType(ContentType.JSON)
            .when().post("/v2/platform/boxes")
            .body()
            .as(BoxRegistryResultV2.class);

        final BoxRegistryResultV2 result1 = given()
                .header("Request-Id", bid + "-1")
                .header("Box-Reg-Key", "box_reg_key")
                .body(info)
                .contentType(ContentType.JSON)
                .when().post("/v2/platform/boxes")
                .body()
                .as(BoxRegistryResultV2.class);
        System.out.println(result1);
        System.out.println(result);
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
            .as(BoxRegistryResultV2.class);


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