package pl.dgrecki.validators;

public interface RequestDataValidator<T> {
    void validate(T request);
}
