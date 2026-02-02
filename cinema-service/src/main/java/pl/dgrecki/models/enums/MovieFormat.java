package pl.dgrecki.models.enums;

public enum MovieFormat {
    FORMAT_2D("2D"),
    FORMAT_3D("3D");

    private final String format;

    MovieFormat(String format) {
        this.format = format;
    }

    @Override
    public String toString() {
        return format;
    }
}
