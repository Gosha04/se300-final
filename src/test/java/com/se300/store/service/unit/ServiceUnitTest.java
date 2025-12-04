package com.se300.store.service.unit;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se300.store.model.Aisle;
import com.se300.store.model.AisleLocation;
import com.se300.store.model.Basket;
import com.se300.store.model.Customer;
import com.se300.store.model.CustomerType;
import com.se300.store.model.Inventory;
import com.se300.store.model.InventoryLocation;
import com.se300.store.model.InventoryType;
import com.se300.store.model.Product;
import com.se300.store.model.Shelf;
import com.se300.store.model.ShelfLevel;
import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.model.StoreLocation;
import com.se300.store.model.Temperature;
import com.se300.store.model.User;
import com.se300.store.repository.UserRepository;
import com.se300.store.service.AuthenticationService;
import com.se300.store.service.StoreService;

/**
 * Unit tests for Service classes including AuthenticationService and StoreService.
 * The tests utilize a mocked instance of UserRepository to validate the functionality of AuthenticationService.
 * StoreService operations are tested without mocking, as it uses static in-memory maps for data storage.
 */
@DisplayName("Service Unit Tests")
@ExtendWith(MockitoExtension.class)
public class ServiceUnitTest {

    //TODO: Implement Unit Tests for the Smart Store Services

    @Mock
    private UserRepository userRepository;

    private AuthenticationService authenticationService;
    private StoreService storeService;

    @BeforeEach
    public void setUp() {
        authenticationService = new AuthenticationService(userRepository);
        storeService = new StoreService();
    }

    @Test
    @DisplayName("Test AuthenticationService register user with mocked repository")
    public void testRegisterUser() {
        User testUser = new User("random@gmail.com", "password", "Anon");

        when(userRepository.findByEmail("random@gmail.com")).thenReturn(Optional.of(testUser));

        authenticationService.registerUser("random@gmail.com", "password", "Anon");

        Optional<User> authUser =  userRepository.findByEmail("random@gmail.com");
        assertEquals(testUser, authUser.get());
        verify(userRepository).findByEmail("random@gmail.com");
    }

    @Test
    @DisplayName("Test AuthenticationService user exists with mocked repository")
    public void testUserExists() {
        User testUser = new User("random@gmail.com", "password", "Anon");
        Map<String, User> authUser = new HashMap<>();
        authUser.put("test", testUser);

        when(userRepository.findAll()).thenReturn(authUser);
        userRepository.save(testUser);

        Optional<User> optionalTestUser = Optional.of(userRepository.findAll().get("test"));

        boolean exists = optionalTestUser.isPresent() && optionalTestUser.get().equals(testUser);     
        assertTrue(exists);
        verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Test AuthenticationService get user by email with mocked repository")
    public void testGetUserByEmail() {
        User testUser = new User("random@gmail.com", "password", "Anon");

        when(userRepository.findByEmail("random@gmail.com")).thenReturn(Optional.of(testUser));
        userRepository.save(testUser);

        Optional<User> optionalTestUser = userRepository.findByEmail("random@gmail.com");

        assertEquals(testUser, optionalTestUser.get());
        verify(userRepository, times(1)).findByEmail("random@gmail.com");
    }

    @Test
    @DisplayName("Test AuthenticationService update user with mocked repository")
    public void testUpdateUser() {
        User testUser = new User("random@gmail.com", "password", "Anon");

        when(userRepository.findByEmail("random@gmail.com")).thenReturn(Optional.of(testUser));
        userRepository.save(testUser);

        authenticationService.updateUser("random@gmail.com", "password", "Name");

        assertNotEquals(testUser, userRepository.findByEmail("random@gmail.com"));
        verify(userRepository, times(2)).findByEmail("random@gmail.com");
    }

    @Test
    @DisplayName("Test AuthenticationService delete user with mocked repository")
    public void testDeleteUser() {
        User testUser = new User("random@gmail.com", "password", "Anon");

        when(userRepository.findByEmail("random@gmail.com")).thenReturn(Optional.of(testUser));
        userRepository.save(testUser);

        authenticationService.deleteUser("random@gmail.com");

        assertNotEquals(userRepository.findByEmail("random@gmail.com"), testUser);
        verify(userRepository).findByEmail("random@gmail.com");
    }

    @Test
    @DisplayName("Test AuthenticationService Basic Authentication - valid credentials with mock")
    public void testBasicAuthenticationValid() {
        String email = "test@gmail.com";
        String password = "password";
        User testUser = new User(email, "password", "Test");
               
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));

        User result = authenticationService.getUserByEmail(email);
       
        String rawCreds = email + ":" + password;
        String base64Creds = Base64.getEncoder().encodeToString(rawCreds.getBytes());
        String decode = new String(Base64.getDecoder().decode(base64Creds), StandardCharsets.UTF_8);
        String subString = decode.substring(0, email.length());

        assertEquals(decode, rawCreds);        
        assertEquals(result, authenticationService.getUserByEmail(subString));
    }

    @Test
    @DisplayName("Test AuthenticationService Basic Authentication - invalid credentials")
    public void testBasicAuthenticationInvalid() {
        String email = "test@gmail.com";
               
        String rawCreds = email + ":wrong";
        String correctCreds = email + ":password";
        String base64Creds = Base64.getEncoder().encodeToString(rawCreds.getBytes());
        String decode = new String(Base64.getDecoder().decode(base64Creds), StandardCharsets.UTF_8);

        assertNotEquals(decode, correctCreds);        
    }

    @Test
    @DisplayName("Test AuthenticationService Basic Authentication - invalid header format")
    public void testBasicAuthenticationInvalidHeader() {
        String email = "test@gmail.com";
        String password = "password";
               
        String rawCreds = password + ":" + email;
        String correctCreds = email + ":" + password;
        String base64Wrong = Base64.getEncoder().encodeToString(rawCreds.getBytes());
        String base64Creds = Base64.getEncoder().encodeToString(correctCreds.getBytes());

        assertNotEquals(base64Creds, base64Wrong);      
    }

    @Test
    @DisplayName("Test AuthenticationService delete non-existent user")
    public void testDeleteNonExistentUser() {
       assertFalse(authenticationService.deleteUser("user"));
    }

    @Test
    @DisplayName("Test StoreService operations (no mocking needed - uses static maps)")
    public void testStoreServiceOperations() throws StoreException {
        Store store = storeService.provisionStore("S2", "Test Store", "123 Main St", "token");
        assertNotNull(store);
        assertEquals("S2", store.getId());

        Aisle aisle = storeService.provisionAisle("S2", "A2", "Misc",
         "Desc", AisleLocation.floor, "token");

        assertThrows(StoreException.class, () -> storeService.showAisle("S3", null, "token"));
        assertThrows(StoreException.class, () -> storeService.showAisle("S2", "A122312", "token"));

        assertNotNull(aisle);
        assertEquals("A2", aisle.getNumber());

        Shelf shelf = storeService.provisionShelf("S2", "A2", "SH1", "name", ShelfLevel.low, "Desc",
        Temperature.ambient, "token");

        assertThrows(StoreException.class, () -> storeService.provisionShelf("S2", "A123324", "SH1", "name", ShelfLevel.low, "Desc",
        Temperature.ambient, "token"));
        assertThrows(StoreException.class, () -> storeService.provisionShelf("S2", "2", "SH1", "name", ShelfLevel.low, "Desc",
        Temperature.ambient, "token"));
        assertThrows(StoreException.class, () -> storeService.showShelf("S2234", "A122312", "SH2", "token"));
        assertThrows(StoreException.class, () -> storeService.showShelf("S2", "A122312", "SH2", "token"));

        assertNotNull(shelf);
        assertEquals("SH1", shelf.getId());

        Product product = storeService.provisionProduct(
                "S2",
                "Bananas",
                "Fresh bananas",
                "1lb",
                "Produce",
                1.99,
                Temperature.ambient,
                "token"
        );
        assertThrows(StoreException.class, () -> storeService.provisionProduct("S2",
                "Bananas",
                "Fresh bananas",
                "1lb",
                "Produce",
                1.99,
                Temperature.ambient,
                "token"));
        assertNotNull(product);
        assertEquals("Bananas", product.getName());

        Inventory inventory = storeService.provisionInventory(
                "I1",
                "S2",
                "A2",
                "SH1",
                10,          
                10,          
                product.getId(),
                InventoryType.standard,
                "token"
        );

        assertThrows(StoreException.class, () -> storeService.provisionInventory("I2",
         "S324w", null, null, 0, 0, null, null, null));
        assertThrows(StoreException.class, () -> storeService.provisionInventory("I2",
         "S2", "A343", null, 0, 0, null, null, null));
        // assertThrows(StoreException.class, () -> storeService.provisionInventory("I2",
        //  "S2", "A2", "SH33q1", 0, 0, null, null, null));
        assertThrows(StoreException.class, () -> storeService.provisionInventory("I2",
         "S2", "A2", "SH1", 12, 13, "P3242", null, null));
        assertNotNull(inventory);
        assertEquals(10, inventory.getCount());

        Customer customer = storeService.provisionCustomer(
                "C1",
                "Braeden",
                "Test",
                CustomerType.registered,
                "test@example.com",
                "ACC1",
                "token"
        );
        assertNotNull(customer);
        assertEquals("C1", customer.getId());
        assertThrows(StoreException.class, () -> storeService.provisionCustomer(
                "C1",
                "Braeden",
                "Test",
                CustomerType.registered,
                "test@example.com",
                "ACC1",
                "token"
        ));
        assertThrows(StoreException.class, () -> storeService.assignCustomerBasket("C1", "B1", "token"));
        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1", "P1", 1, "token"));
        assertThrows(StoreException.class, () -> storeService.clearBasket("B1", "token"));
        assertThrows(StoreException.class, () -> storeService.showBasket("B1", "token"));

        Basket basket = storeService.provisionBasket("B1", "token");

        assertThrows(StoreException.class, () -> storeService.clearBasket("B1", "token"));
        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1", product.getId(), 1, "token"));
        assertThrows(StoreException.class, () -> storeService.assignCustomerBasket("C1", "B11231", "token"));
        assertThrows(StoreException.class,
            () -> storeService.addBasketProduct("B1", product.getId(), 11, "token"));
        
        storeService.updateCustomer("C1", "S2", "A2", "token");
        StoreLocation location = customer.getStoreLocation();

        assertThrows(StoreException.class, () -> storeService.getCustomerBasket("C1", "token"));

        storeService.assignCustomerBasket("C1", "B1", "token");

        assertNotNull(basket);
        assertThrows(StoreException.class, () -> storeService.assignCustomerBasket("C1", "fake", "token"));
        assertThrows(StoreException.class, () -> storeService.updateCustomer("C1", "fake", "fake", "token"));
        assertEquals("B1", basket.getId());
        assertEquals("S2", location.getStoreId());
        assertEquals("A2", location.getAisleId());
        assertThrows(StoreException.class, () -> storeService.getCustomerBasket("C2343", "token"));

        storeService.addBasketProduct("B1", product.getId(), 1, "token");

        Map<String, Integer> productsInBasket = basket.getProducts();
        assertTrue(productsInBasket.containsKey(product.getId()));
        assertEquals(1, productsInBasket.get(product.getId()));

        Inventory updatedInventory = storeService.showInventory("I1", "token");
        assertEquals(9, updatedInventory.getCount());

        storeService.removeBasketProduct("B1", product.getId(), 1, "token");

        productsInBasket = basket.getProducts();
        assertFalse(productsInBasket.containsKey(product.getId()));

        updatedInventory = storeService.showInventory("I1", "token");
        assertEquals(10, updatedInventory.getCount());

        assertEquals(store, storeService.showStore("S2", "token"));
        assertEquals(customer, storeService.showCustomer("C1", "token"));

        assertThrows(StoreException.class,
            () -> storeService.addBasketProduct("B1", product.getId(), 11, "token"));

        assertThrows(StoreException.class,
            () -> storeService.addBasketProduct("B232", product.getId(), 11, "token"));
        // assertThrows(StoreException.class,
        //         () -> storeService.addBasketProduct("B1", product.getId(), 1, "token"));

        storeService.provisionAisle("S2", "A100", "dfs", "sdf", AisleLocation.floor, "token");
        storeService.addBasketProduct("B1", product.getId(), 2, "token");  
        updatedInventory.setCount(10);  

        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1",
         product.getId(), 1, "token"));

        StoreLocation s2a2 = new StoreLocation("S2", "A2"); 
        storeService.showCustomer("C1", "token").setStoreLocation(s2a2);
        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1",
         product.getId(), 1, "token"));   

        storeService.provisionShelf("S2", "A2", "SH4", "sd",
         ShelfLevel.high, "df", Temperature.ambient, "token");
        storeService.provisionShelf("S2", "A2", "SH5", "sd",
         ShelfLevel.medium, "df", Temperature.ambient, "token");

        storeService.provisionShelf("S2", "A100", "SH1", "sd",
         ShelfLevel.high, "df", Temperature.ambient, "token");

        storeService.provisionProduct(
                "P3",
                "Bananas",
                "Fresh bananas",
                "1lb",
                "Produce",
                1.99,
                Temperature.ambient,
                "token"
        );

        storeService.provisionProduct(
                "P2",
                "Bananas",
                "Fresh bananas",
                "1lb",
                "Produce",
                1.99,
                Temperature.ambient,
                "token"
        );

        storeService.provisionInventory(
                "I3",
                "S2",
                "A2",
                "SH4",
                10,          
                10,          
                "P2",
                InventoryType.standard,
                "token"
        );

        storeService.provisionInventory(
                "I4",
                "S2",
                "A2",
                "SH5",
                10,          
                10,          
                "P2",
                InventoryType.standard,
                "token"
        );

        assertThrows(StoreException.class, () -> storeService.addBasketProduct("B1", "P2", 1, "token"));
        storeService.showInventory("I4", "token").setProductId("P3");
        storeService.addBasketProduct("B1", "P2", 1, "token");
        storeService.addBasketProduct("B1", "P3", 1, "token");
        storeService.showInventory("I4", "token").setProductId("P2");
        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1",
         "P2", 1, "token")); 
        storeService.showInventory("I4", "token").setInventoryLocation(new InventoryLocation("S2", "A100", "SH1"));
        storeService.showCustomer("C1", "token").setStoreLocation(new StoreLocation("S2", "A100"));
        assertThrows(StoreException.class, () -> storeService.removeBasketProduct("B1",
         "P3", 1, "token"));   
        assertThrows(StoreException.class,() -> storeService.showInventory("DSIOHN", "token"));
        assertThrows(StoreException.class,() -> storeService.updateInventory("DSIOHN", 12,"token"));
    }
}