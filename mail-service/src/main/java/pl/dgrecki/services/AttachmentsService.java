package pl.dgrecki.services;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import pl.dgrecki.models.Attachment;

@Component
@RequiredArgsConstructor
public class AttachmentsService {

    private final RestClient restClient;

    public List<Attachment> downloadAttachments(URI url) {
        byte[] zipBytes = restClient.get().uri(url).retrieve().body(byte[].class);

        return unzip(zipBytes);
    }

    private List<Attachment> unzip(byte[] zipBytes) {
        List<Attachment> attachments = new ArrayList<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                byte[] content = zipInputStream.readAllBytes();
                attachments.add(new Attachment(entry.getName(), "application/pdf", content));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to unzip attachments", e);
        }

        return attachments;
    }
}
