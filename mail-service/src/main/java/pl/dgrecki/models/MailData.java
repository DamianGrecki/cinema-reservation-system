package pl.dgrecki.models;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MailData {
    private String to;
    private String subject;
    private String templateName;
    private String templateHtml;
    private List<Attachment> attachments;
    private Map<String, Object> variables;
}
