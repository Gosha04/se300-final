package com.se300.store.controller.internalmockserver;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;

import org.apache.catalina.Context;
import org.apache.catalina.Server;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedConstruction;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

import com.se300.store.SmartStoreApplication;

/**
 * A test class for verifying internal Smart Store API calls using a mock server.
 * Ensures the functionality of multiple API endpoints and tests various scenarios
 * such as successful requests, error handling, and unauthorized access.
 */
@DisplayName("Internal Mock Server Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InternalMockServerTest {

    //COMPLETE: Implement Internal Mock Server to test internal Smart Store API calls

    private static ClientAndServer mockServer;
    private static final int MOCK_SERVER_PORT = 8888;

    @BeforeAll
    public static void setUpMockServer() {
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
    }

    @AfterAll
    public static void tearDownMockServer() {
        if (mockServer != null) {
            mockServer.stop();
        }
    }

    @BeforeEach
    public void setUp() {
        mockServer.reset();
    }

    @Test
    @Order(1)
    @DisplayName("Mock Server: Test internal store provisioning API endpoint")
    public void testInternalStoreProvisioningAPI() {
        new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/internal/store")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"id\":\"S1\",\"name\":\"Main Store\",\"address\":\"123 Road\"}")
                );

        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
                .header("Content-Type", "application/json")
                .body("{\"id\":\"S1\",\"name\":\"Main Store\",\"address\":\"123 Road\"}")
        .when()
                .post("/internal/store")
        .then()
                .statusCode(201)
                .body("id", equalTo("S1"))
                .body("name", equalTo("Main Store"))
                .body("address", equalTo("123 Road"));
    }

    @Test
    @Order(2)
    @DisplayName("Mock Server: Test internal store retrieval API endpoint")
    public void testInternalStoreRetrievalAPI() {
        new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/internal/store/S1")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"id\":\"S1\",\"name\":\"Main Store\",\"address\":\"123 Road\"}")
                );


        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
        .when()
                .get("/internal/store/S1")
        .then()
                .statusCode(200)
                .body("id", equalTo("S1"))
                .body("name", equalTo("Main Store"))
                .body("address", equalTo("123 Road"));
    }

    @Test
    @Order(3)
    @DisplayName("Mock Server: Test internal user registration API endpoint")
    public void testInternalUserRegistrationAPI() {
        new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/internal/user/register")
                )
                .respond(
                        response()
                                .withStatusCode(201)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"email\":\"user@store.com\",\"name\":\"Test User\"}")
                );

        // Act + Assert
        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
                .header("Content-Type", "application/json")
                .body("{\"email\":\"user@store.com\",\"password\":\"pw\",\"name\":\"Test User\"}")
        .when()
                .post("/internal/user/register")
        .then()
                .statusCode(201)
                .body("email", equalTo("user@store.com"))
                .body("name", equalTo("Test User"));
    }

    @Test
    @Order(4)
    @DisplayName("Mock Server: Test internal authentication API endpoint")
    public void testInternalAuthenticationAPI() {
        new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("POST")
                                .withPath("/internal/auth")
                                .withHeader("Authorization", "Basic dXNlcjp0ZXN0")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"token\":\"abc123\",\"status\":\"OK\"}")
                );

        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
                .header("Authorization", "Basic dXNlcjp0ZXN0")
        .when()
                .post("/internal/auth")
        .then()
                .statusCode(200)
                .body("token", equalTo("abc123"))
                .body("status", equalTo("OK"));
    }

    @Test
    @Order(5)
    @DisplayName("Mock Server: Test internal error handling - 404 Not Found")
    public void testInternalErrorHandling() {
         new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/internal/store/unknown")
                )
                .respond(
                        response()
                                .withStatusCode(404)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"error\":\"Store not found\"}")
                );

        // Act + Assert
        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
        .when()
                .get("/internal/store/unknown")
        .then()
                .statusCode(404)
                .body("error", equalTo("Store not found"));
    }

    @Test
    @Order(6)
    @DisplayName("Mock Server: Test internal unauthorized access - 401")
    public void testInternalUnauthorizedAccess() {
        new MockServerClient("localhost", MOCK_SERVER_PORT)
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/internal/secure/resource")
                )
                .respond(
                        response()
                                .withStatusCode(401)
                                .withHeader(new Header("Content-Type", "application/json"))
                                .withBody("{\"error\":\"Unauthorized\"}")
                );


        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
        .when()
                .get("/internal/secure/resource")
        .then()
                .statusCode(401)
                .body("error", equalTo("Unauthorized"));
    }

    @Test
    @Order(7)
    @DisplayName("Mock Server: Verify request was received")
    public void testMockServerRequestVerification() {
         MockServerClient client = new MockServerClient("localhost", MOCK_SERVER_PORT);
        client
                .when(
                        request()
                                .withMethod("GET")
                                .withPath("/internal/ping")
                )
                .respond(
                        response()
                                .withStatusCode(200)
                                .withHeader(new Header("Content-Type", "text/plain"))
                                .withBody("PONG")
                );

        given()
                .baseUri("http://localhost")
                .port(MOCK_SERVER_PORT)
        .when()
                .get("/internal/ping")
        .then()
                .statusCode(200)
                .body(equalTo("PONG"));

        client.verify(
                request()
                        .withMethod("GET")
                        .withPath("/internal/ping"),
                exactly(1)
        );
    }
    @Test
    @Order(8)
    @DisplayName("Test for coverage") 
    public void testServerCoverage() {
        try (MockedConstruction<Tomcat> mocked = mockConstruction(Tomcat.class,
                (mockTomcat, context) -> {
                    when(mockTomcat.getConnector()).thenReturn(null);

                    Context mockContext = mock(Context.class);
                    when(mockTomcat.addContext(anyString(), anyString()))
                            .thenReturn(mockContext);

                    Server mockServer = mock(Server.class);
                    when(mockTomcat.getServer()).thenReturn(mockServer);
                })) {

            SmartStoreApplication app = new SmartStoreApplication();

            assertDoesNotThrow(app::start);

            Tomcat mockTomcat = mocked.constructed().get(0);
            Server mockServer = mockTomcat.getServer();

            verify(mockServer).await();
        }

        try (MockedConstruction<Tomcat> mocked = mockConstruction(Tomcat.class,
                (mockTomcat, context) -> {
                    when(mockTomcat.getConnector()).thenReturn(null);
                    Context mockContext = mock(Context.class);
                    when(mockTomcat.addContext(anyString(), anyString()))
                            .thenReturn(mockContext);
                    Server mockServer = mock(Server.class);
                    when(mockTomcat.getServer()).thenReturn(mockServer);
                })) {

            SmartStoreApplication app = new SmartStoreApplication();

            assertDoesNotThrow(app::startNonBlocking);

            Tomcat mockTomcat = mocked.constructed().get(0);
            Server mockServer = mockTomcat.getServer();

            verify(mockServer, never()).await();
        }
    }
}