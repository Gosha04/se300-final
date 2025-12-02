package com.se300.store.model.unit;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.se300.store.model.Aisle;
import com.se300.store.model.AisleLocation;
import com.se300.store.model.Shelf;
import com.se300.store.model.Basket;
import com.se300.store.model.Customer;
import com.se300.store.model.CommandException;
import com.se300.store.model.CustomerAgeGroup;
import com.se300.store.model.CustomerType;
import com.se300.store.model.Inventory;
import com.se300.store.model.InventoryLocation;
import com.se300.store.model.InventoryType;
import com.se300.store.model.Product;
import com.se300.store.model.Sensor;
import com.se300.store.model.ShelfLevel;
import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.model.StoreLocation;
import com.se300.store.model.Temperature;
import com.se300.store.model.User;

/**
 * The ModelUnitTest class contains unit tests for various models used in the Smart Store application.
 * It includes tests for creation, basic operations, and validation of models and enums utilized in the system.
 */
@DisplayName("Model Unit Tests")
public class ModelUnitTest {

    //COMPLETE: Implement Unit Tests for the Smart Store Models

    @Test
    @DisplayName("Test User model creation and getters/setters")
    public void testUserModel() {
        User user = new User();
        user.setEmail("test@store.com");
        user.setPassword("password123");
        user.setName("Test User");

        assertEquals("test@store.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals("Test User", user.getName());

        // Test parameterized constructor
        User user2 = new User("admin@store.com", "adminpass", "Admin User");

        assertEquals("admin@store.com", user2.getEmail());
        assertEquals("adminpass", user2.getPassword());
        assertEquals("Admin User", user2.getName());
    }

    @Test
    @DisplayName("Test Product model creation and getters/setters")
    public void testProductModel() {
        Product product = new Product(
            "p1",
            "Milk",
            "Organic whole milk",
            "1L",
            "Dairy",
            3.99,
            Temperature.refrigerated
        );

        // Validate constructor-set fields
        assertEquals("p1", product.getId());
        assertEquals("Milk", product.getName());
        assertEquals("Organic whole milk", product.getDescription());
        assertEquals("1L", product.getSize());
        assertEquals("Dairy", product.getCategory());
        assertEquals(3.99, product.getPrice());
        assertEquals(Temperature.refrigerated, product.getTemperature());

        // Update using setters
        product.setId("p2");
        product.setName("Cheese");
        product.setDescription("Cheddar cheese block");
        product.setSize("500g");
        product.setCategory("Cheese");
        product.setPrice(5.49);
        product.setTemperature(Temperature.frozen);

        // Validate updated values
        assertEquals("p2", product.getId());
        assertEquals("Cheese", product.getName());
        assertEquals("Cheddar cheese block", product.getDescription());
        assertEquals("500g", product.getSize());
        assertEquals("Cheese", product.getCategory());
        assertEquals(5.49, product.getPrice());
        assertEquals(Temperature.frozen, product.getTemperature());

        // Simple toString check (not strict match, just sanity)
        String s = product.toString();
        assertTrue(s.contains("p2"));
        assertTrue(s.contains("Cheese"));
        assertTrue(s.contains("frozen"));
    }

    @Test
    @DisplayName("Test Customer model creation and getters/setters")
    public void testCustomerModel() {
        Customer customer = new Customer(
            "c1",
            "John",
            "Doe",
            CustomerType.guest,                     
            "john@store.com",
            "0x12345"
        );

        // Validate constructor-set fields
        assertEquals("c1", customer.getId());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
        assertEquals(CustomerType.guest, customer.getType()); 
        assertEquals("john@store.com", customer.getEmail());
        assertEquals("0x12345", customer.getAccountAddress());

        
        assertNull(customer.getAgeGroup());
        assertNull(customer.getStoreLocation());
        assertNull(customer.getLastSeen());
        assertNull(customer.getBasket());

        // Update via setters / assigners
        customer.setId("c2");
        customer.setFirstName("Jane");
        customer.setLastName("Smith");
        CustomerType type = CustomerType.guest;
        customer.setType(type);
        customer.setEmail("jane@store.com");
        customer.setAccountAddress("0xABCDE");

        CustomerAgeGroup ageGroup = CustomerAgeGroup.adult;
        customer.setAgeGroup(ageGroup);

        StoreLocation location = null;
        customer.setStoreLocation(location);

        Date now = new Date();
        customer.setLastSeen(now);

        Basket basket = null; 
        customer.assignBasket(basket);

        // Validate updated values
        assertEquals("c2", customer.getId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("jane@store.com", customer.getEmail());
        assertEquals("0xABCDE", customer.getAccountAddress());
        assertSame(type, customer.getType());
        assertSame(ageGroup, customer.getAgeGroup());
        assertSame(location, customer.getStoreLocation());
        assertSame(now, customer.getLastSeen());
        assertSame(basket, customer.getBasket());

       
        String s = customer.toString();
        assertTrue(s.contains("c2"));
        assertTrue(s.contains("Jane"));
    }

    @Test
    @DisplayName("Test Store model creation and basic operations")
    public void testStoreModel() throws StoreException {
        Store store = new Store("s1", "123 Main St", "Flagship store");

        assertEquals("s1", store.getId());
        assertEquals("123 Main St", store.getAddress());
        assertEquals("Flagship store", store.getDescription());

        store.setId("s2");
        store.setAddress("456 Second St");
        store.setDescription("Updated description");

        assertEquals("s2", store.getId());
        assertEquals("456 Second St", store.getAddress());
        assertEquals("Updated description", store.getDescription());

        //Aisle operations

        // Add aisle
        Aisle aisle = store.addAisle("A1", "Front Aisle", "Entry aisle", null); // AisleLocation can be null
        aisle.addShelf("SH1", "temp", ShelfLevel.high, "sdf", Temperature.ambient);
        assertThrows(StoreException.class, () -> aisle.addShelf("SH1", "temp", ShelfLevel.low, "sdf", Temperature.ambient));
        assertNotNull(aisle);

        // Get aisle that exists
        Aisle fetchedAisle = store.getAisle("A1");
        assertSame(aisle, fetchedAisle);

        assertThrows(StoreException.class,
            () -> store.addAisle("A1", "Duplicate", "Duplicate aisle", null));

        assertThrows(StoreException.class,
            () -> store.getAisle("DOES_NOT_EXIST"));

        assertAll(
            () -> assertDoesNotThrow(() -> aisle.setNumber("12")),
            () -> assertEquals("12", aisle.getNumber()),
            () -> assertDoesNotThrow(() -> aisle.setName("Snacks")),
            () -> assertEquals("Snacks", aisle.getName()),
            () -> assertDoesNotThrow(() -> aisle.setDescription("Chips and crackers")),
            () -> assertEquals("Chips and crackers", aisle.getDescription()),
            () -> assertDoesNotThrow(() -> aisle.setAisleLocation(AisleLocation.floor)),
            () -> assertNotNull(aisle.getAisleLocation()),
            () -> assertEquals(AisleLocation.floor, aisle.getAisleLocation())
        );

        //Customer operations

        Customer customer = new Customer(
            "c1",
            "John",
            "Doe",
            CustomerType.guest,
            "john@store.com",
            "0x123"
        );

        // Add customer
        store.addCustomer(customer);
        Customer retrieved = store.getCustomer("c1");
        assertSame(customer, retrieved);

        assertThrows(StoreException.class,
            () -> store.addCustomer(customer));

        
        assertNull(store.getCustomer("unknown"));

        Customer unknown = new Customer(
            "unknown",
            "Ghost",
            "User",
            CustomerType.guest,
            "ghost@store.com",
            "0x999"
        );
        store.removeCustomer(unknown);
        assertSame(customer, store.getCustomer("c1"));

        
        String s = store.toString();
        assertTrue(s.contains("s2"));
        assertTrue(s.contains("456 Second St"));

        Sensor sen = new Sensor("Sensor", "No", null, "camera");
        store.addDevice(sen);

        assertAll(
            () -> assertDoesNotThrow(() -> sen.setName("Collin")),
            () -> assertDoesNotThrow(() -> sen.setId("a")),
            () -> assertDoesNotThrow(() -> sen.setStoreLocation(null)),
            () -> assertDoesNotThrow(() -> sen.setType("camera"))
        );

        Inventory inv = new Inventory(s, null, 0, 0, null, null);
        Sensor dev = new Sensor("d1", null, null, null);
        assertAll(
            () -> assertDoesNotThrow(() -> inv.setId(" ")),
            () -> assertDoesNotThrow(() -> inv.setInventoryLocation(new InventoryLocation("s1", "A1", "SH1"))),
            () -> assertNotNull(inv.getInventoryLocation()),
            () -> assertDoesNotThrow(() -> inv.setCapacity(0)),
            () -> assertDoesNotThrow(() -> inv.setProductId(null)),
            () -> assertDoesNotThrow(() -> inv.setType(InventoryType.standard)),
            () -> assertNotNull(inv.getType())
        );

        assertAll(
            () -> assertDoesNotThrow(() -> store.addInventory(inv)),
            () -> assertThrows(StoreException.class, () -> store.addInventory(inv)),

            () -> assertDoesNotThrow(() -> store.addDevice(dev)),
            () -> assertThrows(StoreException.class, () -> store.addDevice(dev))
        );
    }

    @Test
    @DisplayName("Test Basket model operations")
    public void testBasketModel() throws StoreException{
        Basket basket = new Basket("b1");
        assertEquals("b1", basket.getId());

        basket.setId("b2");
        assertEquals("b2", basket.getId());

        Store store = new Store("s1", "123 Main St", "Test store");
        basket.setStore(store);
        assertSame(store, basket.getStore());

        Customer customer = new Customer(
            "c1",
            "John",
            "Doe",
            CustomerType.guest,
            "john@store.com",
            "0x123"
        );
        basket.setCustomer(customer);
        assertSame(customer, basket.getCustomer());


        assertThrows(StoreException.class,
            () -> basket.removeProduct("pX", 1));

        Basket guestBasket = new Basket("b3");
        Customer guest = new Customer(
            "c2",
            "Guest",
            "User",
            CustomerType.guest,
            "guest@store.com",
            "0x999"
        );
        guestBasket.setCustomer(guest);
        store.addBasket(guestBasket);
        assertThrows(StoreException.class, () -> store.addBasket(guestBasket));
        assertThrows(StoreException.class,
            () -> guestBasket.addProduct("p1", 1));

        Basket basketToClear = new Basket("b4");
        Customer owner = new Customer(
            "c3",
            "Owner",
            "User",
            CustomerType.guest,
            "owner@store.com",
            "0xABC"
        );
        owner.assignBasket(basketToClear);
        basketToClear.setCustomer(owner);

        basketToClear.clearBasket();

        assertNull(basketToClear.getCustomer());
        assertNull(owner.getBasket());

        String s = basket.toString();
        assertTrue(s.contains("b2"));
    }

    @Test
    @DisplayName("Test StoreLocation model")
    public void testStoreLocationModel() {
        // Create store location
        StoreLocation location = new StoreLocation("store123", "aisle9");

        assertEquals("store123", location.getStoreId());
        assertEquals("aisle9", location.getAisleId());

        location.setStoreId("store456");
        location.setAisleId("aisle3");

        assertEquals("store456", location.getStoreId());
        assertEquals("aisle3", location.getAisleId());

        String s = location.toString();
        assertTrue(s.contains("store456"));
        assertTrue(s.contains("aisle3"));
    }

    @Test
    @DisplayName("Test Temperature enum")
    public void testTemperatureEnum() {

        Temperature[] temps = Temperature.values();

        assertEquals(5, temps.length);
        assertArrayEquals(
            new Temperature[]{
                    Temperature.frozen,
                    Temperature.refrigerated,
                    Temperature.ambient,
                    Temperature.warm,
                    Temperature.hot
            },
            temps
        );

        assertEquals(Temperature.frozen, Temperature.valueOf("frozen"));
        assertEquals(Temperature.hot, Temperature.valueOf("hot"));

        assertEquals(0, Temperature.frozen.ordinal());
        assertEquals(4, Temperature.hot.ordinal());
    }

    @Test
    @DisplayName("Test CustomerType enum")
    public void testCustomerTypeEnum() {
        CustomerType[] types = CustomerType.values();

        assertEquals(2, types.length);

        assertArrayEquals(
            new CustomerType[] {
                    CustomerType.guest,
                    CustomerType.registered
            },
            types
        );

        assertEquals(CustomerType.guest, CustomerType.valueOf("guest"));
        assertEquals(CustomerType.registered, CustomerType.valueOf("registered"));

        assertEquals(0, CustomerType.guest.ordinal());
        assertEquals(1, CustomerType.registered.ordinal());
    }

    @Test
    @DisplayName("Test Store Exception")
    public void testStoreException() throws StoreException {
        StoreException ex = new StoreException("Add Product", "Not enough inventory");

        assertEquals("Add Product", ex.getAction());
        assertEquals("Not enough inventory", ex.getReason());

        assertTrue(ex instanceof Exception);

        ex.setAction("Update Inventory");
        ex.setReason("Capacity exceeded");

        assertEquals("Update Inventory", ex.getAction());
        assertEquals("Capacity exceeded", ex.getReason());

        InventoryLocation inv = new InventoryLocation(null, null, null);
        assertDoesNotThrow(() -> inv.setShelfId("SH1"));
        assertNotNull(inv.getShelfId());

        CommandException commandException = new CommandException(null, null);
        assertDoesNotThrow(() -> commandException.setCommand("sds"));
        assertNotNull(commandException.getCommand());
        assertDoesNotThrow(() -> commandException.setReason("sdfs"));

        Aisle aisle = new Aisle("A1", "Front Aisle", "Entry aisle", null);

        aisle.setNumber("01");
        aisle.setName("Dairy");
        aisle.setDescription("Cold stuff");
        aisle.setAisleLocation(AisleLocation.floor);

        assertAll(
            () -> assertEquals("01", aisle.getNumber()),
            () -> assertEquals("Dairy", aisle.getName()),
            () -> assertEquals("Cold stuff", aisle.getDescription()),
            () -> assertSame(AisleLocation.floor, aisle.getAisleLocation())
        );

        Shelf shelf = new Shelf("S1", "Snacks", ShelfLevel.low, "Chips", Temperature.ambient);

        shelf.setId("S2");
        shelf.setName("Drinks");
        shelf.setLevel(ShelfLevel.high);
        shelf.setDescription("Sodas");
        shelf.setTemperature(Temperature.frozen);

        assertAll(
            () -> assertEquals("S2", shelf.getId()),
            () -> assertEquals("Drinks", shelf.getName()),
            () -> assertEquals(ShelfLevel.high, shelf.getLevel()),
            () -> assertEquals("Sodas", shelf.getDescription()),
            () -> assertEquals(Temperature.frozen, shelf.getTemperature())
        );
    }
}
