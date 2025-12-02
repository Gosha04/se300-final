package com.se300.store.repository.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;

/**
 * Unit tests for the Repository classes including StoreRepository and UserRepository.
 * This test class uses JUnit 5 and Mockito frameworks to verify the expected behavior
 * of the repository operations with mocked dependencies.
 */
@DisplayName("Repository Unit Tests")
@ExtendWith(MockitoExtension.class)
public class RepositoryUnitTest {

    //COMPLETE: Implement Unit Tests for the Smart Store Repositories

    @Mock
    private DataManager dataManager;

    private StoreRepository storeRepository;
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        storeRepository = new StoreRepository(dataManager);
        userRepository = new UserRepository(dataManager);
    }

    @Test
    @DisplayName("Test StoreRepository save with mocked DataManager")
    public void testStoreRepositorySave() {
        Map<String, Store> storeMap = new HashMap<>();
        when(dataManager.get("stores")).thenReturn(storeMap);

        Store store = mock(Store.class);
        when(store.getId()).thenReturn("store-1");

        storeRepository.save(store);

        assertEquals(1, storeMap.size());
        assertSame(store, storeMap.get("store-1"));

        verify(dataManager).get("stores");
        verify(dataManager).put("stores", storeMap);
    }

    @Test
    @DisplayName("Test StoreRepository findById with mocked DataManager")
    public void testStoreRepositoryFindById() {
        Store store = mock(Store.class);
        // when(store.getId()).thenReturn("store-1");

        Map<String, Store> storeMap = new HashMap<>();
        storeMap.put("store-1", store);
        when(dataManager.get("stores")).thenReturn(storeMap);

        Optional<Store> found = storeRepository.findById("store-1");
        assertTrue(found.isPresent());
        assertSame(store, found.get());

        Optional<Store> notFound = storeRepository.findById("store-2");
        assertFalse(notFound.isPresent());

        verify(dataManager, times(2)).get("stores");
    }

    @Test
    @DisplayName("Test StoreRepository existsById with mocked DataManager")
    public void testStoreRepositoryExistsById() {
        Store store = mock(Store.class);
        // when(store.getId()).thenReturn("store-1");

        Map<String, Store> storeMap = new HashMap<>();
        storeMap.put("store-1", store);
        when(dataManager.get("stores")).thenReturn(storeMap);

        assertTrue(storeRepository.existsById("store-1"));
        assertFalse(storeRepository.existsById("store-2"));

        verify(dataManager, times(2)).get("stores");
    }

    @Test
    @DisplayName("Test StoreRepository delete with mocked DataManager")
    public void testStoreRepositoryDelete() {
        Store store = mock(Store.class);
        // when(store.getId()).thenReturn("store-1");

        Map<String, Store> storeMap = new HashMap<>();
        storeMap.put("store-1", store);
        when(dataManager.get("stores")).thenReturn(storeMap);

        storeRepository.delete("store-1");

        assertFalse(storeMap.containsKey("store-1"));
        verify(dataManager).get("stores");
        verify(dataManager).put("stores", storeMap);
    }

    @Test
    @DisplayName("Test StoreRepository findAll with mocked DataManager")
    public void testStoreRepositoryFindAll() {
        Store store1 = mock(Store.class);
        when(store1.getId()).thenReturn("store1");
        Store store2 = mock(Store.class);
        when(store2.getId()).thenReturn("store2");

        Map<String, Object> storeMap = new HashMap<>();
        storeMap.put(store1.getId(), store1);
        storeMap.put(store2.getId(), store2);
        when(dataManager.get("stores")).thenReturn(storeMap);

        Map<String, Store> result = storeRepository.findAll();

        assertEquals(2, result.size());
        assertSame(store1, result.get("store1"));
        assertSame(store2, result.get("store2"));
        
        assertNotSame(storeMap, result);

        result.remove("store-1");
        assertEquals(2, storeMap.size()); // original map not affected

        verify(dataManager).get("stores");
    }

    @Test
    @DisplayName("Test UserRepository save with mocked DataManager")
    public void testUserRepositorySave() {
        Map<String, User> userMap = new HashMap<>();
        when(dataManager.get("users")).thenReturn(userMap);

        User user = new User("new@store.com", "pass123", "New User");

        userRepository.save(user);

        assertEquals(1, userMap.size());
        assertSame(user, userMap.get("new@store.com"));

        verify(dataManager).get("users");
        verify(dataManager).put("users", userMap);
    }

    @Test
    @DisplayName("Test UserRepository findByEmail with mocked DataManager")
    public void testUserRepositoryFindByEmail() {
        User user = new User("user@store.com", "pw", "Some User");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user@store.com", user);
        when(dataManager.get("users")).thenReturn(userMap);

        Optional<User> found = userRepository.findByEmail("user@store.com");
        assertTrue(found.isPresent());
        assertSame(user, found.get());

        Optional<User> nullUser = userRepository.findByEmail(null);

        assertEquals(nullUser, Optional.empty());

        Optional<User> notFound = userRepository.findByEmail("missing@store.com");
        assertFalse(notFound.isPresent());
        assertEquals(nullUser, Optional.empty());

        verify(dataManager, times(2)).get("users");
    }

    @Test
    @DisplayName("Test UserRepository existsByEmail with mocked DataManager")
    public void testUserRepositoryExistsByEmail() {
         User user = new User("user@store.com", "pw", "Some User");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user@store.com", user);
        when(dataManager.get("users")).thenReturn(userMap);

        assertTrue(userRepository.existsByEmail("user@store.com"));
        assertFalse(userRepository.existsByEmail("other@store.com"));

        verify(dataManager, times(2)).get("users");
    }

    @Test
    @DisplayName("Test UserRepository delete with mocked DataManager")
    public void testUserRepositoryDelete() {
        User user = new User("user@store.com", "pw", "Some User");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user@store.com", user);
        when(dataManager.get("users")).thenReturn(userMap);

        userRepository.delete("user@store.com");

        assertFalse(userMap.containsKey("user@store.com"));

        verify(dataManager).get("users");
        verify(dataManager).put("users", userMap);
    }

    @Test 
    @DisplayName("Test UserRepository findAll with mocked DataManager")
    public void testUserRepositoryFindAll() {
         User user1 = new User("u1@store.com", "pw1", "User One");
        User user2 = new User("u2@store.com", "pw2", "User Two");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("u1@store.com", user1);
        userMap.put("u2@store.com", user2);
        when(dataManager.get("users")).thenReturn(userMap);

        Map<String, User> result = userRepository.findAll();

        assertEquals(2, result.size());
        assertSame(user1, result.get("u1@store.com"));
        assertSame(user2, result.get("u2@store.com"));
        assertNotSame(userMap, result);

        result.remove("u1@store.com");
        assertEquals(2, userMap.size()); // original map not affected

        verify(dataManager).get("users");
        
    }

    @Test
    @DisplayName("Test Repository operations with null DataManager response")
    public void testRepositoryWithNullDataManager() {
        when(dataManager.get("stores")).thenReturn(null);
        Map<String, Store> stores = storeRepository.findAll();
        assertNotNull(stores);
        assertTrue(stores.isEmpty());

        verify(dataManager).get("stores");
        verify(dataManager).put(eq("stores"), any(Map.class)); 

        when(dataManager.get("users")).thenReturn(null);

        Map<String, User> result = userRepository.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(dataManager).get("users");
        verify(dataManager, times(2)).put(eq("users"), any(Map.class));
    }

    @Test
    void testKeysAndSizeAndRemove() {
        DataManager dataManager = DataManager.getInstance(); 
        Map<String, Store> storeMap = new HashMap<>();
        Map<String, User> usermap = new HashMap<>();

        dataManager.put("stores", storeMap);
        dataManager.put("users", usermap);

        assertEquals(2, dataManager.size());

        Iterable<String> keys = dataManager.keys();
        List<String> list = new ArrayList<>();
        keys.forEach(list::add);
        assertTrue(list.contains("stores"));
        assertTrue(list.contains("users"));

        dataManager.remove("stores");
        assertEquals(1, dataManager.size());
        keys = dataManager.keys();
        list.clear();
        keys.forEach(list::add);
        assertFalse(list.contains("stores"));
        assertTrue(list.contains("users"));
    }
}