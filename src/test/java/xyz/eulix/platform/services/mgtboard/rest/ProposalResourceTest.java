package xyz.eulix.platform.services.mgtboard.rest;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProposalResourceTest {

  @Test
  public void exportTest(){
    given()
        .header("Request-Id", "uuid")
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/api/proposal/export")
        .then()
        .statusCode(200);

  }
}
