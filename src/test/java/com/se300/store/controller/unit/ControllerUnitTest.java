package com.se300.store.controller.unit;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for Store and User controllers using Mockito and RestAssured.
 * These tests mock the service layer to test controller logic in isolation.
 */
@DisplayName("Controller Mock Tests - Unit Testing with Mockito")
@ExtendWith(MockitoExtension.class)
public class ControllerUnitTest {

    //TODO: Implement Unit Tests for Smart Store Controllers

    @Mock
    private StoreService storeService;

    @Mock
    private AuthenticationService authenticationService;

    private static Tomcat tomcat;
    private static final int TEST_PORT = 8081; // Different port from integration tests
    private static final String BASE_URL = "http://localhost:" + TEST_PORT;

    private StoreController storeController;
    private UserController userController;

    @BeforeEach
    public void setUp() throws LifecycleException {
        // Create controllers with mocked services
        storeController = new StoreController(storeService);
        userController = new UserController(authenticationService);

        // Start embedded Tomcat server with mocked controllers
        tomcat = new Tomcat();
        tomcat.setPort(TEST_PORT);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        // Register controllers
        Tomcat.addServlet(context, "storeController", storeController);
        context.addServletMappingDecoded("/api/v1/stores/*", "storeController");

        Tomcat.addServlet(context, "userController", userController);
        context.addServletMappingDecoded("/api/v1/users/*", "userController");

        tomcat.start();

        // Configure RestAssured
        RestAssured.baseURI = BASE_URL;
        RestAssured.port = TEST_PORT;
    }

    @AfterEach
    public void tearDown() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
        }
        // Reset mocks after each test
        reset(storeService, authenticationService);
    }

    // ==================== STORE CONTROLLER MOCK TESTS ====================

    @Test
    @DisplayName("Mock: Create store - verify service call")
    public void testCreateStoreWithMock() throws Exception {
        Store store = new Store("123", "address", "desc");

        when(storeService.showStore("123", "admin")).thenReturn(null);
        when(storeService.provisionStore("123", "MyStore", "address", "admin")).thenReturn(store);

        given()
            .contentType(ContentType.URLENC)
            .param("storeId", "123")
            .param("name", "MyStore")
            .param("address", "address")
        .when()
            .post("/api/v1/stores")
        .then()
            .statusCode(201)
            .body("id", equalTo("123"));

        verify(storeService).provisionStore("123", "MyStore", "address", "admin");
    }

    @Test
    @DisplayName("Mock: Get all stores - verify service call")
    public void testGetAllStoresWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Get store by ID - verify service call")
    public void testGetStoreByIdWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Update store - verify service call")
    public void testUpdateStoreWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Delete store - verify service call")
    public void testDeleteStoreWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Store error handling - service throws exception")
    public void testStoreErrorHandlingWithMock() throws Exception {
    }

    // ==================== USER CONTROLLER MOCK TESTS ====================

    @Test
    @DisplayName("Mock: Register user - verify service call")
    public void testRegisterUserWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Get all users - verify service call")
    public void testGetAllUsersWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Get user by email - verify service call")
    public void testGetUserByEmailWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Get user by email - user not found")
    public void testGetUserByEmailNotFoundWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Update user - verify service call")
    public void testUpdateUserWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Delete user - verify service call")
    public void testDeleteUserWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Delete user - user not found")
    public void testDeleteUserNotFoundWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Register duplicate user - verify conflict handling")
    public void testRegisterDuplicateUserWithMock() throws Exception {
    }

    @Test
    @DisplayName("Mock: Verify no unexpected service calls")
    public void testNoUnexpectedServiceCalls() throws Exception {
    }
}
