package com.se300.store.controller;

import java.io.IOException;
import java.util.Collection;

import com.se300.store.model.Store;
import com.se300.store.model.StoreException;
import com.se300.store.service.StoreService;
import com.se300.store.servlet.BaseServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST API controller for Store operations
 * Implements full CRUD operations
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class StoreController extends BaseServlet {

    // REST CRUD API for Store operations

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
            try {
                sendJsonResponse(response, stores);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send stores list response", e);
            }
            return;
        }

        // GET SINGLE STORE
        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Store Does Not Exist");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'store not found' response", e);
            }
        } else {
            try {
                sendJsonResponse(response, store);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send store detail response", e);
            }
        }
    }

    /**
     * Handle POST requests - Create new store
     * POST /api/v1/stores?storeId=xxx&name=xxx&address=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId  = request.getParameter("storeId");
        String name     = request.getParameter("name");
        String address  = request.getParameter("address");

        if (storeId == null || name == null || address == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "storeId, name, and address required");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'store parameters required' response", e);
            }
            return;
        }

        Store existing = null;
        try {
            existing = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (existing != null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Store Already Exists");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'store already exists' response", e);
            }
            return;
        }

        Store created = null;
        try {
            created = storeService.provisionStore(storeId, name, address, TOKEN);
        } catch (StoreException ignored) {
            // If provisioning fails, you might want to send a 500 or 400 here instead of blindly returning null.
        }

        try {
            sendJsonResponse(response, created, HttpServletResponse.SC_CREATED);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send 'store created' response", e);
        }
    }

    /**
     * Handle PUT requests - Update existing store
     * PUT /api/v1/stores/{storeId}?description=xxx&address=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId     = extractResourceId(request);
        String description = request.getParameter("description");
        String address     = request.getParameter("address");

        if (storeId == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "storeId path parameter required");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'storeId path parameter required' response", e);
            }
            return;
        }

        if (description == null || address == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Need description or address");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'need description or address' response", e);
            }
            return;
        }

        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Store Does Not Exist");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'store does not exist' response", e);
            }
            return;
        }

        Store updated = null;
        try {
            updated = storeService.updateStore(storeId, description, address);
        } catch (StoreException ignored) {}

        try {
            sendJsonResponse(response, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send 'store updated' response", e);
        }
    }

    /**
     * Handle DELETE requests - Delete store
     * DELETE /api/v1/stores/{storeId}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String storeId = extractResourceId(request);

        if (storeId == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "storeId path parameter required");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'storeId path parameter required' response", e);
            }
            return;
        }

        // Check existence
        Store store = null;
        try {
            store = storeService.showStore(storeId, TOKEN);
        } catch (StoreException ignored) {}

        if (store == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "Store Does Not Exist");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'store does not exist' delete response", e);
            }
            return;
        }

        // Delete
        try {
            storeService.deleteStore(storeId);
        } catch (StoreException ignored) {}

        // 204 No Content, no body so no JSON wrapping needed
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
