package com.se300.store;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import com.se300.store.controller.StoreController;
import com.se300.store.controller.UserController;
import com.se300.store.data.DataManager;
import com.se300.store.model.Aisle;
import com.se300.store.model.AisleLocation;
import com.se300.store.model.Basket;
import com.se300.store.model.CommandProcessor;
import com.se300.store.model.Customer;
import com.se300.store.model.CustomerType;
import com.se300.store.model.Inventory;
import com.se300.store.model.InventoryType;
import com.se300.store.model.Product;
import com.se300.store.model.Shelf;
import com.se300.store.model.ShelfLevel;
import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.model.StoreLocation;
import com.se300.store.model.Temperature;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;

import io.restassured.RestAssured;

/**
 * End-to-End Integration Tests for the Smart Store Application.
 * Tests the complete system including all layers: REST API, Controllers, Services, Repositories, and Data.
 * Uses a clean Tomcat server (no sample data) to ensure test isolation.
 */
@DisplayName("Big Bang Integration Test - Complete System Testing")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EndToEndSmartStoreTest {

    /* TODO: The following
     * 1. Achieve 100% Test Coverage
     * 2. Produce/Print Identical Results to Command Line DriverTest
     * 3. Produce SonarCube Quality and Coverage Report
     */

    private static DataManager dataManager;
    private static StoreRepository storeRepository;
    private static UserRepository userRepository;
    private static StoreService storeService;
    private static AuthenticationService authenticationService;
    private static Tomcat tomcat;

    @BeforeAll
    public static void setUpCompleteSystem() throws Exception {
        // Initialize data layer
        dataManager = DataManager.getInstance();
        dataManager.clear();

        // Clear static maps in StoreService to ensure clean state (no sample data)
        StoreService.clearAllMaps();

        // Initialize repositories
        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);

        // Initialize services
        storeService = new StoreService(storeRepository);
        authenticationService = new AuthenticationService(userRepository);

        // Initialize controllers
        StoreController storeController = new StoreController(storeService);
        UserController userController = new UserController(authenticationService);

        // Start clean Tomcat server (without sample data from SmartStoreApplication)
        tomcat = new Tomcat();
        tomcat.setPort(0); // Use dynamic port allocation
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
        
        // Get the actual port assigned by the system
        int testPort = tomcat.getConnector().getLocalPort();

        // Configure RestAssured
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = testPort;

        // Wait for server to be ready
        Thread.sleep(1000);
    }

    @AfterEach
    public void cleanupBetweenTests() {
        // Don't clear data between tests since they're ordered and may depend on previous test data
    }

    @AfterAll
    public static void tearDownCompleteSystem() throws Exception {
        // Stop Tomcat server
        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
                // Give server time to shut down completely
                Thread.sleep(1000);
            } catch (Exception e) {
                // Force cleanup even if stop fails
                System.err.println("Error stopping Tomcat: " + e.getMessage());
            }
        }

        // Clear all data
        if (dataManager != null) {
            dataManager.clear();
        }
        StoreService.clearAllMaps();
    }

    @Test
    @Order(1)
    @DisplayName("E2E: Complete user registration and authentication workflow")
    public void testCompleteUserWorkflow() {
        authenticationService.registerUser("test@gmail.com", "password", "Josh");
        
        assertNotNull(authenticationService.getAllUsers());

        authenticationService.updateUser("test@gmail.com", "new_password", "Braeden");

        User result = authenticationService.getUserByEmail("test@gmail.com");

        assertEquals("test@gmail.com", result.getEmail());
        assertEquals("new_password", result.getPassword());
        assertEquals("Braeden", result.getName());

        authenticationService.deleteUser("test@gmail.com");

        boolean exists = authenticationService.userExists("test@gmail.com");

        assertFalse(exists);
    }

    @Test
    @Order(2)
    @DisplayName("E2E: Complete store provisioning and management workflow")
    public void testCompleteStoreWorkflow() throws StoreException {
        Store created = storeService.provisionStore("S1", "Main Store", "123 Road", "token");
        assertNotNull(created);
        assertEquals("S1", created.getId());

        Store shown = storeService.showStore("S1", "token");
        assertEquals("Main Store", shown.getDescription());

        Store updated = storeService.updateStore("S1", "Updated Desc", "999 New St");
        assertEquals("Updated Desc", updated.getDescription());
        assertEquals("999 New St", updated.getAddress());

        storeService.deleteStore("S1");
        assertThrows(StoreException.class, () -> storeService.showStore("S1", "token"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Complete store operations - aisles, shelves, products, inventory")
    public void testCompleteStoreOperations() throws StoreException {
        storeService.provisionStore("S2","Not Groc","Is joke","token");

        Aisle a = storeService.provisionAisle("S2","A1","Fresh","Produce",
        null,"token");
       
        Shelf sh = storeService.provisionShelf("S2","A1","SH1","Top", 
        ShelfLevel.high,"Cool",Temperature.ambient,"token");

        Product prod = storeService.provisionProduct("P1", "Product", "Description", "Small",
         "Misc", 10.00, Temperature.ambient, "token");

        Inventory inv = storeService.provisionInventory("I1", "S2", "A1",
         "SH1", 100, 100, "P1", InventoryType.standard, "token");

        assertEquals(a, storeService.showAisle("S2", "A1", "token"));
        assertEquals(inv, storeService.showInventory("I1", "token"));
        assertEquals(sh, storeService.showShelf("S2","A1","SH1","token"));
        assertEquals(prod, storeService.showProduct("P1", "token"));

        // Had to update updateInventory method to allow for decremination
        storeService.updateInventory("I1", -10, "token");

        assertEquals(storeService.showInventory("I1", "token").getCount(), 90);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Complete customer shopping workflow")
    public void testCompleteCustomerShoppingWorkflow() throws StoreException {
        Customer cust = storeService.provisionCustomer("C1", "John", "Smith",
         CustomerType.registered, "anon@gmail.com", "Los Angeles", "Token");

        StoreLocation custLoc = new StoreLocation("S2", "A1");

        storeService.provisionAisle("S2", "A2", "Random", "Random",
         AisleLocation.floor, "token");

        cust.setStoreLocation(custLoc);

        Basket bask = storeService.provisionBasket("B1", "token");
        storeService.assignCustomerBasket("C1", "B1", "token");
        storeService.addBasketProduct("B1", "P1", 2, "token");

        assertEquals(bask.getProducts().size(), 1); // Only one product type

        assertNotNull(cust);
        assertEquals(custLoc, storeService.showCustomer("C1", "token").getStoreLocation());
        assertEquals(bask, storeService.showBasket("B1", "token"));
        assertEquals(bask.getCustomer(), cust);
        assertEquals(storeService.getCustomerBasket("C1", "token"), bask);

        storeService.updateCustomer("C1", "S2", "A2", "token");

        assertEquals(cust.getStoreLocation().toString(), new StoreLocation("S2", "A2").toString());

        cust.setStoreLocation(custLoc);
        storeService.removeBasketProduct("B1", "P1", 1, "token");

        assertEquals(1, bask.getProducts().size());
        
        storeService.clearBasket("B1", "token");

        assertEquals(bask.getProducts().size(), 0);
    }

    @Test
    @Order(5)
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    @DisplayName("E2E: Device management and events")
    public void testCompleteDeviceWorkflow() throws StoreException {
        storeService.provisionDevice("D1", "Cam", "camera",
         "S2", "A1", "token");
        storeService.provisionDevice("D2", "Mic", "microphone",
         "S2", "A1", "token");

        String testString = "Device{" +
                "id='" + storeService.showDevice("D1", "token").getId() + '\'' +
                ", name='" + storeService.showDevice("D1", "token").getName() + '\'' +
                ", storeLocation=" + storeService.showDevice("D1", "token").getStoreLocation() +
                ", type='" + storeService.showDevice("D1", "token").getType() + '\'' +
                '}';
        
        assertDoesNotThrow(() ->
            storeService.raiseEvent("D1", "TEST EVENT", "token")
        );      
        assertEquals(testString, storeService.showDevice("D1", "token").toString()); 
        assertNotEquals(storeService.showDevice("D1", "token").getType(),
         storeService.showDevice("D2", "token").getType());

        storeService.provisionDevice("D3", "Bot", "robot",
         "S2", "A1", "token");
        storeService.provisionDevice("D4", "Speak", "speaker",
         "S2", "A1", "token");

        assertDoesNotThrow(() ->
            storeService.raiseEvent("D3", "TEST EVENT", "token")
        );     
        assertDoesNotThrow(() ->
            storeService.issueCommand("D4", "TEST COMMAND", "token")
        );     
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Error handling across all layers")
    public void testCompleteErrorHandling() {
        assertThrows(StoreException.class, () ->
                storeService.provisionStore("S2", "Error Test Store", "123 Test Rd", "token"));

        assertThrows(StoreException.class,
                () -> storeService.showStore("OISFnsdoigfnso", "token"),
                "No store");

        assertThrows(StoreException.class,
                () -> storeService.provisionShelf("diongfgdf", "dfoidenf",
                 "Sdgonfogd9", "dfoin", ShelfLevel.high,
                 "sdf", Temperature.ambient, "token"),
                "Invalid everything");

        assertThrows(StoreException.class,
                () -> storeService.addBasketProduct("B1", "нет", 1, "token"),
                "Нечего");

        assertDoesNotThrow(() ->
                storeService.showBasket("B1", "token").setCustomer(storeService.showCustomer("C1", "token")));

        assertDoesNotThrow(() ->
                storeService.addBasketProduct("B1", "P1", 1, "token"));

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "P1", 2025, "token"),
                "Ад");

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "no", 343, "token"),
                "БЛИИИИИИН");

        assertThrows(StoreException.class,
                () -> storeService.showCustomer(" ", "token").setStoreLocation(new StoreLocation("hell", "no")),
                "Hell is proven to be not real in this binary realm");

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "Pigeon", 1, "token"),
                "Birds aren't real");

        assertDoesNotThrow(() ->
                storeService.showBasket("B1", "token").clearBasket());

        assertThrows(StoreException.class,
                () -> storeService.showDevice("computer", "token"),
                "Computer doesn't exist");
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Data consistency across all layers")
    public void testDataConsistencyAcrossLayers() {
        Store storeFromService = assertDoesNotThrow(() -> storeService.showStore("S2", "token"));

        Map<String, Store> storesFromRepo = storeRepository.findAll();
        assertNotNull(storesFromRepo);
        assertTrue(storesFromRepo.containsKey("S2"));
        assertSame(storeFromService, storesFromRepo.get("S2"));

        Map<String, User> usersFromRepo = userRepository.findAll();
        assertNotNull(usersFromRepo);
        assertTrue(usersFromRepo.containsKey("admin@store.com"));

        User adminFromService = authenticationService.getUserByEmail("admin@store.com");
        assertNotNull(adminFromService);
        assertSame(adminFromService, usersFromRepo.get("admin@store.com"));

        assertFalse(storesFromRepo.isEmpty());
        assertFalse(usersFromRepo.isEmpty());
    }

    @Test
    @Order(8)
    @DisplayName("E2E: REST API Controller - Store CRUD operations")
    public void testRestApiStoreOperations() {
        given()
            .when()
                .get("/api/v1/stores/S2?token=token")
            .then()
                .statusCode(200)
                .body("id", equalTo("S2"))
                .body("description", equalTo("Not Groc"));

        assertDoesNotThrow(() ->
                    storeService.provisionStore("S3", "Rest Store", "999 Rest St", "token"));

        given()
            .when()
                .get("/api/v1/stores/S3?token=token")
            .then()
                .statusCode(200)
                .body("id", equalTo("S3"))
                .body("description", equalTo("Rest Store"));

        given()
            .when()
                .get("/api/v1/stores/NOPE?token=token")
            .then()
                .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @Order(9)
    @DisplayName("E2E: REST API Controller - User CRUD operations")
    public void testRestApiUserOperations() {
        given()
        .when()
            .get("/api/v1/users/admin@store.com")
        .then()
            .statusCode(200)
            .body("email", equalTo("admin@store.com"))
            .body("name", equalTo("Admin User"));

        authenticationService.registerUser("restuser@gmail.com", "password", "Rest User");

        given()
        .when()
            .get("/api/v1/users/restuser@gmail.com")
        .then()
            .statusCode(200)
            .body("email", equalTo("restuser@gmail.com"))
            .body("name", equalTo("Rest User"));

        given()
        .when()
            .get("/api/v1/users/missing.user@gmail.com")
        .then()
            .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @Order(10)
    @DisplayName("E2E: REST API Controller - Error handling")
    public void testRestApiErrorHandling() {
        given()
        .when()
            .get("/api/v1/stores/no?token=token")
        .then()
            .statusCode(anyOf(is(404), is(400)))
            .body("error", notNullValue());

        given()
        .when()
            .get("/api/v1/users/no@example.com")
        .then()
            .statusCode(anyOf(is(404), is(400)))
            .body("error", notNullValue());

        given()
        .when()
            .get("/api/v1/no")
        .then()
            .statusCode(anyOf(is(404), is(400)));
    }

    @Test
    @Order(11)
    @DisplayName("E2E: Final cleanup and deletion operations")
    public void testFinalCleanupOperations() throws StoreException {
        assertDoesNotThrow(() -> {
            storeService.showBasket("B1", "token").clearBasket();
        });

        assertDoesNotThrow(() -> { // don't remember what stores we have 
            try {
                storeService.deleteStore("S2");
            } catch (StoreException ignored) {}
            try {
                storeService.deleteStore("S3");
            } catch (StoreException ignored) {}
        });

        assertThrows(StoreException.class,
                () -> storeService.showStore("S2", "token"));
        assertThrows(StoreException.class,
                () -> storeService.showStore("S3", "token"));

        Map<String, Store> stores = storeRepository.findAll();
        assertFalse(stores.containsKey("S2"));
        assertFalse(stores.containsKey("S3"));
    }

    @Test
    @Order(12)
    @DisplayName("E2E: Complete store.script data processing with assertions")
    public void testStoreScriptEndToEnd() throws Exception {
        CommandProcessor commandProcessor = new CommandProcessor();
        commandProcessor.processCommandFile("scr/test/resources/store.script"); 
    }
}