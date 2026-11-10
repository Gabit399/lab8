package com.example.lab8.exception;

/** Mapped to HTTP 409 (duplicate email, duplicate course code, record still referenced). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
