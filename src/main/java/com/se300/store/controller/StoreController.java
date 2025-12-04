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

    // private static final String TOKEN = "admin";

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
        String token   = request.getParameter("token");

        if (token == null || token.isBlank()) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "No token");
            return;
        }

        // GET ALL STORES
        if (storeId == null) {
            Collection<Store> stores = storeService.getAllStores();
                sendJsonResponse(response, stores);
            return;
        }

        // GET SINGLE STORE
        Store store = null;
        try {
            store = storeService.showStore(storeId, token);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Store Does Not Exist");

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
        String token    = request.getParameter("token");
        String storeId  = request.getParameter("storeId");
        String name     = request.getParameter("name");
        String address  = request.getParameter("address");

        if (storeId == null || name == null || address == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "storeId, name, and address required");
            return;
        }

        if (token == null || token.isBlank()) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        if (!"admin".equals(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Only admin allowed to create store");
            return;
        }

        Store existing = null;
        try {
            existing = storeService.showStore(storeId, token);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        if (existing != null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Store Already Exists");
            return;
        }

        Store created = null;
        try {
            created = storeService.provisionStore(storeId, name, address, token);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        sendJsonResponse(response, created, HttpServletResponse.SC_CREATED);
    }

    /**
     * Handle PUT requests - Update existing store
     * PUT /api/v1/stores/{storeId}?description=xxx&address=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token       = request.getParameter("token");
        String storeId     = extractResourceId(request);
        String description = request.getParameter("description");
        String address     = request.getParameter("address");

        if (token == null || token.isBlank()) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        if (!"admin".equals(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Only admin allowed to update store");
            return;
        }

        if (storeId == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "storeId path parameter required");
            return;
        }

        if (description == null || address == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Need description or address");
            return;
        }


        Store store = null;
        try {
            store = storeService.showStore(storeId, token);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Store Does Not Exist");
            return;
        }

        Store updated = null;
        try {
            updated = storeService.updateStore(storeId, description, address);
        } catch (StoreException ignored) {} // Not hit by coverage, errors are thrown on model level

        sendJsonResponse(response, updated);
    }

    /**
     * Handle DELETE requests - Delete store
     * DELETE /api/v1/stores/{storeId}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token    = request.getParameter("token");
        String storeId = extractResourceId(request);

        if (storeId == null) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "storeId path parameter required");
            return;
        }

        if (token == null || token.isBlank()) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Missing token");
            return;
        }

        if (!"admin".equals(token)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Only admin can delete store");
            return;
        }

        // Check existence
        Store store = null;
        try {
            store = storeService.showStore(storeId, token);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        if (store == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Store Does Not Exist");
            return;
        }

        // Delete
        try {
            storeService.deleteStore(storeId);
        } catch (StoreException ignored) {}  // Not hit by coverage, errors are thrown on model level

        // 204 No Content, no body so no JSON wrapping needed
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
