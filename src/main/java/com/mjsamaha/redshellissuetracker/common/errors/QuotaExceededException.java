package com.mjsamaha.redshellissuetracker.common.errors;

public class QuotaExceededException extends RuntimeException {
    public QuotaExceededException(String message) { super(message); }
}
