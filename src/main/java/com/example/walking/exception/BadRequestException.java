package com.example.walking.exception;

/** 잘못된 요청 파라미터 → HTTP 400 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
