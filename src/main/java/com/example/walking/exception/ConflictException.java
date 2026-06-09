package com.example.walking.exception;

/** 자원 충돌(중복 등) → HTTP 409 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
