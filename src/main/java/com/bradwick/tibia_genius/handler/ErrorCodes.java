package com.bradwick.tibia_genius.handler;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

public enum ErrorCodes {
    NO_CODE(0, NOT_IMPLEMENTED, "No error code available"),
    INCORRECT_CURRENT_PASSWORD(300, BAD_REQUEST, "The current password is incorrect"),
    NEW_PASSWORD_DOES_NOT_MATCH(301, BAD_REQUEST, "The new passwords do not match"),
    ACCOUNT_LOCKED(302, FORBIDDEN, "Your account is locked"),
    ACCOUNT_DISABLED(303, FORBIDDEN, "Your account is disabled"),
    BAD_CREDENTIALS(304, FORBIDDEN, "Invalid login or password"),
    ;

    @Getter
    private final int code;

    @Getter
    private final HttpStatus httpStatus;

    @Getter
    private final String description;

    ErrorCodes(int code, HttpStatus httpStatus, String description) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.description = description;
    }
}
