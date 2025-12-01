package com.se300.store.service.integration;

import com.se300.store.data.DataManager;
import com.se300.store.model.*;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This class contains integration tests for verifying the correct functionality
 * of various service workflows in the Smart Store system.
 */
@DisplayName("Service Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiceIntegrationTest {

    //COMPLETE: Implement Integration Tests for the Smart Store Services

    private static StoreService storeService;
    private static AuthenticationService authenticationService;
    private static DataManager dataManager;

    @BeforeAll
    public static void setUpClass() {
        dataManager = DataManager.getInstance();
        dataManager.clear();
        dataManager.put("stores", new HashMap<String, Store>());
        StoreRepository storeRepository = new StoreRepository(dataManager);
        UserRepository userRepository = new UserRepository(dataManager);

        storeService = new StoreService(storeRepository);
        authenticationService = new AuthenticationService(userRepository);
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Complete Store workflow - provision, show, update, delete")
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
    @Order(2)
    @DisplayName("Integration: Store with Aisles and Shelves")
    public void testStoreWithAislesAndShelves() throws StoreException {
        storeService.provisionStore("S2","Groc","45","t");
        Aisle a = storeService.provisionAisle("S2","A1","Fresh","Produce",null,"t");
        assertEquals("A1",a.getNumber());
        Shelf sh = storeService.provisionShelf("S2","A1","SH1","Top",ShelfLevel.high,"Cool",Temperature.ambient,"t");
        assertEquals("SH1",sh.getId());
        assertNotNull(storeService.showShelf("S2","A1","SH1","t"));
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Product and Inventory workflow")
    public void testProductAndInventoryWorkflow() throws StoreException {
        storeService.provisionStore("S3","HW","222","t");
        storeService.provisionAisle("S3","A1","Gen","Tools",null,"t");
        storeService.provisionShelf("S3","A1","SH1","Mid",ShelfLevel.medium,"Shelf",Temperature.ambient,"t");

        Product p = storeService.provisionProduct("P1","Hammer","Strong","1pc","Tools",9.99,Temperature.ambient,"t");
        assertEquals("P1",p.getId());

        Inventory inv = storeService.provisionInventory("I1","S3","A1","SH1",10,5,"P1",InventoryType.standard,"t");
        assertEquals("I1",inv.getId());

        Inventory before = storeService.showInventory("I1","t");
        assertEquals(5, before.getCount());

        storeService.updateInventory("I1",1,"t");

        Inventory after = storeService.showInventory("I1","t");
        assertEquals(6, after.getCount());
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Customer and Basket workflow")
    public void testCustomerAndBasketWorkflow() throws StoreException {
        storeService.provisionStore("S4", "Market", "55 Way", "token");
        storeService.provisionAisle("S4", "A1", "Items", "General", null, "token");

        
        Customer customer = storeService.provisionCustomer(
                "C1", "John", "Doe", CustomerType.registered, "john@mail.com", "addr", "token");
        assertNotNull(customer);

        
        Customer updated = storeService.updateCustomer("C1", "S4", "A1", "token");
        assertNotNull(updated.getStoreLocation());
        assertEquals("S4", updated.getStoreLocation().getStoreId());
        assertEquals("A1", updated.getStoreLocation().getAisleId());
        assertNotNull(updated.getLastSeen());

        
        Basket basket = storeService.provisionBasket("B1", "token");
        assertNotNull(basket);
        Basket assigned = storeService.assignCustomerBasket("C1", "B1", "token");
        assertEquals("B1", assigned.getId());

        // Retrieve customer's basket and verify links
        Basket customerBasket = storeService.getCustomerBasket("C1", "token");
        assertEquals("B1", customerBasket.getId());
        assertNotNull(customerBasket.getCustomer());
        assertEquals("C1", customerBasket.getCustomer().getId());
        assertNotNull(customerBasket.getStore());
        assertEquals("S4", customerBasket.getStore().getId());
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Authentication service full workflow")
    public void testAuthenticationWorkflow() {
        Collection<User> initialUsers = authenticationService.getAllUsers();
        int initialSize = initialUsers.size();

        
        User user = authenticationService.registerUser("abc@test.com", "123", "ABC");
        assertNotNull(user);
        assertTrue(authenticationService.userExists("abc@test.com"));

        User fetched = authenticationService.getUserByEmail("abc@test.com");
        assertNotNull(fetched);
        assertEquals("ABC", fetched.getName());
        assertEquals("123", fetched.getPassword());

        
        User updated = authenticationService.updateUser("abc@test.com", "456", "New Name");
        assertNotNull(updated);
        assertEquals("New Name", updated.getName());
        assertEquals("456", updated.getPassword());

       
        Collection<User> usersAfterAdd = authenticationService.getAllUsers();
        assertTrue(usersAfterAdd.size() >= initialSize + 1);

    
        boolean deleted = authenticationService.deleteUser("abc@test.com");
        assertTrue(deleted);
        assertFalse(authenticationService.userExists("abc@test.com"));
        assertNull(authenticationService.getUserByEmail("abc@test.com"));
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Device provisioning and events")
    public void testDeviceProvisioningAndEvents() throws StoreException {
        
        storeService.provisionStore("S5", "Electronics", "88 Tech Rd", "token");
        storeService.provisionAisle("S5", "A1", "Sensors", "Devices", null, "token");

        String deviceType = SensorType.values()[0].name();

        Device device = storeService.provisionDevice("D1", "TempSensor", deviceType, "S5", "A1", "token");
        assertNotNull(device);
        assertEquals("D1", device.getId());

        assertDoesNotThrow(() -> storeService.raiseEvent("D1", "READ", "token"));

        assertThrows(StoreException.class, () -> storeService.issueCommand("BAD", "ON", "token"));
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Error handling across services")
    public void testErrorHandling() {
        assertThrows(StoreException.class, () -> storeService.showStore("NO_SUCH_STORE", "token"));
        assertThrows(StoreException.class, () -> storeService.showProduct("NO_SUCH_PRODUCT", "token"));
        assertThrows(StoreException.class, () -> storeService.showCustomer("NO_SUCH_CUSTOMER", "token"));

        assertThrows(StoreException.class,
                () -> storeService.provisionAisle("BAD_STORE", "AX", "Name", "Desc", null, "token"));

        assertThrows(StoreException.class,
                () -> storeService.assignCustomerBasket("BAD_CUSTOMER", "B9", "token"));

        assertNull(authenticationService.updateUser("missing@test.com", "pw", "Name"));
        assertFalse(authenticationService.deleteUser("missing@test.com"));
    }
}
