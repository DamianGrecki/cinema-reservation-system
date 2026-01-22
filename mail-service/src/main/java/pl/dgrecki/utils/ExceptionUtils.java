package pl.dgrecki.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionUtils {

    public static String getRootCauseAsString(Throwable cause) {
        StringBuilder stringBuilder = new StringBuilder();

        if (cause == null) {
            return "";
        }

        stringBuilder.append("Exception: ").append(cause.getMessage()).append(System.lineSeparator());

        while (cause.getCause() != null) {
            cause = cause.getCause();
            stringBuilder
                    .append(cause.getClass().getName())
                    .append(": ")
                    .append(cause.getMessage())
                    .append(System.lineSeparator());

            if (stringBuilder.length() >= 1000) {
                stringBuilder.append("...");
                break;
            }
        }

        return stringBuilder.toString();
    }
}
