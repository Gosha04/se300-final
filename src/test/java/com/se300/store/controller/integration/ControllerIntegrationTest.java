package com.se300.store.controller.integration;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.se300.store.SmartStoreApplication;
import com.se300.store.data.DataManager;

import io.restassured.RestAssured;

/**
 * Integration tests for Store and User controllers using RestAssured.
 * These tests validate the complete REST API by making HTTP requests to the running server.
 */

@DisplayName("Controller Integration Tests - REST API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerIntegrationTest {

    //COMPLETE: Implement Integration Tests for Smart Store Controllers

    private static SmartStoreApplication application;
    private static final int TEST_PORT = 8080;
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    @BeforeAll
    public static void setUpClass() throws Exception {
        // Clear any existing data
        DataManager.getInstance().clear();

        // Start the embedded Tomcat server
        application = new SmartStoreApplication();
        application.startNonBlocking();

        // Configure RestAssured
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = TEST_PORT;

        // Wait for server to be ready
        Thread.sleep(2000);
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        // Stop the server after all tests
        if (application != null) {
            application.stop();
        }
    }

    @BeforeEach
    public void setUp() {
        // Clear data between tests
        DataManager.getInstance().clear();
    }

    // ==================== STORE CONTROLLER TESTS ====================

    @Test
    @Order(1)
    @DisplayName("Integration: Create store via REST API") 
    public void testCreateStore() {
        given()
            .param("token", "admin")
            .param("storeId", "1")
            .param("name", "testName")
            .param("address", "testAddress")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(201)
            .body("id", equalTo("1"))
            .body("description", equalTo("testName"))
            .body("address", equalTo("testAddress"));

        given()
            .param("token", "admin")
            .param("storeId", "1")
            .param("name", "testName")
            .param("address", "testAddress")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400);
    }
    
    @Test
    @Order(2)
    @DisplayName("Integration: Get all stores via REST API")
    public void testGetAllStores() {
        given()
            .param("token", "admin")
        .when()
            .get("/api/v1/stores")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Get store by ID via REST API")
    public void testGetStoreById() {
        given()
            .param("token", "admin")
        .when()
            .get("/api/v1/stores/1")
        .then()
            .statusCode(200)
            .body("id", equalTo("1"))
            .body("description", equalTo("testName"))
            .body("address", equalTo("testAddress"));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Update store via REST API")
    public void testUpdateStore() {
        given()
            .param("token", "admin")
            .param("description", "updateName")
            .param("address", "updateAddress")
        .when()
            .put("/api/v1/stores/1")
        .then()
            .statusCode(200)
            .body("id", equalTo("1"))
            .body("description", equalTo("updateName"))
            .body("address", equalTo("updateAddress"));

        given()
            .param("token", "admin")
            .param("description", "updateName")
            .param("address", "updateAddress")
        .when()
            .put("/api/v1/stores")
        .then()
            .statusCode(400);

        given()
            .param("token", "admin")
        .when()
            .put("/api/v1/stores/1")
        .then()
            .statusCode(400);

        given()
            .param("token", "admin")
            .param("description", "updateName")
            .param("address", "updateAddress")
        .when()
            .put("/api/v1/stores/12")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Delete store via REST API")
    public void testDeleteStore() {
        given()
            .param("token", "admin")
        .when()
            .delete("/api/v1/stores/1")
        .then()
            .statusCode(204);

        given()
            .param("token", "admin")
        .when()
            .delete("/api/v1/stores")
        .then()
            .statusCode(400);

        given()
            .param("token", "admin")
        .when()
            .delete("/api/v1/stores/12")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Complete store CRUD workflow via REST API")
    public void testStoreCompleteWorkflow() {
        given()
            .param("token", "admin")
            .param("storeId", "1")
            .param("name", "testName")
            .param("address", "testAddress")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(201)
            .body("id", equalTo("1"))
            .body("description", equalTo("testName"))
            .body("address", equalTo("testAddress"));

        given()
            .param("token", "admin")
        .when()
            .get("/api/v1/stores/1")
        .then()
            .statusCode(200);

        given()
            .param("token", "admin")
        .when()
            .get("/api/v1/stores")
        .then()
            .statusCode(200);

        given()
            .param("token", "admin")
            .param("description", "updateName")
            .param("address", "updateAddress")
        .when()
            .put("/api/v1/stores/1")
        .then()
            .statusCode(200)
            .body("id", equalTo("1"))
            .body("description", equalTo("updateName"))
            .body("address", equalTo("updateAddress"));


        given()
            .param("token", "admin")
        .when()
            .delete("/api/v1/stores/1")
        .then()
            .statusCode(204);
    }

    // ==================== USER CONTROLLER TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Integration: Register user via REST API")
    public void testRegisterUser() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201)
            .body("email", equalTo("testMail"))
            .body("password", equalTo("password"))
            .body("name", equalTo("testName"));
    }

    @Test
    @Order(8)
    @DisplayName("Integration: Get all users via REST API")
    public void testGetAllUsers() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(200);
    }

    @Test
    @Order(9)
    @DisplayName("Integration: Get user by email via REST API")
    public void testGetUserByEmail() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
        .when()
            .get("/api/v1/users/testMail")
        .then()
            .statusCode(200)
            .body("email", equalTo("testMail"))
            .body("password", equalTo("password"))
            .body("name", equalTo("testName"));
    }

    @Test
    @Order(10)
    @DisplayName("Integration: Update user via REST API")
    public void testUpdateUser() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
            .param("password", "test")
            .param("name", "test")
        .when()
            .put("/api/v1/users")
        .then()
            .statusCode(400);

        given()
            .param("password", "test")
        .when()
            .put("/api/v1/users/testMail")
        .then()
            .statusCode(400);

        given()
            .param("password", "test")
            .param("name", "test")
        .when()
            .put("/api/v1/users/noExist")
        .then()
            .statusCode(404);

        given()
            .param("password", "test")
            .param("name", "test")
        .when()
            .put("/api/v1/users/testMail")
        .then()
            .statusCode(200)
            .body("email", equalTo("testMail"))
            .body("password", equalTo("test"))
            .body("name", equalTo("test"));
    }

    @Test
    @Order(11)
    @DisplayName("Integration: Delete user via REST API")
    public void testDeleteUser() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
        .when()
            .delete("/api/v1/users")
        .then()
            .statusCode(400);

        given()
        .when()
            .delete("/api/v1/users/testMail")
        .then()
            .statusCode(204);
    }

    @Test
    @Order(12)
    @DisplayName("Integration: Complete user CRUD workflow via REST API")
    public void testUserCompleteWorkflow() {
        given()
            .param("email", "testMail")
            .param("password", "password")
            .param("name", "testName")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
        .when()
            .get("/api/v1/users/testMail")
        .then()
            .statusCode(200)
            .body("email", equalTo("testMail"))
            .body("password", equalTo("password"))
            .body("name", equalTo("testName"));

        given()
            .param("password", "updatedPass")
            .param("name", "updatedName")
        .when()
            .put("/api/v1/users/testMail")
        .then()
            .statusCode(200)
            .body("email", equalTo("testMail"))
            .body("password", equalTo("updatedPass"))
            .body("name", equalTo("updatedName"));

        given()
        .when()
            .delete("/api/v1/users/testMail")
        .then()
            .statusCode(204);
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @Order(13)
    @DisplayName("Integration: Test error handling - Missing parameters")
    public void testErrorHandlingMissingParameters() {
        given()
            .param("storeId", "1")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(400);

        given()
            .param("email", "badMail")
            .param("name", "Bad User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(400);
    }

    @Test
    @Order(14)
    @DisplayName("Integration: Test error handling - User not found")
    public void testErrorHandlingUserNotFound() {
        given()
        .when()
            .get("/api/v1/users/nonexistent@example.com")
        .then()
            .statusCode(404);
    }

    @Test
    @Order(15)
    @DisplayName("Integration: Test error handling - Duplicate user")
    public void testErrorHandlingDuplicateUser() {
        given()
            .param("email", "dup@example.com")
            .param("password", "password")
            .param("name", "Dup User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(201);

        given()
            .param("email", "dup@example.com")
            .param("password", "password")
            .param("name", "Dup User")
        .when()
            .post("/api/v1/users")
        .then()
            .statusCode(400); 
    }
}