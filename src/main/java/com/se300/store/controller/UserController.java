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

    // REST CRUD API for User operations
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
            try {
                sendJsonResponse(response, users, HttpServletResponse.SC_OK);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send users list response", e);
            }
            return;
        }

        User user = authenticationService.getUserByEmail(userId);

        if (user == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User Does Not Exist");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'user not found' response", e);
            }
        } else {
            try {
                sendJsonResponse(response, user);
            } catch (IOException e) {
                throw new RuntimeException("Failed to send user detail response", e);
            }
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

            // Missing required params
            if (email == null || password == null || name == null) {
                try {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                            "New User Parameters Incorrect");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send 'bad request - new user params' response", e);
                }
                return;
            }

            // New user
            if (authenticationService.getUserByEmail(email) == null) {
                User created = authenticationService.registerUser(email, password, name);
                try {
                    sendJsonResponse(response, created, HttpServletResponse.SC_CREATED);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send 'user created' response", e);
                }
                return;
            } else {
                // Duplicate user
                try {
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Duplicate User");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send 'duplicate user' response", e);
                }
                return;
            }
        }

        // Wrong endpoint/path
        try {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "Endpoint not found");
        } catch (IOException e) {
            throw new RuntimeException("Failed to send 'endpoint not found' response", e);
        }
    }

    /**
     * Handle PUT requests - Update user information
     * PUT /api/v1/users/{email}?password=xxx&name=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String userId   = extractResourceId(request);
        String password = request.getParameter("password");
        String name     = request.getParameter("name");

        // Missing path parameter
        if (userId == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "email path parameter required");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'email path parameter required' response", e);
            }
            return;
        }

        // Missing body parameters
        if (password == null || name == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Need password or name");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'missing password or name' response", e);
            }
            return;
        }

        // Check existence
        User user = authenticationService.getUserByEmail(userId);
        if (user == null) {
            try {
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                        "User Does Not Exist");
            } catch (IOException e) {
                throw new RuntimeException("Failed to send 'user does not exist' response", e);
            }
            return;
        }

        // Update user
        User updated = authenticationService.updateUser(userId, password, name);
        try {
            sendJsonResponse(response, updated);
        } catch (IOException e) {
            throw new RuntimeException("Failed to send 'user updated' response", e);
        }
    }

    /**
     * Handle DELETE requests - Delete user
     * DELETE /api/v1/users/{email}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email    = extractResourceId(request);
        String pathInfo = request.getPathInfo();

        // DELETE /api/v1/users/{email}
        if (pathInfo != null) {
            User user = authenticationService.getUserByEmail(email);
            if (user == null) {
                try {
                    sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                            "User not found");
                } catch (IOException e) {
                    throw new RuntimeException("Failed to send 'user not found' delete response", e);
                }
                return;
            }

            authenticationService.deleteUser(email);
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        // Missing path parameter
        try {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Delete User Parameters Incorrect");
        } catch (IOException e) {
            throw new RuntimeException("Failed to send 'delete user parameters incorrect' response", e);
        }
    }
}
