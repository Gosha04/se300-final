package com.se300.store;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;

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
    }

    @Test
    @Order(6)
    @DisplayName("E2E: Error handling across all layers")
    public void testCompleteErrorHandling() {
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Data consistency across all layers")
    public void testDataConsistencyAcrossLayers() {
    }

    @Test
    @Order(8)
    @DisplayName("E2E: REST API Controller - Store CRUD operations")
    public void testRestApiStoreOperations() {
    }

    @Test
    @Order(9)
    @DisplayName("E2E: REST API Controller - User CRUD operations")
    public void testRestApiUserOperations() {
    }

    @Test
    @Order(10)
    @DisplayName("E2E: REST API Controller - Error handling")
    public void testRestApiErrorHandling() {
    }

    @Test
    @Order(11)
    @DisplayName("E2E: Final cleanup and deletion operations")
    public void testFinalCleanupOperations() throws StoreException {
    }

    @Test
    @Order(12)
    @DisplayName("E2E: Complete store.script data processing with assertions")
    public void testStoreScriptEndToEnd() throws Exception {
    }
}