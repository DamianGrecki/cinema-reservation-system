package org.example.validators;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class StringLengthValidator extends Validator {
    private final int minLength;
    private final int maxLength;
    private final String toShortStringMessage;
    private final String toLongStringMessage;


    @Override
    protected void check(String text, List<String> errors) {
        if (text.length() < minLength) {
            errors.add(String.format(toShortStringMessage, minLength));
        }
        if (text.length() > maxLength) {
            errors.add(String.format(toLongStringMessage, maxLength));
        }
    }
}
