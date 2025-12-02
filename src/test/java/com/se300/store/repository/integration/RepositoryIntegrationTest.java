package com.se300.store.repository.integration;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import org.junit.jupiter.api.*;

import java.util.Map;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

/**
 * RepositoryIntegrationTest is designed to perform integration tests
 * for repository classes, ensuring their functionality and verifying
 * operations such as persistence, updates, deletions, and concurrency.
 */
@DisplayName("Repository Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RepositoryIntegrationTest {

    //COMPLETEs: Implement Integration Tests for the Smart Store Repositories

    private static DataManager dataManager;
    private static StoreRepository storeRepository;
    private static UserRepository userRepository;

    @BeforeAll
    public static void setUpClass() {
        dataManager = DataManager.getInstance();
        dataManager.clear();
        dataManager.put("stores", new HashMap<String, Store>());
        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);
    }

    @Test
    @Order(1)
    @DisplayName("Integration: Save multiple stores and verify persistence")
    public void testSaveMultipleStores() {
        Store s1 = new Store("store1", "123 Main", "First store");
        Store s2 = new Store("store2", "456 High", "Second store");

        storeRepository.save(s1);
        storeRepository.save(s2);

        Map<String, Store> allStores = storeRepository.findAll();
        assertEquals(2, allStores.size());
        assertSame(s1, allStores.get("store1"));
        assertSame(s2, allStores.get("store2"));

        
        Map<String, Store> storedInDataManager = dataManager.get("stores");
        assertNotNull(storedInDataManager);
        assertEquals(2, storedInDataManager.size());
        assertSame(s1, storedInDataManager.get("store1"));
        assertSame(s2, storedInDataManager.get("store2"));
    }

    @Test
    @Order(2)
    @DisplayName("Integration: Update store and verify changes")
    public void testUpdateStore() {
        
        Store existing = storeRepository.findById("store1").orElseThrow();
        assertEquals("123 Main", existing.getAddress());

        existing.setAddress("999 New Address");
        existing.setDescription("Updated store description");
        storeRepository.save(existing);

        Store updated = storeRepository.findById("store1").orElseThrow();
        assertEquals("999 New Address", updated.getAddress());
        assertEquals("Updated store description", updated.getDescription());

        @SuppressWarnings("unchecked")
        Map<String, Store> storedInDataManager = dataManager.get("stores");
        assertEquals("999 New Address", storedInDataManager.get("store1").getAddress());
    }

    @Test
    @Order(3)
    @DisplayName("Integration: Delete store and verify removal")
    public void testDeleteStore() {
        assertTrue(storeRepository.existsById("store2"));

        storeRepository.delete("store2");

        assertFalse(storeRepository.existsById("store2"));
        Map<String, Store> allStores = storeRepository.findAll();
        assertEquals(1, allStores.size());
        assertFalse(allStores.containsKey("store2"));

        Map<String, Store> storedInDataManager = dataManager.get("stores");
        assertFalse(storedInDataManager.containsKey("store2"));
    }

    @Test
    @Order(4)
    @DisplayName("Integration: Register multiple users and verify")
    public void testRegisterMultipleUsers() {
        Map<String, User> initialUsers = userRepository.findAll();
        int initialSize = initialUsers.size();
        assertTrue(initialSize >= 2);

        User u1 = new User("u1@store.com", "pw1", "User One");
        User u2 = new User("u2@store.com", "pw2", "User Two");

        userRepository.save(u1);
        userRepository.save(u2);

        Map<String, User> allUsers = userRepository.findAll();
        assertEquals(initialSize + 2, allUsers.size());
        assertSame(u1, allUsers.get("u1@store.com"));
        assertSame(u2, allUsers.get("u2@store.com"));
    }

    @Test
    @Order(5)
    @DisplayName("Integration: Update user and verify changes")
    public void testUpdateUser() {
        User user = userRepository.findByEmail("u1@store.com").orElseThrow();

        user.setPassword("newpw1");
        user.setName("User One Updated");
        userRepository.save(user);

        User updated = userRepository.findByEmail("u1@store.com").orElseThrow();
        assertEquals("newpw1", updated.getPassword());
        assertEquals("User One Updated", updated.getName());

        Map<String, User> usersInDM = dataManager.get("users");
        assertEquals("newpw1", usersInDM.get("u1@store.com").getPassword());
    }

    @Test
    @Order(6)
    @DisplayName("Integration: Cross-repository data consistency")
    public void testCrossRepositoryConsistency() {
        Map<String, Store> storesInDM = dataManager.get("stores");
        Map<String, Store> storesFromRepo = storeRepository.findAll();
        assertEquals(storesInDM.size(), storesFromRepo.size());
        storesFromRepo.forEach((id, store) -> assertSame(storesInDM.get(id), store));

        Map<String, User> usersInDM = dataManager.get("users");
        Map<String, User> usersFromRepo = userRepository.findAll();
        assertEquals(usersInDM.size(), usersFromRepo.size());
        usersFromRepo.forEach((email, user) -> assertSame(usersInDM.get(email), user));
    }

    @Test
    @Order(7)
    @DisplayName("Integration: Concurrent repository operations")
    public void testConcurrentOperations() {
        Thread storeThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                String id = "concurrent-store-" + i;
                Store s = new Store(id, "Addr " + i, "Desc " + i);
                storeRepository.save(s);
            }
        });

        Thread userThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                String email = "concurrent" + i + "@store.com";
                User u = new User(email, "pw" + i, "User " + i);
                userRepository.save(u);
            }
        });

        storeThread.start();
        userThread.start();
        try {
            storeThread.join();
            userThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Test interrupted: " + e.getMessage());
        }

        Map<String, Store> stores = storeRepository.findAll();
        Map<String, User> users = userRepository.findAll();


        for (int i = 0; i < 10; i++) {
            assertTrue(stores.containsKey("concurrent-store-" + i));
            assertTrue(users.containsKey("concurrent" + i + "@store.com"));
        }
    }
}
