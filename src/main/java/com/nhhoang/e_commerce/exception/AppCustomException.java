package com.nhhoang.e_commerce.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AppCustomException extends RuntimeException {
    private final HttpStatus status;

    public AppCustomException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public AppCustomException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}
