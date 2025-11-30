package com.se300.store.controller.unit;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;

import io.restassured.RestAssured;

/**
 * Unit tests for Store and User controllers using Mockito and RestAssured.
 * These tests mock the service layer to test controller logic in isolation.
 */
@DisplayName("Controller Mock Tests - Unit Testing with Mockito")
@ExtendWith(MockitoExtension.class)
public class ControllerUnitTest {

    //COMPLETE: Implement Unit Tests for Smart Store Controllers

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
        Store store = new Store("123", "addr", "desc");
        when(storeService.getAllStores()).thenReturn(java.util.List.of(store));

        given()
        .when()
            .get("/api/v1/stores")
        .then()
            .statusCode(200)
            .body("[0].id", equalTo("123"));

        verify(storeService).getAllStores();
    }

    @Test
    @DisplayName("Mock: Get store by ID - verify service call")
    public void testGetStoreByIdWithMock() throws Exception {
        Store store = new Store("123", "addr", "desc");
        when(storeService.showStore("123", "admin")).thenReturn(store);

        given() 
            .param("storeId", "123")
        .when()
            .get("/api/v1/stores/123")
        .then()
            .statusCode(200)
            .body("id", equalTo("123"));

        verify(storeService).showStore("123", "admin");
    }

    @Test
    @DisplayName("Mock: Update store - verify service call")
    public void testUpdateStoreWithMock() throws Exception {
        Store store = new Store("123", "addr", "desc");

        when(storeService.showStore(eq("123"), "admin"))
            .thenReturn(store);

        when(storeService.updateStore("123", "description", "address"))
                .thenReturn(store);

        given()
            .param("description", "description")  
            .param("address", "address")          
        .when()
            .put("/api/v1/stores/123")
        .then()
            .statusCode(200)
            .body("id", equalTo("123"));
        verify(storeService).updateStore("123", "description", "address");
    }


    @Test
    @DisplayName("Mock: Delete store - verify service call")
    public void testDeleteStoreWithMock() throws Exception {
        Store store = new Store("123", "addr", "desc");

        when(storeService.showStore(eq("123"), anyString()))
            .thenReturn(store);

        doNothing().when(storeService).deleteStore("123");
  
        given()
            .param("storeId", "123")
        .when()
            .delete("/api/v1/stores/123")
        .then()
            .statusCode(204);
        verify(storeService).deleteStore("123");
    }

    @Test
    @DisplayName("Mock: Store error handling - service throws exception")
    public void testStoreErrorHandlingWithMock() throws Exception {
        when(storeService.showStore("123", "admin"))
        .thenThrow(new com.se300.store.model.StoreException("show", "failure"));

        given()
            .param("storeId", "123")
        .when()
            .get("/api/v1/stores/123")
        .then()
            .statusCode(404)
            .body("message", equalTo("Store Does Not Exist"));

        verify(storeService).showStore("123", "admin");
    }

    // ==================== USER CONTROLLER MOCK TESTS ====================

    @Test
    @DisplayName("Mock: Register user - verify service call")
    public void testRegisterUserWithMock() throws Exception {
        User user = new User("test@gmail.com", "1234", "Anon");

        when(authenticationService.registerUser("test@gmail.com", "1234", "Anon")).thenReturn(user);

        given()
            .param("email", "test@gmail.com")
            .param("password", "1234")
            .param("name", "Anon")
        .when()
            .post("/api/v1/users/")
        .then()
            .statusCode(200);
        
        verify(authenticationService).registerUser("test@gmail.com", "1234", "Anon");
    }

    @Test
    @DisplayName("Mock: Get all users - verify service call")
    public void testGetAllUsersWithMock() throws Exception {
        User user = new User("test@gmail.com", "1234", "Anon");

        when(authenticationService.getAllUsers()).thenReturn(java.util.List.of(user));

        given()
        .when()
            .get("/api/v1/users")
        .then()
            .statusCode(200)
            .body("[0].email", equalTo("test@gmail.com"));
        
        verify(authenticationService).getAllUsers();
    }

    @Test
    @DisplayName("Mock: Get user by email - verify service call")
    public void testGetUserByEmailWithMock() throws Exception {
        User user = new User("test@gmail.com", "1234", "Anon");

        when(authenticationService.getUserByEmail("test@gmail.com")).thenReturn(user);

        given()
        .when()
            .get("/api/v1/users/test@gmail.com")
        .then()
            .statusCode(200)
            .body("email", equalTo("test@gmail.com"));
        
        verify(authenticationService).getUserByEmail("test@gmail.com");
    }

    @Test
    @DisplayName("Mock: Get user by email - user not found")
    public void testGetUserByEmailNotFoundWithMock() throws Exception {
        when(authenticationService.getUserByEmail("test@gmail.com")).thenReturn(null);

        given()
        .when()
            .get("/api/v1/users/test@gmail.com")
        .then()
            .statusCode(404)
            .body("message", equalTo("User not found"));
        
        verify(authenticationService).getUserByEmail("test@gmail.com");
    }

    @Test
    @DisplayName("Mock: Update user - verify service call")
    public void testUpdateUserWithMock() throws Exception {
        User user = new User("test@gmail.com", "1234", "Anon");
        User updatedUser = new User("test@gmail.com", "12345", "NON");

        
        when(authenticationService.getUserByEmail("test@gmail.com")).thenReturn(user);
        when(authenticationService.updateUser("test@gmail.com", "12345", "NON"))
        .thenReturn(updatedUser);

        given()
            .param("email", "test@gmail.com")
            .param("password", "12345")
            .param("name", "NON")
        .when()
            .put("/api/v1/users/test@gmail.com")
        .then()
            .statusCode(200);

        verify(authenticationService).updateUser("test@gmail.com", "12345", "NON");
    }

    @Test
    @DisplayName("Mock: Delete user - verify service call")
    public void testDeleteUserWithMock() throws Exception {
        given()
            .param("email", "test@gmail.com")
        .when()
            .delete("/api/v1/users/test@gmail.com") 
        .then()
            .statusCode(200); 

        verify(authenticationService).deleteUser("test@gmail.com");
    }

    @Test
    @DisplayName("Mock: Delete user - user not found")
    public void testDeleteUserNotFoundWithMock() throws Exception {
        when(authenticationService.getUserByEmail("missing@gmail.com"))
        .thenReturn(null);

        given()
            .param("email", "missing@gmail.com")
        .when()
            .delete("/api/v1/users/missing@gmail.com")
        .then()
            .statusCode(404);

        verify(authenticationService).getUserByEmail("missing@gmail.com");
        verify(authenticationService, never()).deleteUser(anyString());
    }

    @Test
    @DisplayName("Mock: Register duplicate user - verify conflict handling")
    public void testRegisterDuplicateUserWithMock() throws Exception {
        User existingUser = new User("test@gmail.com", "1234", "Anon");

        when(authenticationService.getUserByEmail("test@gmail.com")).thenReturn(existingUser);
        when(authenticationService.registerUser("test@gmail.com", "5678", "NewName")).thenReturn(null);

        given()
            .param("email", "test@gmail.com")
            .param("password", "5678")
            .param("name", "NewName")
        .when()
            .post("/api/v1/users/")
        .then()
            .statusCode(200);

        verify(authenticationService).registerUser("test@gmail.com", "5678", "NewName");
    }

    @Test
    @DisplayName("Mock: Verify no unexpected service calls")
    public void testNoUnexpectedServiceCalls() throws Exception {
        User user = new User("test@gmail.com", "1234", "Anon");
        when(authenticationService.getUserByEmail("test@gmail.com")).thenReturn(user);

        given()
        .when()
            .get("/api/v1/users/test@gmail.com")
        .then()
            .statusCode(200)
            .body("email", equalTo("test@gmail.com"));

        verify(authenticationService, never()).deleteUser(anyString());
    }
}
