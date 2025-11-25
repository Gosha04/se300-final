package com.se300.store.repository.unit;

import com.se300.store.data.DataManager;
import com.se300.store.model.Store;
import com.se300.store.model.User;
import com.se300.store.repository.StoreRepository;
import com.se300.store.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the Repository classes including StoreRepository and UserRepository.
 * This test class uses JUnit 5 and Mockito frameworks to verify the expected behavior
 * of the repository operations with mocked dependencies.
 */
@DisplayName("Repository Unit Tests")
@ExtendWith(MockitoExtension.class)
public class RepositoryUnitTest {

    //TODO: Implement Unit Tests for the Smart Store Repositories

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

        User user = mock(User.class);
        when(user.getEmail()).thenReturn("new@store.com");

        userRepository.save(user);

        assertEquals(1, userMap.size());
        assertSame(user, userMap.get("new@store.com"));

        verify(dataManager).get("users");
        verify(dataManager).put("users", userMap);
    }

    @Test
    @DisplayName("Test UserRepository findByEmail with mocked DataManager")
    public void testUserRepositoryFindByEmail() {
        User user = mock(User.class);
        // when(user.getEmail()).thenReturn("user@store.com");

        Map<String, User> userMap = new HashMap<>();
        userMap.put("user@store.com", user);
        when(dataManager.get("users")).thenReturn(userMap);

        Optional<User> found = userRepository.findByEmail("user@store.com");
        assertTrue(found.isPresent());
        assertSame(user, found.get());

        Optional<User> notFound = userRepository.findByEmail("missing@store.com");
        assertFalse(notFound.isPresent());

        verify(dataManager, times(2)).get("users");
    }

    @Test
    @DisplayName("Test UserRepository existsByEmail with mocked DataManager")
    public void testUserRepositoryExistsByEmail() {
        User user = mock(User.class);
        // when(user.getEmail()).thenReturn("user@store.com");

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
        User user = mock(User.class);
        // when(user.getEmail()).thenReturn("user@store.com");

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
        User user1 = mock(User.class);
        // when(user1.getEmail()).thenReturn("u1@store.com");
        User user2 = mock(User.class);
        // when(user2.getEmail()).thenReturn("u2@store.com");

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
        assertThrows(NullPointerException.class, () -> storeRepository.findAll());

        when(dataManager.get("users")).thenReturn(null); 

        Map<String, User> result = userRepository.findAll();
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(dataManager).get("users");
        verify(dataManager, times(2)).put(eq("users"), any(Map.class));
    }
}