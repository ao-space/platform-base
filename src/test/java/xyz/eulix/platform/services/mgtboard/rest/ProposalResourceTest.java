package xyz.eulix.platform.services.mgtboard.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.keycloak.client.KeycloakTestClient;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import xyz.eulix.platform.services.mgtboard.dto.ProposalReq;

@QuarkusTest
class ProposalResourceTest {

  protected String getAccessToken() {

    RestAssured.baseURI = "https://dev-sso.eulix.xyz/auth/realms/eulix-oss-test/protocol/openid-connect/token";
    RestAssured.port = 443;
    KeycloakTestClient keycloakClient = new KeycloakTestClient();

    String accessToken = keycloakClient.getAccessToken("admin");
    RestAssured.baseURI = RestAssured.DEFAULT_URI;
    RestAssured.port = 8081;
    return accessToken;
  }

  @Test
  void exportTest(){
    given().auth().oauth2(getAccessToken())
        .header("Request-Id", "uuid")
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/api/proposals/export")
        .then()
        .statusCode(200);

  }

  @Test
  void testProposalSaveRequestIdAbsentFailedOk() {
    given().auth().oauth2(getAccessToken())
        .contentType(ContentType.JSON)
        .body(ProposalReq.of())
        .when()
        .post("/v1/api/proposal")
        .then()
        .statusCode(400)
        .body(containsString("\"error\""));
  }
}
