package xyz.eulix.platform.services;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

/**
 * Ref: https://quarkus.io/guides/getting-started-testing
 */
@QuarkusTest
public class GreetingResourceTest {

  @Test
  public void testHelloEndpoint() {
    given()
        .when().get("/hello-resteasy")
        .then()
        .statusCode(200)
        .body(is("Hello RESTEasy"));
  }

}