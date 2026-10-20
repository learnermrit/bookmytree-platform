package com.bookmytree.platform.common.exception;

import org.springframework.http.HttpStatus;

public class ConcurrencyLockException extends BaseException {
    public ConcurrencyLockException(String message) {
        super(message, HttpStatus.CONFLICT); // 409 Conflict when Redlock fails
    }
}