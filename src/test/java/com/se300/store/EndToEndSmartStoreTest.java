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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
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
import com.se300.store.model.CommandException;
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
        storeService.updateCustomer("C1", "S2", "A2", "admin");

        assertEquals(cust.getStoreLocation().toString(), new StoreLocation("S2", "A2").toString());

        cust.setStoreLocation(custLoc);
        storeService.removeBasketProduct("B1", "P1", 1, "admin");

        assertEquals(1, bask.getProducts().size());
        
        storeService.clearBasket("B1", "admin");
        assertEquals(bask.getProducts().size(), 0);
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
    public void testRestApiStoreOperations() throws StoreException {
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
        assertThrows(StoreException.class, () -> storeService.showAisle("S3", "A2", "admin"));
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
        assertThrows(CommandException.class, () -> commandProcessor.processCommand("dasionfo"));
        assertDoesNotThrow(() -> commandProcessor.processCommandFile("src/test/resources/test.script")); 
        
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        System.setErr(new PrintStream(errContent)); 

        commandProcessor.processCommandFile("does_not_exist.txt");

        String output = errContent.toString();
        assertTrue(output.contains("NoSuchFileException"));

        BufferedWriter writer = new BufferedWriter(new FileWriter("E2E_Script_Results.txt"));

        // Store Script Processing
        // Store Commands
        writer.write("define  store  store_123 name Chapman address \"One University Drive, Orange, CA 92866\"");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  store  store_123 name Chapman address \"One University Drive, Orange, CA 92866\""));

        writer.write("show  store  store_123");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  store  store_123"));

        // Aisle Commands
        writer.write("define  aisle  store_123:aisle_A1  name  AISLE_A1  description  AISLE_A1_desc location  store_room");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_A1  name  AISLE_A1  description  AISLE_A1_desc location  store_room"));

        writer.write("define  aisle  store_123:aisle_A2  name  AISLE_A2  description  AISLE_A2_desc location  floor");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_A2  name  AISLE_A2  description  AISLE_A2_desc location  floor"));

        writer.write("define  aisle  store_123:aisle_A3  name  AISLE_A3  description  AISLE_A3_desc location  floor");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_A3  name  AISLE_A3  description  AISLE_A3_desc location  floor"));

        writer.write("define  aisle  store_123:aisle_B1  name  AISLE_B1  description  AISLE_B1_desc location  store_room");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_B1  name  AISLE_B1  description  AISLE_B1_desc location  store_room"));

        writer.write("define  aisle  store_123:aisle_B2  name  AISLE_B2  description  AISLE_B2_desc location  floor");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_B2  name  AISLE_B2  description  AISLE_B2_desc location  floor"));

        writer.write("define  aisle  store_123:aisle_B3  name  AISLE_B3  description  AISLE_B3_desc location  floor");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  aisle  store_123:aisle_B3  name  AISLE_B3  description  AISLE_B3_desc location  floor"));

        // Show aisles
        writer.write("show  aisle  store_123:aisle_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  aisle  store_123:aisle_A1"));

        writer.write("show  aisle  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  aisle  store_123:aisle_A2"));

        writer.write("show  aisle  store_123:aisle_B2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  aisle  store_123:aisle_B2"));

        // Shelf Commands
        writer.write("define  shelf  store_123:aisle_A1:shelf_q1  name  Shelf_Q1  level  high   description  \"lasanaga\" temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A1:shelf_q1  name  Shelf_Q1  level  high   description  \"lasanaga\" temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_A1:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A1:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_A1:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A1:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_A1:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A1:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_A1:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A1:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        writer.write("define  shelf  store_123:aisle_A2:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A2:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_A2:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A2:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_A2:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A2:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_A2:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A2:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_A2:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A2:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        writer.write("define  shelf  store_123:aisle_A3:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A3:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_A3:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A3:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_A3:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A3:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_A3:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class,() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A3:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_A3:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_A3:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        writer.write("define  shelf  store_123:aisle_B1:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B1:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_B1:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B1:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_B1:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B1:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_B1:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B1:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_B1:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B1:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        writer.write("define  shelf  store_123:aisle_B2:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B2:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_B2:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B2:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_B2:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B2:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_B2:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B2:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_B2:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B2:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        writer.write("define  shelf  store_123:aisle_B3:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B3:shelf_q1  name  Shelf_Q1  level  high   description  Shelf_Q1_Desc  temperature  frozen"));

        writer.write("define  shelf  store_123:aisle_B3:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B3:shelf_q2  name  Shelf_Q2  level  medium   description  Shelf_Q2_Desc  temperature  ambient"));

        writer.write("define  shelf  store_123:aisle_B3:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B3:shelf_q3  name  Shelf_Q3  level  low   description  Shelf_Q3_Desc  temperature  refrigerated"));

        writer.write("define  shelf  store_123:aisle_B3:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B3:shelf_q4  name  Shelf_Q4  level  low   description  Shelf_Q4_Desc  temperature  warm"));

        writer.write("define  shelf  store_123:aisle_B3:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "define  shelf  store_123:aisle_B3:shelf_q5  name  Shelf_Q5  level  medium   description  Shelf_Q5_Desc  temperature  hot"));

        // Show shelves
        writer.write("show  shelf  store_123:aisle_A2:shelf_q1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  shelf  store_123:aisle_A2:shelf_q1"));

        writer.write("show  shelf  store_123:aisle_B1:shelf_q5");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  shelf  store_123:aisle_B1:shelf_q5"));

        // Product Commands
        writer.write("define  product  prod10  name  bournvita  description  bournvita  size 250g  category  Food  unit_price  2  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  product  prod10  name  bournvita  description  bournvita  size 250g  category  Food  unit_price  2  temperature  ambient"));

        writer.write("define  product  prod11  name  tea  description  \"green tea\"  size 500g  category  Food  unit_price  1  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  product  prod11  name  tea  description  \"green tea\"  size 500g  category  Food  unit_price  1  temperature  ambient"));

        writer.write("define  product  prod12  name  coffee  description  \"premium coffee\" size 100g  category  Food  unit_price  3  temperature  refrigerated");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  product  prod12  name  coffee  description  \"premium coffee\" size 100g  category  Food  unit_price  3  temperature  refrigerated"));

        writer.write("define  product  prod13  name  goggles  description  \"eye wear\" size 3  category  Men_accessories  unit_price  5  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  product  prod13  name  goggles  description  \"eye wear\" size 3  category  Men_accessories  unit_price  5  temperature  ambient"));

        writer.write("define  product  prod14  name  sun_glass  description  \"eye wear\" size 3  category  Fashion  unit_price  7  temperature  ambient");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  product  prod14  name  sun_glass  description  \"eye wear\" size 3  category  Fashion  unit_price  7  temperature  ambient"));

        // Show product
        writer.write("show  product  prod10");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  product  prod10"));

        // Inventory Commands
        writer.write("define  inventory  inv_u21  location  store_123:aisle_A1:shelf_q1 capacity  1500  count  1000 type standard product  prod10");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u21  location  store_123:aisle_A1:shelf_q1 capacity  1500  count  1000 type standard product  prod10"));

        writer.write("define  inventory  inv_u22  location  store_123:aisle_A1:shelf_q2 capacity  1500  count  1000 type flexible product  prod11");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u22  location  store_123:aisle_A1:shelf_q2 capacity  1500  count  1000 type flexible product  prod11"));

        writer.write("define  inventory  inv_u23  location  store_123:aisle_B2:shelf_q1 capacity  500  count  200  type flexible product  prod11");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u23  location  store_123:aisle_B2:shelf_q1 capacity  500  count  200  type flexible product  prod11"));

        writer.write("define  inventory  inv_u24  location  store_123:aisle_B2:shelf_q2 capacity  500  count  200  type standard product  prod10");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u24  location  store_123:aisle_B2:shelf_q2 capacity  500  count  200  type standard product  prod10"));

        writer.write("define  inventory  inv_u25  location  store_123:aisle_A2:shelf_q1 capacity  200  count  100  type standard product  prod10");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u25  location  store_123:aisle_A2:shelf_q1 capacity  200  count  100  type standard product  prod10"));

        writer.write("define  inventory  inv_u26  location  store_123:aisle_A2:shelf_q3 capacity  300  count  100  type flexible product  prod12");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  inventory  inv_u26  location  store_123:aisle_A2:shelf_q3 capacity  300  count  100  type flexible product  prod12"));

        // Show inventory
        writer.write("show  inventory  inv_u24");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  inventory  inv_u24"));

        writer.write("show  inventory  inv_u21");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  inventory  inv_u21"));

        // Update inventory
        writer.write("update  inventory  inv_u26  update_count  260");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "update  inventory  inv_u26  update_count  260"));

        writer.write("update  inventory  inv_u24  update_count  4");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "update  inventory  inv_u24  update_count  4"));

        writer.write("update  inventory  inv_u21  update_count  7");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "update  inventory  inv_u21  update_count  7"));

        // Customer Commands
        writer.write("define  customer  cust_AB        first_name  JSON  last_name  WALLACE type  guest  email_address  json.wallace@ymail.com  account  json");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_AB        first_name  JSON  last_name  WALLACE type  guest  email_address  json.wallace@ymail.com  account  json"));

        writer.write("define  customer  cust_21        first_name  BILL  last_name  ROSE type  registered  email_address  bill.rose@gmail.com  account  bill");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_21        first_name  BILL  last_name  ROSE type  registered  email_address  bill.rose@gmail.com  account  bill"));

        writer.write("define  customer  cust_22        first_name  MARY last_name  KELVIN type  registered  email_address  mary.kevin@yahoomail.com  account  mary");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_22        first_name  MARY last_name  KELVIN type  registered  email_address  mary.kevin@yahoomail.com  account  mary"));

        writer.write("define  customer  cust_E2        first_name  SILVER  last_name  HAWK type  guest  email_address  silver.hawk@rocketmail.com  account  silver");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_E2        first_name  SILVER  last_name  HAWK type  guest  email_address  silver.hawk@rocketmail.com  account  silver"));

        writer.write("define  customer  cust_23        first_name  MEGON  last_name  FOX type  guest  email_address  megonfox@testmail.com  account  megon");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_23        first_name  MEGON  last_name  FOX type  guest  email_address  megonfox@testmail.com  account  megon"));

        writer.write("define  customer  cust_24        first_name  MARIA last_name  WILIAMSON type  registered  email_address  maria4567@ymail.com  account  maria");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_24        first_name  MARIA last_name  WILIAMSON type  registered  email_address  maria4567@ymail.com  account  maria"));

        writer.write("define  customer  cust_S2        first_name  SALINA  last_name  GOMEZ type  registered  email_address  salina@gmail.com  account  salina");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  customer  cust_S2        first_name  SALINA  last_name  GOMEZ type  registered  email_address  salina@gmail.com  account  salina"));

        // Update customer location
        writer.write("update  customer  cust_S2  location  store_123:aisle_B2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "update  customer  cust_S2  location  store_123:aisle_B2"));

        writer.write("update  customer  cust_21  location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "update  customer  cust_21  location  store_123:aisle_A2"));

        writer.write("update  customer  cust_22  location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "update  customer  cust_22  location  store_123:aisle_A2"));

        // Show customers
        writer.write("show  customer  cust_21");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  customer  cust_21"));

        writer.write("show  customer  cust_S2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  customer  cust_S2"));

        // Basket Commands
        writer.write("define basket b1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("define basket b1"));

        writer.write("define basket b2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("define basket b2"));

        writer.write("define basket b3");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("define basket b3"));

        writer.write("define basket b4");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("define basket b4"));

        writer.write("define basket b5");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("define basket b5"));

        // Assign baskets
        writer.write("assign basket b1 customer  cust_21");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "assign basket b1 customer  cust_21"));

        writer.write("assign basket b2 customer  cust_S2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "assign basket b2 customer  cust_S2"));

        writer.write("assign basket b3 customer  cust_22");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "assign basket b3 customer  cust_22"));

        // Get customer basket
        writer.write("get_customer_basket  cust_21");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "get_customer_basket  cust_21"));

        writer.write("get_customer_basket  cust_S2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "get_customer_basket  cust_S2"));

        writer.write("get_customer_basket  cust_22");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "get_customer_basket  cust_22"));

        // Add basket items
        writer.write("add_basket_item  b1  product  prod10  item_count  4");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "add_basket_item  b1  product  prod10  item_count  4"));

        writer.write("add_basket_item  b3  product  prod11  item_count  2");
        writer.newLine();
        assertThrows(StoreException.class, () -> commandProcessor.processCommand(
                "add_basket_item  b3  product  prod11  item_count  2"));

        writer.write("add_basket_item  b3  product  prod12  item_count  7");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "add_basket_item  b3  product  prod12  item_count  7"));

        // Remove basket items
        writer.write("remove_basket_item  b1  product  prod10  item_count  1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "remove_basket_item  b1  product  prod10  item_count  1"));

        writer.write("remove_basket_item  b3  product  prod12  item_count  5");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "remove_basket_item  b3  product  prod12  item_count  5"));

        // Clear basket
        writer.write("clear_basket  b3");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "clear_basket  b3"));

        // Show basket items
        writer.write("Show  basket_items  b3");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "Show  basket_items  b3"));

        writer.write("Show  basket_items  b1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "Show  basket_items  b1"));

        writer.write("Show  basket_items  b5");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "Show  basket_items  b5"));

        // Sensor device Commands
        writer.write("define  device  mic_A1  name  MicrophoneA1  type  microphone  location store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  mic_A1  name  MicrophoneA1  type  microphone  location store_123:aisle_A2"));

        writer.write("define  device  cam_A1  name  CameraA1  type  camera  location store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  cam_A1  name  CameraA1  type  camera  location store_123:aisle_A2"));

        writer.write("define  device  cam_A2  name  CameraA2  type  camera  location store_123:aisle_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  cam_A2  name  CameraA2  type  camera  location store_123:aisle_A1"));

        writer.write("define  device  cam_B2  name  CameraB1  type  camera  location store_123:aisle_B2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  cam_B2  name  CameraB1  type  camera  location store_123:aisle_B2"));

        // Show devices
        writer.write("show  device  cam_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  device  cam_A1"));

        writer.write("show  device  mic_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  device  mic_A1"));

        writer.write("show  device  cam_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  device  cam_A2"));

        // Sensor events
        writer.write("create_event  cam_A1  event  item_added_to_basket b1 prod10");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create_event  cam_A1  event  item_added_to_basket b1 prod10"));

        writer.write("create_event  cam_A1  event  item_added_to_basket b1 prod11");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create_event  cam_A1  event  item_added_to_basket b1 prod11"));

        writer.write("create_event  mic_A1  event  custmer_asked_question cust_S2 \"where can I find the milk?\"");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create_event  mic_A1  event  custmer_asked_question cust_S2 \"where can I find the milk?\""));

        // Appliance device Commands
        writer.write("define  device  rob_1  name  ROBOT_1  type  robot location  store_123:aisle_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  rob_1  name  ROBOT_1  type  robot location  store_123:aisle_A1"));

        writer.write("define  device  rob_2  name  ROBOT_2  type  robot location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  rob_2  name  ROBOT_2  type  robot location  store_123:aisle_A2"));

        writer.write("define  device  spk_1  name  SPEAKER_1  type  speaker location  store_123:aisle_A1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  spk_1  name  SPEAKER_1  type  speaker location  store_123:aisle_A1"));

        writer.write("define  device  spk_2  name  SPEAKER_2  type  speaker location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  spk_2  name  SPEAKER_2  type  speaker location  store_123:aisle_A2"));

        writer.write("define  device  turn_a1  name  TURNSTILE_A1  type  turnstile location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  turn_a1  name  TURNSTILE_A1  type  turnstile location  store_123:aisle_A2"));

        writer.write("define  device  turn_a2  name  TURNSTILE_A2  type  turnstile location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  turn_a2  name  TURNSTILE_A2  type  turnstile location  store_123:aisle_A2"));

        writer.write("define  device  turn_a3  name  TURNSTILE_A3  type  turnstile location  store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "define  device  turn_a3  name  TURNSTILE_A3  type  turnstile location  store_123:aisle_A2"));

        // Show appliance devices
        writer.write("show  device  turn_a1");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  device  turn_a1"));

        writer.write("show  device  rob_2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand("show  device  rob_2"));

        // Appliance events (create event)
        writer.write("create  event  rob_2  event SPILLED_MILK store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  event  rob_2  event SPILLED_MILK store_123:aisle_A2"));

        writer.write("create  event  rob_1  event CUSTOMER_QUESTION \"where is the detergent?\"");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  event  rob_1  event CUSTOMER_QUESTION \"where is the detergent?\""));

        writer.write("create  event  turn_a1  event PRICE_CHECK prod11");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  event  turn_a1  event PRICE_CHECK prod11"));

        // Appliance commands (create command)
        writer.write("create  command  rob_1  message  STOCK_SHELF store_123:aisle_A2:shelf_q3");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  command  rob_1  message  STOCK_SHELF store_123:aisle_A2:shelf_q3"));

        writer.write("create  command  rob_2  message  CLEAN_FLOOR store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  command  rob_2  message  CLEAN_FLOOR store_123:aisle_A2"));

        writer.write("create  command  turn_a3  message  COUNT_INVENTORY store_123:aisle_A2");
        writer.newLine();
        assertDoesNotThrow(() -> commandProcessor.processCommand(
                "create  command  turn_a3  message  COUNT_INVENTORY store_123:aisle_A2"));

        writer.flush();
    }   
}