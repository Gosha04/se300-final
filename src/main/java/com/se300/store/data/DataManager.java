package com.se300.store.data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataManager - Singleton class for managing data storage.
 * Provides centralized data management for the application.
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class DataManager {

    //TODO: Implement Fake Persistent Data Storage for the application

    private static volatile DataManager instance;

    //TODO: Use ConcurrentHashMap for thread-safety
    private final Map<String, Object> dataStore;

    // Private constructor to prevent instantiation
    private DataManager() {
        dataStore = new ConcurrentHashMap<>();
    }

    public static DataManager getInstance() {
         
        if (instance == null) {
            synchronized (DataManager.class) {
                if (instance == null) {
                    instance = new DataManager();
                }
            }
        }
        return instance;
    }

    /**
     * Store data with a given key
     */
    public <T> void put(String key, T value) {
        dataStore.put(key, value);
    }

    /**
     * Retrieve data by key
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) dataStore.get(key);
    }

    /**
     * Check if key exists
     */
    public boolean containsKey(String key) {
        return dataStore.containsKey(key);
    }

    /**
     * Remove data by key
     */
    public void remove(String key) {
        dataStore.remove(key);
    }

    /**
     * Clear all data
     */
    public void clear() {
        dataStore.clear();
    }

    /**
     * Get all keys
     */
    public Iterable<String> keys() {
        return dataStore.keySet();
    }

    /**
     * Get the size of the datastore
     */
    public int size() {
        return dataStore.size();
    }
}
