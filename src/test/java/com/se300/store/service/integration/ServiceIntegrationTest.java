package com.se300.store.service.integration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.se300.store.data.DataManager;
import com.se300.store.model.Aisle;
import com.se300.store.model.AisleLocation;
import com.se300.store.model.Basket;
import com.se300.store.model.Customer;
import com.se300.store.model.CustomerType;
import com.se300.store.model.Device;
import com.se300.store.model.Inventory;
import com.se300.store.model.InventoryType;
import com.se300.store.model.Product;
import com.se300.store.model.SensorType;
import com.se300.store.model.Shelf;
import com.se300.store.model.ShelfLevel;
import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.model.Temperature;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;

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
        Store created = storeService.provisionStore("S1", "Main Store", "123 Road", "admin");
        assertNotNull(created);
        assertEquals("S1", created.getId());

        Store shown = storeService.showStore("S1", "admin");
        assertEquals("Main Store", shown.getDescription());

        Store updated = storeService.updateStore("S1", "Updated Desc", "999 New St");
        assertEquals("Updated Desc", updated.getDescription());
        assertEquals("999 New St", updated.getAddress());
        assertThrows(StoreException.class, () -> storeService.updateStore("S33434", "no", "no"));

        storeService.deleteStore("S1");
        assertThrows(StoreException.class, () -> storeService.deleteStore("S1"));
        assertThrows(StoreException.class, () -> storeService.showStore("S1", "admin"));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Store with Aisles and Shelves")
    public void testStoreWithAislesAndShelves() throws StoreException {
        storeService.provisionStore("S1", "Main Store", "123 Road", "admin");
        Aisle a = storeService.provisionAisle("S1","A1","Fresh","Produce", AisleLocation.floor,"t");
        assertEquals("A1",a.getNumber());
        Shelf sh = storeService.provisionShelf("S1","A1","SH1","Top",ShelfLevel.high,"Cool",Temperature.ambient,"t");
        assertEquals("SH1",sh.getId());
        assertNotNull(storeService.showShelf("S1","A1","SH1","t"));
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Product and Inventory workflow")
    public void testProductAndInventoryWorkflow() throws StoreException {
        Product p = storeService.provisionProduct("P1","Hammer","Strong","1pc","Tools",9.99,Temperature.ambient,"t");
        assertEquals("P1",p.getId());

        Inventory inv = storeService.provisionInventory("I1","S1","A1","SH1",10,5,"P1",InventoryType.standard,"t");
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
        String customerId = "C11";
        String basketId   = "B11";

        Customer customer = storeService.provisionCustomer(
                customerId, "John", "Doe", CustomerType.registered,
                "john+integration@mail.com", "addr", "admin");
        assertNotNull(customer);

        // S1 / A1 are still assumed to exist from earlier tests in this class
        Customer updated = storeService.updateCustomer(customerId, "S1", "A1", "admin");
        assertNotNull(updated.getStoreLocation());
        assertEquals("S1", updated.getStoreLocation().getStoreId());
        assertEquals("A1", updated.getStoreLocation().getAisleId());
        assertNotNull(updated.getLastSeen());

        Basket basket = storeService.provisionBasket(basketId, "admin");
        assertNotNull(basket);

        Basket assigned = storeService.assignCustomerBasket(customerId, basketId, "admin");
        assertEquals(basketId, assigned.getId());


        Basket customerBasket = storeService.getCustomerBasket(customerId, "admin");
        assertEquals(basketId, customerBasket.getId());
        assertNotNull(customerBasket.getCustomer());
        assertEquals(customerId, customerBasket.getCustomer().getId());
        assertNotNull(customerBasket.getStore());
        assertEquals("S1", customerBasket.getStore().getId());  

        assertDoesNotThrow(() -> storeService.addBasketProduct("B11", "P1", 1, "token" ));
        assertDoesNotThrow(() -> storeService.removeBasketProduct(basketId, "P1", 1, "token"));

        storeService.provisionStore("S3", "test", "add", "admin");
        storeService.provisionAisle("S3", "A33", "aisle", "dad", AisleLocation.floor, "admin");
        assertDoesNotThrow(() ->  storeService.updateCustomer("C11", "S3", "A33", "admin"));
        assertNull(storeService.showBasket("B11", "token").getCustomer());
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
        String deviceType = SensorType.values()[0].name();

        Device device = storeService.provisionDevice("D1", "TempSensor", deviceType, "S1", "A1", "admin");
        assertNotNull(device);
        assertEquals("D1", device.getId());
        assertThrows(StoreException.class, () -> storeService.provisionDevice("D1", "fake", deviceType, "Sr3", "sdfs", "admin"));
        assertThrows(StoreException.class, () -> storeService.provisionDevice("D1", "fake", deviceType, "S1", "sdfs", "admin"));
        assertThrows(StoreException.class, () -> storeService.provisionDevice("D1", "fake", deviceType, "S1", "A1", "admin"));

        assertDoesNotThrow(() -> storeService.raiseEvent("D1", "READ", "admin"));
        assertThrows(StoreException.class, () -> storeService.raiseEvent("D3432", "READ", "admin"));

        assertThrows(StoreException.class, () -> storeService.issueCommand("BAD", "ON", "admin"));
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Error handling across services")
    public void testErrorHandling() {
        assertThrows(StoreException.class, () -> storeService.showStore("NO_SUCH_STORE", "admin"));
        assertThrows(StoreException.class, () -> storeService.showProduct("NO_SUCH_PRODUCT", "admin"));
        assertThrows(StoreException.class, () -> storeService.showCustomer("NO_SUCH_CUSTOMER", "admin"));

        assertThrows(StoreException.class,
                () -> storeService.provisionAisle("BAD_STORE", "AX", "Name", "Desc", null, "admin"));

        assertThrows(StoreException.class,
                () -> storeService.assignCustomerBasket("BAD_CUSTOMER", "B9", "admin"));

        assertNull(authenticationService.updateUser("missing@test.com", "pw", "Name"));
        assertFalse(authenticationService.deleteUser("missing@test.com"));
    }
}
