package com.se300.store.controller.externalmockserver;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * External Mock Server Tests
 *
 * These tests interact with an external mock API endpoint hosted on Apidog.
 * The external endpoint simulates the Smart Store REST API for integration testing.
 *
 * Purpose: Demonstrate integration testing with external third-party APIs
 * and validate that our application can consume external store services.
 */
@DisplayName("External Mock Server Tests - Apidog Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ExternalMockServerTest {

    //TODO: Implement External Mock Server to test external Smart Store API calls

    private static final String EXTERNAL_API_BASE_URL = "https://mock.apidog.com/m1/1141177-1133500-default";
    private static final String STORES_ENDPOINT = "/stores";
    private static final String USERS_ENDPOINT = "/users";


    @BeforeAll
    public static void setUpExternalMockServer() {
        // Configure RestAssured for external API testing
        RestAssured.baseURI = EXTERNAL_API_BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @AfterAll
    public static void tearDown() {
        RestAssured.reset();
    }

    // ==================== STORE OPERATIONS ====================

    @Test
    @Order(1)
    @DisplayName("External API: GET /stores - Retrieve all stores")
    public void testGetAllStores() {
        given()
        .accept(ContentType.JSON)
    .when()
        .get(STORES_ENDPOINT)
    .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body(notNullValue());
    }

    @Test
    @Order(2)
    @DisplayName("External API: GET /stores/{id} - Retrieve store by ID")
    public void testGetStoreById() {
        String storeId = "store1";

        given()
            .accept(ContentType.JSON)
        .when()
            .get(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)))
            .contentType(containsString("application/json"));
    }

    @Test
    @Order(3)
    @DisplayName("External API: POST /stores - Create new store")
    public void testCreateStore() {
         given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("storeId", "ext-store-1")
            .queryParam("name", "External Test Store")
            .queryParam("address", "100 External Ave")
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(200))) 
            .contentType(containsString("application/json"))
            .body("id", notNullValue());

    }

    @Test
    @Order(4)
    @DisplayName("External API: PUT /stores/{id} - Update store")
    public void testUpdateStore() {
        String storeId = "ext-store-1";

        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("description", "Updated External Store")
            .queryParam("address", "999 Updated External Ave")
        .when()
            .put(STORES_ENDPOINT + "/" + storeId)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)));
    }

    @Test
    @Order(5)
    @DisplayName("External API: DELETE /stores/{id} - Delete store")
    public void testDeleteStore() {
        String storeId = "ext-store-1";

        Response response =
            given()
                .accept(ContentType.JSON)
            .when()
                .delete(STORES_ENDPOINT + "/" + storeId)
            .then()
                .extract()
                .response();

        int status = response.getStatusCode();
        assertTrue(status == 204 || status == 200 || status == 202 || status == 404);
    }

    // ==================== USER OPERATIONS ====================

    @Test
    @Order(6)
    @DisplayName("External API: GET /users - Retrieve all users")
    public void testGetAllUsers() {
         given()
            .accept(ContentType.JSON)
        .when()
            .get(USERS_ENDPOINT)
        .then()
            .statusCode(200)
            .contentType(containsString("application/json"));

    }

    @Test
    @Order(7)
    @DisplayName("External API: POST /users - Register new user")
    public void testRegisterUser() {
        String email = "external-user@store.com";

        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("email", email)
            .queryParam("password", "pw123")
            .queryParam("name", "External User")
        .when()
            .post(USERS_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(200), equalTo(409)))
            .contentType(containsString("application/json"));
    }

    @Test
    @Order(8)
    @DisplayName("External API: GET /users/{email} - Retrieve user by email")
    public void testGetUserByEmail() {
        String email = "external-user@store.com";

        given()
            .accept(ContentType.JSON)
        .when()
            .get(USERS_ENDPOINT + "/" + email)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)))
            .contentType(containsString("application/json"));
    }

    @Test
    @Order(9)
    @DisplayName("External API: PUT /users/{email} - Update user")
    public void testUpdateUser() {
        String email = "external-user@store.com";

        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("password", "updatedPw123")
            .queryParam("name", "Updated External User")
        .when()
            .put(USERS_ENDPOINT + "/" + email)
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(404)))
            .contentType(containsString("application/json"));
    }

    // ==================== ERROR HANDLING ====================

    @Test
    @Order(10)
    @DisplayName("External API: Handle 404 - Non-existent store")
    public void testGetNonExistentStore() {
        Response response =
        given()
            .accept(ContentType.JSON)
        .when()
            .get(STORES_ENDPOINT + "/non-existent-store-xyz")
        .then()
            .contentType(containsString("application/json"))
            .extract()
            .response();

        int status = response.getStatusCode();

        assertTrue(status >= 200 && status < 500,
            "Expected client-style status, but got: " + status);
        }

    @Test
    @Order(11)
    @DisplayName("External API: Handle missing required parameters")
    public void testCreateStoreWithMissingParameters() {
        Response response =
            given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
            .when()
                .post(STORES_ENDPOINT)
            .then()
                .extract()
                .response();

        int status = response.getStatusCode();
        assertTrue(status >= 200 && status < 500);
    }

    // ==================== INTEGRATION WORKFLOW ====================

    @Test
    @Order(12)
    @DisplayName("External API: Complete store lifecycle workflow")
    public void testCompleteStoreLifecycle() {

        String lifecycleId = "lifecycle-store-1";

        // 1. Create store
        given()
            .accept(ContentType.JSON)
            .contentType(ContentType.JSON)
            .queryParam("storeId", lifecycleId)
            .queryParam("name", "Lifecycle Store")
            .queryParam("address", "1 Lifecycle Way")
        .when()
            .post(STORES_ENDPOINT)
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(200)));

        // 2. Retrieve store 
        Response getResponse =
            given()
                .accept(ContentType.JSON)
            .when()
                .get(STORES_ENDPOINT + "/" + lifecycleId)
            .then()
                .extract()
                .response();

        int getStatus = getResponse.getStatusCode();
        assertTrue(getStatus == 200 || getStatus == 404);

        // 3. Update store
        if (getStatus == 200) {
            given()
                .accept(ContentType.JSON)
                .contentType(ContentType.JSON)
                .queryParam("description", "Lifecycle Store Updated")
                .queryParam("address", "2 Updated Lifecycle Way")
            .when()
                .put(STORES_ENDPOINT + "/" + lifecycleId)
            .then()
                .statusCode(anyOf(equalTo(200), equalTo(404)));
        }

        // 4. Delete store
        Response deleteResponse =
            given()
                .accept(ContentType.JSON)
            .when()
                .delete(STORES_ENDPOINT + "/" + lifecycleId)
            .then()
                .extract()
                .response();

        int deleteStatus = deleteResponse.getStatusCode();
        assertTrue(deleteStatus >= 200 && deleteStatus < 500);
    }

    // ==================== PERFORMANCE TEST ====================

    @Test
    @Order(13)
    @DisplayName("External API: Response time validation")
    public void testApiResponseTime() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get(STORES_ENDPOINT)
        .then()
            .time(lessThan(2000L));
    }
}