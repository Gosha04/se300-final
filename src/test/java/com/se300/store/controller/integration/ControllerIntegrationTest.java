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
import io.restassured.http.ContentType;

/**
 * Integration tests for Store and User controllers using RestAssured.
 * These tests validate the complete REST API by making HTTP requests to the running server.
 */
@DisplayName("Controller Integration Tests - REST API")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ControllerIntegrationTest {

    //TODO: Implement Integration Tests for Smart Store Controllers

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
            .contentType(ContentType.URLENC)
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
    }


    @Test
    @Order(2)
    @DisplayName("Integration: Get all stores via REST API")
    public void testGetAllStores() {
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Get store by ID via REST API")
    public void testGetStoreById() {
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Update store via REST API")
    public void testUpdateStore() {
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Delete store via REST API")
    public void testDeleteStore() {
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Complete store CRUD workflow via REST API")
    public void testStoreCompleteWorkflow() {
    }

    // ==================== USER CONTROLLER TESTS ====================

    @Test
    @Order(7)
    @DisplayName("Integration: Register user via REST API")
    public void testRegisterUser() {
    }

    @Test
    @Order(8)
    @DisplayName("Integration: Get all users via REST API")
    public void testGetAllUsers() {
    }

    @Test
    @Order(9)
    @DisplayName("Integration: Get user by email via REST API")
    public void testGetUserByEmail() {
    }

    @Test
    @Order(10)
    @DisplayName("Integration: Update user via REST API")
    public void testUpdateUser() {
    }

    @Test
    @Order(11)
    @DisplayName("Integration: Delete user via REST API")
    public void testDeleteUser() {
    }

    @Test
    @Order(12)
    @DisplayName("Integration: Complete user CRUD workflow via REST API")
    public void testUserCompleteWorkflow() {
    }

    // ==================== ERROR HANDLING TESTS ====================

    @Test
    @Order(13)
    @DisplayName("Integration: Test error handling - Missing parameters")
    public void testErrorHandlingMissingParameters() {
    }

    @Test
    @Order(14)
    @DisplayName("Integration: Test error handling - User not found")
    public void testErrorHandlingUserNotFound() {
    }

    @Test
    @Order(15)
    @DisplayName("Integration: Test error handling - Duplicate user")
    public void testErrorHandlingDuplicateUser() {
    }
}