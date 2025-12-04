package com.se300.store;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
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
        Store created = storeService.provisionStore("S1", "Main Store", "123 Road", "admin");
        assertNotNull(created);
        assertEquals("S1", created.getId());

        Store shown = storeService.showStore("S1", "admin");
        assertEquals("Main Store", shown.getDescription());

        Store updated = storeService.updateStore("S1", "Updated Desc", "999 New St");
        assertEquals("Updated Desc", updated.getDescription());
        assertEquals("999 New St", updated.getAddress());

        storeService.deleteStore("S1");
        assertThrows(StoreException.class, () -> storeService.showStore("S1", "admin"));
    }

    @Test
    @Order(3)
    @DisplayName("E2E: Complete store operations - aisles, shelves, products, inventory")
    public void testCompleteStoreOperations() throws StoreException {
        storeService.provisionStore("S2","Not Groc","Is joke","admin");

        Aisle a = storeService.provisionAisle("S2","A1","Fresh","Produce",
        null,"admin");
       
        Shelf sh = storeService.provisionShelf("S2","A1","SH1","Top", 
        ShelfLevel.high,"Cool",Temperature.ambient,"admin");

        Product prod = storeService.provisionProduct("P1", "Product", "Description", "Small",
         "Misc", 10.00, Temperature.ambient, "admin");

        Inventory inv = storeService.provisionInventory("I1", "S2", "A1",
         "SH1", 100, 100, "P1", InventoryType.standard, "admin");
        
        assertThrows(StoreException.class, () -> storeService.provisionInventory("I1", "S2", "A1",
         "SH1", 100, 100, "P1", InventoryType.standard, "admin"));
        assertThrows(StoreException.class, () -> storeService.provisionInventory("I1",
         "S2", "A1","SH1", -1, 0, "P1", InventoryType.standard, "admin"));
        assertEquals(a, storeService.showAisle("S2", "A1", "admin"));
        assertEquals(inv, storeService.showInventory("I1", "admin"));
        assertEquals(sh, storeService.showShelf("S2","A1","SH1","admin"));
        assertEquals(prod, storeService.showProduct("P1", "admin"));

        // Had to update updateInventory method to allow for decremination
        storeService.updateInventory("I1", -10, "admin");

        assertEquals(storeService.showInventory("I1", "admin").getCount(), 90);
    }

    @Test
    @Order(4)
    @DisplayName("E2E: Complete customer shopping workflow")
    public void testCompleteCustomerShoppingWorkflow() throws StoreException {
        Customer cust = storeService.provisionCustomer("C1", "John", "Smith",
         CustomerType.registered, "anon@gmail.com", "Los Angeles", "admin");

        StoreLocation custLoc = new StoreLocation("S2", "A1");
        StoreLocation testLoc = new StoreLocation("S2", null);

        storeService.provisionAisle("S2", "A2", "Random", "Random",
         AisleLocation.floor, "admin");

        cust.setStoreLocation(custLoc);

        Basket bask = storeService.provisionBasket("B1", "admin");
        storeService.assignCustomerBasket("C1", "B1", "admin");
        storeService.addBasketProduct("B1", "P1", 2, "admin");
        assertEquals(bask.getProducts().size(), 1); // Only one product type

        assertNotNull(cust);
        assertEquals(custLoc, storeService.showCustomer("C1", "admin").getStoreLocation());
        assertEquals(bask, storeService.showBasket("B1", "admin"));
        assertEquals(bask.getCustomer(), cust);
        assertEquals(storeService.getCustomerBasket("C1", "admin"), bask);
        assertThrows(StoreException.class, () -> storeService.addBasketProduct("B1", null, 0, "admin"));
        // assertThrows(StoreException.class, () -> storeService.addBasketProduct("B1", "P1", 0, "admin"));
        storeService.updateCustomer("C1", "S2", "A2", "admin");

        assertEquals(cust.getStoreLocation().toString(), new StoreLocation("S2", "A2").toString());

        cust.setStoreLocation(custLoc);
        storeService.removeBasketProduct("B1", "P1", 1, "admin");

        assertEquals(1, bask.getProducts().size());
        
        storeService.clearBasket("B1", "admin");
        assertEquals(bask.getProducts().size(), 0);

        // storeService.showCustomer("C1", "admin").setStoreLocation(null);
        // assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1", "P1", 1, "admin"));
        
        // storeService.showCustomer("C1", "admin").setStoreLocation(custLoc);
        // // storeService.assignCustomerBasket("C1", "B1", "admin");
        // assertThrows(StoreException.class, () -> storeService.showBasket("B1", "admin").addProduct("P1", 10));
        // storeService.updateInventory("I1", 20, "admin");
        // assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1", "P1", 10, "admin"));
        // storeService.updateInventory("I1", -10, "admin");
        // storeService.removeBasketProduct("B1", "P1", 10, "admin");
        // storeService.clearBasket("B1", "admin");
    }

    @Test
    @Order(5)
    @Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
    @DisplayName("E2E: Device management and events")
    public void testCompleteDeviceWorkflow() throws StoreException {
        storeService.provisionDevice("D1", "Cam", "camera",
         "S2", "A1", "admin");
        storeService.provisionDevice("D2", "Mic", "microphone",
         "S2", "A1", "admin");

        String testString = "Device{" +
                "id='" + storeService.showDevice("D1", "admin").getId() + '\'' +
                ", name='" + storeService.showDevice("D1", "admin").getName() + '\'' +
                ", storeLocation=" + storeService.showDevice("D1", "admin").getStoreLocation() +
                ", type='" + storeService.showDevice("D1", "admin").getType() + '\'' +
                '}';
        
        assertDoesNotThrow(() ->
            storeService.raiseEvent("D1", "TEST EVENT", "admin")
        );      
        assertEquals(testString, storeService.showDevice("D1", "admin").toString()); 
        assertNotEquals(storeService.showDevice("D1", "admin").getType(),
         storeService.showDevice("D2", "admin").getType());

        storeService.provisionDevice("D3", "Bot", "robot",
         "S2", "A1", "admin");
        storeService.provisionDevice("D4", "Speak", "speaker",
         "S2", "A1", "admin");

        assertDoesNotThrow(() ->
            storeService.raiseEvent("D3", "TEST EVENT", "admin")
        );     
        assertDoesNotThrow(() ->
            storeService.issueCommand("D4", "TEST COMMAND", "admin")
        );     
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Error handling across all layers")
    public void testCompleteErrorHandling() {
        assertThrows(StoreException.class, () ->
                storeService.provisionStore("S2", "Error Test Store", "123 Test Rd", "admin"));

        assertThrows(StoreException.class,
                () -> storeService.showStore("OISFnsdoigfnso", "admin"),
                "No store");

        assertThrows(StoreException.class,
                () -> storeService.provisionShelf("diongfgdf", "dfoidenf",
                 "Sdgonfogd9", "dfoin", ShelfLevel.high,
                 "sdf", Temperature.ambient, "admin"),
                "Invalid everything");

        assertThrows(StoreException.class,
                () -> storeService.addBasketProduct("B1", "нет", 1, "admin"),
                "Нечего");
        
        assertThrows(StoreException.class, () -> storeService.provisionBasket("B1", "admin"));

        assertDoesNotThrow(() ->
                storeService.showBasket("B1", "admin").setCustomer(storeService.showCustomer("C1", "admin")));

        assertDoesNotThrow(() ->
                storeService.addBasketProduct("B1", "P1", 1, "admin"));

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "P1", 2025, "admin"),
                "Ад");

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "no", 343, "admin"),
                "БЛИИИИИИН");

        assertThrows(StoreException.class,
                () -> storeService.showCustomer(" ", "admin").setStoreLocation(new StoreLocation("hell", "no")),
                "Hell is proven to be not real in this binary realm");

        assertThrows(StoreException.class,
                () -> storeService.removeBasketProduct("B1", "Pigeon", 1, "admin"),
                "Birds aren't real");

        assertDoesNotThrow(() ->
                storeService.showBasket("B1", "admin").clearBasket());

        assertThrows(StoreException.class,
                () -> storeService.showDevice("computer", "admin"),
                "Computer doesn't exist");
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Data consistency across all layers")
    public void testDataConsistencyAcrossLayers() {
        Store storeFromService = assertDoesNotThrow(() -> storeService.showStore("S2", "admin"));

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
                    storeService.provisionStore("S3", "Rest Store", "999 Rest St", "admin"));

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
            .body("error", nullValue());

        given()
        .when()
            .get("/api/v1/users/no@example.com")
        .then()
            .statusCode(anyOf(is(404), is(400)))
            .body("error", nullValue());

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
        assertThrows(NullPointerException.class, () -> { // deleted earlier in error layer test
            storeService.showBasket("B1", "admin").clearBasket();
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
                () -> storeService.showStore("S2", "admin"));
        assertThrows(StoreException.class,
                () -> storeService.showStore("S3", "admin"));

        Map<String, Store> stores = storeRepository.findAll();
        assertFalse(stores.containsKey("S2"));
        assertFalse(stores.containsKey("S3"));
    }

    @Test
    @Order(12)
    @DisplayName("E2E: Complete store.script data processing with assertions")
    public void testStoreScriptEndToEnd() throws Exception {
        CommandProcessor commandProcessor = new CommandProcessor();
        assertDoesNotThrow(() -> commandProcessor.processCommandFile("src/test/resources/store.script")); 

        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent)); 

        commandProcessor.processCommandFile("does_not_exist.txt");

        String output = errContent.toString();
        assertTrue(output.contains("NoSuchFileException"));
    }
}