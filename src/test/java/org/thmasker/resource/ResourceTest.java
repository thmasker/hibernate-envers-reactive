package org.thmasker.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ResourceTest {

    @Test
    void post() {
        RestAssured.given()
                .when().post("/resource")
                .then().statusCode(200);
    }

}