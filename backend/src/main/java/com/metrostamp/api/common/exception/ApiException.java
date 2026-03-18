package com.metrostamp.api.common.exception;

public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }
}

