package com.se300.store.service.unit;


import static org.junit.Assert.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.se300.store.model.StoreException;
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
        verify(userRepository, times(2)).findByEmail("random@gmail.com");
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
        
    }

    @Test
    @DisplayName("Test AuthenticationService Basic Authentication - invalid credentials")
    public void testBasicAuthenticationInvalid() {
    }

    @Test
    @DisplayName("Test AuthenticationService Basic Authentication - invalid header format")
    public void testBasicAuthenticationInvalidHeader() {
    }

    @Test
    @DisplayName("Test AuthenticationService delete non-existent user")
    public void testDeleteNonExistentUser() {
    }

    @Test
    @DisplayName("Test StoreService operations (no mocking needed - uses static maps)")
    public void testStoreServiceOperations() throws StoreException {
    }
}