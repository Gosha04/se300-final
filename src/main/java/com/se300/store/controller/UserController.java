package com.se300.store.controller;

import java.io.IOException;
import java.util.Collection;

import com.se300.store.model.User;
import com.se300.store.service.AuthenticationService;
import com.se300.store.servlet.BaseServlet;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * REST API controller for User operations
 * Implements full CRUD operations
 *
 * @author Sergey L. Sundukovskiy, Ph.D.
 * @version 1.0
 */
public class UserController extends BaseServlet {

    //COMPLETE: Implement REST CRUD API for User operations

    private final AuthenticationService authenticationService;

    public UserController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Handle GET requests
     * - GET /api/v1/users (no parameters) - Get all users
     * - GET /api/v1/users/{email} - Get user by email
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = extractResourceId(request);
        if (userId == null) {
            Collection<User> users = authenticationService.getAllUsers();
            sendJsonResponse(response, users, HttpServletResponse.SC_OK);
            return;
        }

        Object user = authenticationService.getUserByEmail(userId);

        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "User Does Not Exist");
        } else {
            sendJsonResponse(response, user);
        }
    }

    /**
     * Handle POST requests - Register new user
     * POST /api/v1/users?email=xxx&password=xxx&name=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo(); 

        if (pathInfo == null || "/".equals(pathInfo)) {
            String email    = request.getParameter("email");
            String password = request.getParameter("password");
            String name     = request.getParameter("name");

            if (email == null || password == null || name == null) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "New User Parameters Incorrect");
                return;
            }

            if (authenticationService.getUserByEmail(email) == null) {
                User created = authenticationService.registerUser(email, password, name);
                sendJsonResponse(response, created, HttpServletResponse.SC_CREATED);
                return;
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Duplicate User");
            }

        }

    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
    }

    /**
     * Handle PUT requests - Update user information
     * PUT /api/v1/users/{email}?password=xxx&name=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId = extractResourceId(request);
        String password = request.getParameter("password");
        String name     = request.getParameter("name");
        
       if (userId == null) {
            sendErrorResponse(response, 400, "email path parameter required");
            return;
        }

        if (userId == null || name == null) {
            sendErrorResponse(response, 400, "Need password or name");
            return;
        }

        // Check existence
        User user = null;
        try {
            user = authenticationService.getUserByEmail(userId);
        } catch (Exception ignored) {}

        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "User Does Not Exist");
            return;
        }

        // Update store
        User updated = null;
        try {
            updated = authenticationService.updateUser(userId, password, name);
        } catch (Exception ignored) {}

        sendJsonResponse(response, updated);
    }

    /**
     * Handle DELETE requests - Delete user
     * DELETE /api/v1/users/{email}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = extractResourceId(request);

        String pathInfo = request.getPathInfo();

        if (pathInfo != null) {
            User user = authenticationService.getUserByEmail(email);
            if (user == null) {
                sendErrorResponse(response, 404, "User not found");
                return;
            }

            authenticationService.deleteUser(email);
            response.setStatus(204);
            return;
        }
        sendErrorResponse(response, 400, "Delete User Parameters Incorrect");
    }
}