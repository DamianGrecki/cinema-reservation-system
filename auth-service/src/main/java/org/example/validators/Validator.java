package org.example.validators;

import java.util.ArrayList;
import java.util.List;

public abstract class Validator {
    protected Validator validator;

    public static Validator link(Validator first, Validator... chain) {
        Validator head = first;
        for (Validator nextInChain : chain) {
            head.validator = nextInChain;
            head = nextInChain;
        }
        return first;
    }

    public List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        check(password, errors);
        if (validator != null) {
            errors.addAll(validator.validate(password));
        }
        return errors;
    }

    protected abstract void check(String password, List<String> errors);
}