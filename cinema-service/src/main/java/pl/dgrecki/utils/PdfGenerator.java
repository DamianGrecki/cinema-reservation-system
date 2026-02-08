package pl.dgrecki.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import pl.dgrecki.exceptions.PdfGenerationException;

@UtilityClass
public class PdfGenerator {

    private static File fontFile;

    @SneakyThrows
    public static void generatePdf(String html, String outputPath) {
        try (FileOutputStream os = new FileOutputStream(outputPath)) {
            File fontFile = getFontFile();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.useFont(fontFile, "Roboto");
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new PdfGenerationException("PDF generation failed", e);
        }
    }

    private static File getFontFile() throws IOException {
        if (fontFile == null) {
            InputStream fontStream = PdfGenerator.class.getResourceAsStream("/fonts/Roboto-Regular.ttf");
            if (fontStream == null) {
                throw new IOException("Font Roboto-Regular.ttf not found in classpath");
            }
            fontFile = File.createTempFile("roboto", ".ttf");
            fontFile.deleteOnExit();
            Files.copy(fontStream, fontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return fontFile;
    }
}
