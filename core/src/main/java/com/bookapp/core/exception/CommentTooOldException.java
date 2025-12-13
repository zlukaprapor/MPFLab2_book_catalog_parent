package com.bookapp.core.exception;

public class CommentTooOldException extends RuntimeException {
    public CommentTooOldException(String message) {
        super(message);
    }
}