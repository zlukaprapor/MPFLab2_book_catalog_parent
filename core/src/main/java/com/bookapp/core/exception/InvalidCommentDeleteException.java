package com.bookapp.core.exception;

public class InvalidCommentDeleteException extends RuntimeException {
    public InvalidCommentDeleteException(String message) {
        super(message);
    }
}
