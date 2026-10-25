package com.mjsamaha.redshellissuetracker.common.errors;

public class InvalidRequestException extends RuntimeException {
    public InvalidRequestException(String message) { super(message); }
}
