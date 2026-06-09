package com.example.walking.exception;

/** 리소스를 찾을 수 없음 → HTTP 404 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
