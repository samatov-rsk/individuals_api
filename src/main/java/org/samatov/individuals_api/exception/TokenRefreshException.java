package org.samatov.individuals_api.exception;

public class TokenRefreshException extends RuntimeException {
    public TokenRefreshException(String message, Throwable cause) { super(message, cause); }
}
