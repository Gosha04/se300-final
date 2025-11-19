package com.se300.store.controller;

import java.io.IOException;

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
            sendJsonResponse(response, authenticationService.getAllUsers(), HttpServletResponse.SC_OK);
            return;
        }

        Object user = authenticationService.getUserByEmail(userId);
        if (user == null) {
            sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, "User not found");
            return;
        }

        sendJsonResponse(response, user, HttpServletResponse.SC_OK);
    }

    /**
     * Handle POST requests - Register new user
     * POST /api/v1/users?email=xxx&password=xxx&name=xxx
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null) {
            authenticationService.registerUser(request.getParameter("email"), request.getParameter("password"),
             request.getParameter("name"));
            
            return;
        } 
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "New User Paramaters Incorrect");
    }

    /**
     * Handle PUT requests - Update user information
     * PUT /api/v1/users/{email}?password=xxx&name=xxx
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null) {
            authenticationService.updateUser(request.getParameter("email"), request.getParameter("password"),
             request.getParameter("name"));
            
            return;
        } 
        
        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Update User Paramaters Incorrect");
    }

    /**
     * Handle DELETE requests - Delete user
     * DELETE /api/v1/users/{email}
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo != null) {
            authenticationService.deleteUser(request.getParameter("email"));

            return;
        }

        sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Delete User Paramaters Incorrect");
    }
}