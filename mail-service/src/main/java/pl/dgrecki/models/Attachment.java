package pl.dgrecki.models;

public record Attachment(String fileName, String contentType, byte[] content) {}
