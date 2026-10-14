package com.app.storemanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingResourceReturnsNotFound() {
        HttpServletRequest request = request("/favicon.ico");
        NoResourceFoundException exception = new NoResourceFoundException(
                HttpMethod.GET, "static", "favicon.ico"
        );

        ErrorResponse response = handler.handleNoResourceFound(exception, request);

        assertEquals(404, response.status());
        assertEquals("Resource not found", response.message());
        assertEquals("/favicon.ico", response.path());
    }

    @Test
    void unsupportedMethodReturnsMethodNotAllowed() {
        HttpServletRequest request = request("/api/products");
        HttpRequestMethodNotSupportedException exception =
                new HttpRequestMethodNotSupportedException("PATCH");

        ErrorResponse response = handler.handleMethodNotSupported(exception, request);

        assertEquals(405, response.status());
        assertEquals("HTTP method not supported for this resource", response.message());
        assertEquals("/api/products", response.path());
    }

    private MockHttpServletRequest request(String path) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(path);
        return request;
    }
}
