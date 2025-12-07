package org.example.validators;

public interface RequestDataValidator<T> {
    void validate(T request);
}
