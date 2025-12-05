package org.example.exceptions;

import java.util.List;

public class ValidationsException extends RuntimeException {
    private final List<String> messages;

    public ValidationsException(List<String> messages) {
        this.messages = messages;
    }

    public List<String> getMessages() {
        return messages;
    }
}
