package com.se300.store.controller;

import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.service.StoreService;
import com.se300.store.servlet.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collection;

/**
 * REST API controller for Store operations
 * Implements full CRUD operations
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class StoreController extends BaseServlet {

    //TODO: Implement REST CRUD API for Store operations

    private static final String TOKEN = "admin";

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /**
     * Handle GET requests
     * - GET /api/v1/stores (no parameters) - Get all stores
     * - GET /api/v1/stores/{storeId} - Get store by ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String storeId = extractResourceId(request);
        
          // GET ALL STORES
        if (storeId == null) {
            Collection<Store> stores = storeService.getAllStores();
            sendJsonResponse(response, stores);
            return;
        }

        // GET SINGLE STORE
        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "Store Does Not Exist");
        } else {
            sendJsonResponse(response, store);
        }

    }

    /**
     * Handle POST requests - Create new store
     * POST /api/v1/stores?storeId=xxx&name=xxx&address=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId = request.getParameter("storeId");
        String name = request.getParameter("name");
        String address = request.getParameter("address");

        if (storeId == null || name == null || address == null) {
            sendErrorResponse(response, 400, "storeId, name, and address required");
            return;
        }

        // Check if store already exists
        Store existing = null;
        try {
            existing = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (existing != null) {
            sendErrorResponse(response, 400, "Store Already Exists");
            return;
        }

        // Create store
        Store created = null;
        try {
            created = storeService.provisionStore(storeId, name, address, TOKEN);
        } catch (StoreException ignored) {}

        sendJsonResponse(response, created, HttpServletResponse.SC_CREATED);

    }

    /**
     * Handle PUT requests - Update existing store
     * PUT /api/v1/stores/{storeId}?description=xxx&address=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId = extractResourceId(request);
        String description = request.getParameter("description");
        String address = request.getParameter("address");

        if (storeId == null) {
            sendErrorResponse(response, 400, "storeId path parameter required");
            return;
        }

        if (description == null && address == null) {
            sendErrorResponse(response, 400, "Need description or address");
            return;
        }

        // Check existence
        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "Store Does Not Exist");
            return;
        }

        // Update store
        Store updated = null;
        try {
            updated = storeService.updateStore(storeId, description, address);
        } catch (StoreException ignored) {}

        sendJsonResponse(response, updated);

    }

    /**
     * Handle DELETE requests - Delete store
     * DELETE /api/v1/stores/{storeId}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId = extractResourceId(request);

        if (storeId == null) {
            sendErrorResponse(response, 400, "storeId path parameter required");
            return;
        }

        // Check existence
        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "Store Does Not Exist");
            return;
        }

        // Delete
        try {
            storeService.deleteStore(storeId);
        } catch (StoreException ignored) {}

        response.setStatus(HttpServletResponse.SC_NO_CONTENT);

    }
}