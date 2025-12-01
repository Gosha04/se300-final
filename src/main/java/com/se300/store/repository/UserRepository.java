package com.se300.store.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.se300.store.data.DataManager;
import com.se300.store.model.User;

/**
 * User Repository represents the user data access layer
 *
 * @author  Sergey L. Sundukovskiy
 * @version 1.0
 * @since   2025-11-06
 */
public class UserRepository {

    private static final String USERS_KEY = "users";
    private final DataManager dataManager;

    public UserRepository(DataManager dataManager) {
        this.dataManager = dataManager;
        // Initialize user storage in DataManager if not exists
        if (!dataManager.containsKey(USERS_KEY)) {
            Map<String, User> users = new ConcurrentHashMap<>();
            // Add default test users
            users.put("admin@store.com", new User("admin@store.com", "admin123", "Admin User"));
            users.put("user@store.com", new User("user@store.com", "user123", "Regular User"));
            dataManager.put(USERS_KEY, users);
        }
    }

    /**
     * Find user by email
     */
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        Map<String, User> users = getUsersMap();
        return Optional.ofNullable(users.get(email));
    }

    /**
     * Save or update a user
     */
    public void save(User user) {
        Map<String, User> users = getUsersMap();
        users.put(user.getEmail(), user);
        dataManager.put(USERS_KEY, users);
    }

    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        Map<String, User> users = getUsersMap();
        return users.containsKey(email);
    }

    /**
     * Delete user by email
     */
    public void delete(String email) {
        Map<String, User> users = getUsersMap();
        users.remove(email);
        dataManager.put(USERS_KEY, users);
    }

    /**
     * Get all users
     */
    public Map<String, User> findAll() {
        return new HashMap<>(getUsersMap());
    }

    /**
     * Helper method to get users map from DataManager
     */
    @SuppressWarnings("unchecked")
    private Map<String, User> getUsersMap() {
        Map<String, User> users = dataManager.get(USERS_KEY);
        if (users == null) {
            users = new ConcurrentHashMap<>();
            dataManager.put(USERS_KEY, users);
        }
        return users;
    }
}
