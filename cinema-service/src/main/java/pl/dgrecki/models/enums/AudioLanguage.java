package pl.dgrecki.models.enums;

public enum AudioLanguage {
    ENGLISH("Angielski"),
    POLISH("Polski");

    private final String language;

    AudioLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return language;
    }
}
